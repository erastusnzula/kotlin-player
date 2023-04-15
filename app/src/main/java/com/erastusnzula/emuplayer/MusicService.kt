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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

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

    fun displayNotification(playIcon: Int, repeatIcon: Int) {
        try {
            val intent = Intent(this, MainActivity::class.java)
            val contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else 0
            )
            val repeatIntent = Intent(
                baseContext,
                NotificationReceiver::class.java
            ).setAction(ApplicationClass.REPEAT)
            val repeatPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                repeatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val previousIntent = Intent(
                baseContext,
                NotificationReceiver::class.java
            ).setAction(ApplicationClass.PREVIOUS)
            val previousPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val playIntent = Intent(
                baseContext,
                NotificationReceiver::class.java
            ).setAction(ApplicationClass.PLAY)
            val playPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val nextIntent = Intent(
                baseContext,
                NotificationReceiver::class.java
            ).setAction(ApplicationClass.NEXT)
            val nextPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val exitIntent = Intent(
                baseContext,
                NotificationReceiver::class.java
            ).setAction(ApplicationClass.EXIT)
            val exitPendingIntent = PendingIntent.getBroadcast(
                baseContext,
                0,
                exitIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val musicArt = getMusicArt(PlayerActivity.musicListA[PlayerActivity.songPosition].path)
            val largeIcon = if (musicArt != null) {
                BitmapFactory.decodeByteArray(musicArt, 0, musicArt.size)
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.player_icon)
//                BitmapFactory.decodeResource(resources, R.drawable.large_icon)
            }
            val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setContentIntent(contentIntent)
                .setContentTitle(PlayerActivity.musicListA[PlayerActivity.songPosition].title)
                .setContentText(PlayerActivity.musicListA[PlayerActivity.songPosition].artist)
                .addAction(NotificationCompat.Action(
                    repeatIcon, "Repeat", repeatPendingIntent)
                )
                .addAction(NotificationCompat.Action(
                    R.drawable.ic_baseline_skip_previous_24,"Previous", previousPendingIntent)
                )
                .addAction(NotificationCompat.Action(
                    playIcon, "Play", playPendingIntent)
                )
                .addAction(NotificationCompat.Action(
                    R.drawable.ic_baseline_skip_next_24, "Next", nextPendingIntent)
                )
                .addAction(NotificationCompat.Action(
                    R.drawable.ic_baseline_close_24, "Exit", exitPendingIntent)
                )
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1,2,3)
                        .setMediaSession(mediaSession.sessionToken)
                )
                .setColorized(true)
                .setLargeIcon(largeIcon)
                .setColor(ContextCompat.getColor(baseContext, R.color.mainPrimaryColor))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setSilent(true)
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
        } catch (e: Exception) {
            Toast.makeText(this, e.stackTrace.toString(), Toast.LENGTH_LONG).show()
        }


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
            CurrentPlayingFragment.binding.fragmentSongName.text = PlayerActivity.musicListA[PlayerActivity.songPosition].title
            repeatPlayControl()
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

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            PlayerActivity.isActive = false
            mediaPlayer!!.pause()
            PlayerActivity.binding.playA.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            CurrentPlayingFragment.binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            repeatPauseControl()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


}