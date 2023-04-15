package com.erastusnzula.emuplayer

import android.content.*
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erastusnzula.emuplayer.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var musicListA: ArrayList<MusicFile>
        var songPosition: Int = 0
        var isActive = false
        var repeat = false
        var isShuffle = false
        var mins10: Boolean = false
        var mins20:Boolean = false
        var mins30:Boolean = false
        var isFavourite = false
        var favouriteIndex: Int = -1
        var currentPlayingID = ""
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        title = "Active"
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        currentPlayerInitialize()
//        binding.currentSongA.isSelected = true
//        binding.albumNameA.isSelected = true
        binding.playA.setOnClickListener {
            if (isActive) pause() else play()
        }
        binding.previousA.setOnClickListener { playPreviousOrNext(increment = false) }
        binding.nextA.setOnClickListener { playPreviousOrNext(increment = true) }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    if (isActive) {
                        musicService!!.displayNotification(
                            R.drawable.ic_baseline_pause_24,
                            R.drawable.ic_baseline_repeat_one_24
                        )
                    } else {
                        musicService!!.displayNotification(
                            R.drawable.ic_baseline_play_circle_filled_24,
                            R.drawable.ic_baseline_repeat_one_24
                        )
                    }

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        binding.equalizerA.setOnClickListener {
            try {
                val equalizerIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                equalizerIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                equalizerIntent.putExtra(
                    AudioEffect.EXTRA_CONTENT_TYPE,
                    AudioEffect.CONTENT_TYPE_MUSIC
                )
                startActivityForResult(equalizerIntent, 5)
            }catch (e:Exception){
                return@setOnClickListener
            }
        }
        binding.repeatA.setOnClickListener {
            if (!repeat) {
                repeat = true
                isShuffle = false
                binding.shuffleA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.mainPrimaryColor
                    )
                )
                if (isActive) {
                    musicService!!.displayNotification(
                        R.drawable.ic_baseline_pause_24,
                        R.drawable.ic_baseline_repeat_one_24
                    )
                } else {
                    musicService!!.displayNotification(
                        R.drawable.ic_baseline_play_circle_filled_24,
                        R.drawable.ic_baseline_repeat_one_24
                    )
                }
                binding.repeatA.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                binding.repeatA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.activePageColor
                    )
                )
            } else {
                repeat = false
                if (isActive) {
                    musicService!!.displayNotification(
                        R.drawable.ic_baseline_pause_24,
                        R.drawable.ic_baseline_repeat_24
                    )
                } else {
                    musicService!!.displayNotification(
                        R.drawable.ic_baseline_play_circle_filled_24,
                        R.drawable.ic_baseline_repeat_24
                    )
                }
                binding.repeatA.setImageResource(R.drawable.ic_baseline_repeat_24)
                binding.repeatA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.mainPrimaryColor
                    )
                )
            }
        }

        binding.shuffleA.setOnClickListener {
            if (!isShuffle) {
                isShuffle = true
                repeat = false
                binding.repeatA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.mainPrimaryColor
                    )
                )
                binding.repeatA.setImageResource(R.drawable.ic_baseline_repeat_24)
                if (isActive) {
                    musicService!!.displayNotification(
                        R.drawable.ic_baseline_pause_24,
                        R.drawable.ic_baseline_repeat_24
                    )
                } else {
                    musicService!!.displayNotification(
                        R.drawable.ic_baseline_play_circle_filled_24,
                        R.drawable.ic_baseline_repeat_24
                    )
                }
                binding.shuffleA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.activePageColor
                    )
                )

            } else {
                isShuffle = false
                binding.shuffleA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.mainPrimaryColor
                    )
                )
            }
        }

        binding.shareA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing audio file"))
        }

        binding.alarmA.setOnClickListener {
            val timer = mins10 || mins20 || mins30
            if (!timer){
                appCloseDialog()
            }else{
                val builder = MaterialAlertDialogBuilder(this)
                    .setTitle("Stop timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("Yes"){_,_, ->
                        mins10 = false
                        mins20 = false
                        mins30 = false
                        binding.alarmA.setColorFilter(ContextCompat.getColor(this, R.color.mainPrimaryColor))
                    }
                    .setNegativeButton("No"){ dialog,_, ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }

        }

        binding.favouriteA.setOnClickListener {
            if (isFavourite){
                try {
                    isFavourite = false
                    binding.favouriteA.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    FavouriteActivity.favouriteList.remove(musicListA[songPosition])
                }catch (e:Exception){
                    return@setOnClickListener
                }
            }else{
                isFavourite = true
                binding.favouriteA.setImageResource(R.drawable.ic_baseline_favorite_24)
                FavouriteActivity.favouriteList.add(musicListA[songPosition])


            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5 || resultCode == RESULT_OK){
            return
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        musicService!!.audioBecomingNoisy = BecomingNoisy()
        musicService!!.intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        createMediaPlayer()
        musicService!!.seekBarSetUp()


    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        Glide.with(applicationContext)
            .load(musicListA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_baseline_music_splash).centerCrop())
            .into(binding.currentAlbumImage)
        binding.currentSongA.text = musicListA[songPosition].title
        binding.albumNameA.text = musicListA[songPosition].album
        if (repeat) {
            binding.repeatA.setColorFilter(ContextCompat.getColor(this, R.color.activePageColor))
            binding.repeatA.setImageResource(R.drawable.ic_baseline_repeat_one_24)
        }
        if (isShuffle) {
            binding.shuffleA.setColorFilter(ContextCompat.getColor(this, R.color.activePageColor))
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun currentPlayerInitialize() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "MusicAdapter" -> {
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)
                musicListA = ArrayList()
                musicListA.addAll(MainActivity.musicList)
                currentPlayerLayout()

            }
            "MusicAdapterSearch" -> {
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)
                musicListA = ArrayList()
                musicListA.addAll(MainActivity.musicListSearch)
                currentPlayerLayout()

            }
            "MainActivity" -> {
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)
                musicListA = ArrayList()
                musicListA.addAll(MainActivity.musicList)
                musicListA.shuffle()
                currentPlayerLayout()


            }
            "CurrentPlaying" -> {
                currentPlayerLayout()
                binding.startA.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.endA.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                if (isActive) {
                    binding.playA.setImageResource(R.drawable.ic_baseline_pause_24)
                } else {
                    binding.playA.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                }
            }
            "FavouriteAdapter" ->{
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)
                musicListA = ArrayList()
                musicListA.addAll(FavouriteActivity.favouriteList)
                currentPlayerLayout()

            }
            "PlaylistDetailsAdapter" ->{
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)
                musicListA = ArrayList()
                musicListA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist)
                currentPlayerLayout()
            }
        }
    }

    private fun currentPlayerLayout() {
        favouriteIndex = checkIfFavourite(musicListA[songPosition].id)
        Glide.with(this@PlayerActivity)
            .load(musicListA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_baseline_music_splash).centerCrop())
            .into(binding.currentAlbumImage)
        binding.currentSongA.text = musicListA[songPosition].title
        binding.albumNameA.text = musicListA[songPosition].album
        if (repeat) {
            binding.repeatA.setColorFilter(ContextCompat.getColor(this, R.color.activePageColor))
            binding.repeatA.setImageResource(R.drawable.ic_baseline_repeat_one_24)
        }
        if (isShuffle) {
            binding.shuffleA.setColorFilter(ContextCompat.getColor(this, R.color.activePageColor))
        }
        if (mins10 || mins20 || mins30){
            binding.alarmA.setColorFilter(ContextCompat.getColor(this, R.color.red))
        }
        if (isFavourite){
            binding.favouriteA.setImageResource(R.drawable.ic_baseline_favorite_24)
        }else{
            binding.favouriteA.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isActive = true
            try{
                registerReceiver(musicService!!.audioBecomingNoisy, musicService!!.intentFilter)
            }catch (e:Exception){return}
            binding.playA.setImageResource(R.drawable.ic_baseline_pause_24)
            repeatPlayControl()
            binding.startA.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.endA.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            currentPlayingID = musicListA[songPosition].id
            CurrentPlayingFragment.binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            CurrentPlayingFragment.binding.fragmentSongName.text = musicListA[songPosition].title
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

        } catch (e: Exception) {
            return
        }

    }

    private fun play() {
        binding.playA.setImageResource(R.drawable.ic_baseline_pause_24)
        repeatPlayControl()
        musicService!!.mediaPlayer!!.start()
        isActive = true
        try{
            registerReceiver(musicService!!.audioBecomingNoisy, musicService!!.intentFilter)
        }catch (e:Exception){return}


    }

    private fun pause() {
        isActive = false
        binding.playA.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        musicService!!.mediaPlayer!!.pause()
        repeatPauseControl()
        try{
            unregisterReceiver(musicService!!.audioBecomingNoisy)
        }catch(_:Exception){}
    }

    private fun playPreviousOrNext(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            currentPlayerLayout()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            currentPlayerLayout()
            createMediaPlayer()
        }
    }

    private fun appCloseDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.app_close)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.mins_10)?.setOnClickListener {
            binding.alarmA.setColorFilter(ContextCompat.getColor(this, R.color.red))
            mins10 = true
            Thread{
                Thread.sleep(60000*10)
                if (mins10) exitProtocol()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.mins_20)?.setOnClickListener {
            binding.alarmA.setColorFilter(ContextCompat.getColor(this, R.color.red))
            mins20 = true
            Thread{
                Thread.sleep(60000*20)
                if (mins20) exitProtocol()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.mins_30)?.setOnClickListener {
            binding.alarmA.setColorFilter(ContextCompat.getColor(this, R.color.red))
            mins30 = true
            Thread{
                Thread.sleep(60000*30)
                if (mins30) exitProtocol()
            }.start()
            dialog.dismiss()
        }
    }


}

