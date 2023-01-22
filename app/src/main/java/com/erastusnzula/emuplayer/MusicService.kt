package com.erastusnzula.emuplayer

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {
    var mediaPlayer: MediaPlayer? = null
    private var myIBinder = MyBinder()
    lateinit var mediaSession: MediaSessionCompat
    lateinit var audioManager: AudioManager
    private lateinit var runnable: Runnable
    lateinit var audioBecomingNoisy: BecomingNoisy
    lateinit var intentFilter: IntentFilter

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "EMU")
        return myIBinder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            PlayerActivity.isActive = false
            mediaPlayer!!.pause()
            PlayerActivity.binding.playA.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            CurrentPlayingFragment.binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)

            if (PlayerActivity.repeat) {
                displayNotification(
                    R.drawable.ic_baseline_play_circle_filled_24,
                    R.drawable.ic_baseline_repeat_one_24
                )
            } else {
                displayNotification(
                    R.drawable.ic_baseline_play_circle_filled_24,
                    R.drawable.ic_baseline_repeat_24
                )
            }


        }

    }

    fun displayNotification(playIcon: Int, repeatIcon: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            0
        )
        val repeatIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.REPEAT)
        val repeatPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            repeatIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val previousIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.PREVIOUS)
        val previousPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            previousIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val exitIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            exitIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val musicArt = getMusicArt(PlayerActivity.musicListA[PlayerActivity.songPosition].path)
        val largeIcon = if (musicArt != null) {
            BitmapFactory.decodeByteArray(musicArt, 0, musicArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.ic_baseline_music_splash)
        }
        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setLargeIcon(largeIcon)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(repeatIcon, "Repeat", repeatPendingIntent)
            .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", previousPendingIntent)
            .addAction(playIcon, "Play", playPendingIntent)
            .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_baseline_close_24, "Exit", exitPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (PlayerActivity.isActive) 1F else 0F
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            val playBackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {

                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }

        startForeground(1, notification)


    }

    fun createMediaPlayer() {
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) {
                PlayerActivity.musicService!!.mediaPlayer =
                    MediaPlayer()
            }
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.musicService!!.mediaPlayer!!.start()
            PlayerActivity.binding.playA.setImageResource(R.drawable.ic_baseline_pause_24)
            CurrentPlayingFragment.binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            if (PlayerActivity.repeat) {
                PlayerActivity.musicService!!.displayNotification(
                    R.drawable.ic_baseline_pause_24,
                    R.drawable.ic_baseline_repeat_one_24
                )
            } else {
                PlayerActivity.musicService!!.displayNotification(
                    R.drawable.ic_baseline_pause_24,
                    R.drawable.ic_baseline_repeat_24
                )
            }

            PlayerActivity.isActive = true
            PlayerActivity.binding.startA.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.endA.text =
                formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBar.progress = 0
            PlayerActivity.binding.seekBar.max = mediaPlayer!!.duration
            PlayerActivity.currentPlayingID =
                PlayerActivity.musicListA[PlayerActivity.songPosition].id

        } catch (e: Exception) {
            return
        }

    }

    fun seekBarSetUp() {
        runnable = Runnable {
            PlayerActivity.binding.startA.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }


}