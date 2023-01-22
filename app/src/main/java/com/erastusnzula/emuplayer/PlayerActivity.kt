package com.erastusnzula.emuplayer

import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erastusnzula.emuplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var musicListA: ArrayList<MusicFile>
        var songPosition: Int = 0
        var isActive = false
        var repeat = false
        var isShuffle = false
        var currentPlayingID = ""
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        title = "Active Player"
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
                musicListA = ArrayList()
                musicListA.addAll(MainActivity.musicList)
                currentPlayerLayout()
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)
            }
            "MusicAdapterSearch" -> {
                musicListA = ArrayList()
                musicListA.addAll(MainActivity.musicListSearch)
                currentPlayerLayout()
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)
            }
            "MainActivity" -> {
                musicListA = ArrayList()
                musicListA.addAll(MainActivity.musicList)
                musicListA.shuffle()
                currentPlayerLayout()
                val intent = Intent(this@PlayerActivity, MusicService::class.java)
                bindService(intent, this@PlayerActivity, BIND_AUTO_CREATE)
                startService(intent)

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
        }
    }

    private fun currentPlayerLayout() {
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
            if (repeat) {
                musicService!!.displayNotification(
                    R.drawable.ic_baseline_pause_24,
                    R.drawable.ic_baseline_repeat_one_24
                )
            } else {
                musicService!!.displayNotification(
                    R.drawable.ic_baseline_pause_24,
                    R.drawable.ic_baseline_repeat_24
                )
            }
            binding.startA.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.endA.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            currentPlayingID = musicListA[songPosition].id
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

        } catch (e: Exception) {
            return
        }

    }

    private fun play() {
        binding.playA.setImageResource(R.drawable.ic_baseline_pause_24)
        if (repeat) {
            musicService!!.displayNotification(
                R.drawable.ic_baseline_pause_24,
                R.drawable.ic_baseline_repeat_one_24
            )
        } else {
            musicService!!.displayNotification(
                R.drawable.ic_baseline_pause_24,
                R.drawable.ic_baseline_repeat_24
            )
        }
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
        if (repeat) {
            musicService!!.displayNotification(
                R.drawable.ic_baseline_play_circle_filled_24,
                R.drawable.ic_baseline_repeat_one_24
            )
        } else {
            musicService!!.displayNotification(
                R.drawable.ic_baseline_play_circle_filled_24,
                R.drawable.ic_baseline_repeat_24
            )
        }

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



}

