package com.erastusnzula.emuplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class BecomingNoisy: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY){
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
    }
}