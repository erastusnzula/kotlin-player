package com.erastusnzula.emuplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> playNextOrPrevious(increment = false, context = context!!)
            ApplicationClass.PLAY -> if (PlayerActivity.isActive) pause() else play()
            ApplicationClass.NEXT -> playNextOrPrevious(increment = true, context = context!!)
            ApplicationClass.REPEAT -> repeatMode(context)
            ApplicationClass.EXIT -> {
                PlayerActivity.musicService!!.stopForeground(true)
                PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                PlayerActivity.musicService = null
                exitProcess(0)
            }

        }
    }

    private fun repeatMode(context: Context?) {
        if (!PlayerActivity.repeat) {
            PlayerActivity.repeat = true
            PlayerActivity.isShuffle = false
            PlayerActivity.binding.shuffleA.setColorFilter(
                ContextCompat.getColor(
                    context!!,
                    R.color.mainPrimaryColor
                )
            )
            if (PlayerActivity.isActive) {
                PlayerActivity.musicService!!.displayNotification(
                    R.drawable.ic_baseline_pause_24,
                    R.drawable.ic_baseline_repeat_one_24
                )
            } else {
                PlayerActivity.musicService!!.displayNotification(
                    R.drawable.ic_baseline_play_circle_filled_24,
                    R.drawable.ic_baseline_repeat_one_24
                )
            }
            PlayerActivity.binding.repeatA.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            PlayerActivity.binding.repeatA.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.activePageColor
                )
            )
        } else {
            PlayerActivity.repeat = false
            if (PlayerActivity.isActive) {
                PlayerActivity.musicService!!.displayNotification(
                    R.drawable.ic_baseline_pause_24,
                    R.drawable.ic_baseline_repeat_24
                )
            } else {
                PlayerActivity.musicService!!.displayNotification(
                    R.drawable.ic_baseline_play_circle_filled_24,
                    R.drawable.ic_baseline_repeat_24
                )
            }
            PlayerActivity.binding.repeatA.setImageResource(R.drawable.ic_baseline_repeat_24)
            PlayerActivity.binding.repeatA.setColorFilter(
                ContextCompat.getColor(
                    context!!,
                    R.color.mainPrimaryColor
                )
            )
        }
    }

    private fun play() {
        PlayerActivity.isActive = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
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
        PlayerActivity.binding.playA.setImageResource(R.drawable.ic_baseline_pause_24)

    }

    private fun pause() {
        PlayerActivity.isActive = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        CurrentPlayingFragment.binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        if (PlayerActivity.repeat) {
            PlayerActivity.musicService!!.displayNotification(
                R.drawable.ic_baseline_play_circle_filled_24,
                R.drawable.ic_baseline_repeat_one_24
            )
        } else {
            PlayerActivity.musicService!!.displayNotification(
                R.drawable.ic_baseline_play_circle_filled_24,
                R.drawable.ic_baseline_repeat_24
            )
        }
        PlayerActivity.binding.playA.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
    }

    private fun playNextOrPrevious(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_baseline_music_splash).centerCrop())
            .into(PlayerActivity.binding.currentAlbumImage)
        PlayerActivity.binding.currentSongA.text =
            PlayerActivity.musicListA[PlayerActivity.songPosition].title
        PlayerActivity.binding.albumNameA.text =
            PlayerActivity.musicListA[PlayerActivity.songPosition].album


    }
}