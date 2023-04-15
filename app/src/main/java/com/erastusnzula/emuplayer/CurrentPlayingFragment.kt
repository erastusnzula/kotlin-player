package com.erastusnzula.emuplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erastusnzula.emuplayer.databinding.FragmentCurrentPlayingBinding


class CurrentPlayingFragment : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentCurrentPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_playing, container, false)
        binding = FragmentCurrentPlayingBinding.bind(view)
        binding.root.visibility = View.GONE
        binding.fragmentSongName.isSelected = true
        binding.fragmentPlay.setOnClickListener {
            if (PlayerActivity.isActive) pause() else play()
        }
        binding.fragmentNext.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()
            Glide.with(this)
                .load(PlayerActivity.musicListA[PlayerActivity.songPosition].artUri)
                .apply(
                    RequestOptions().placeholder(R.drawable.ic_baseline_music_splash).centerCrop()
                )
                .into(PlayerActivity.binding.currentAlbumImage)
            PlayerActivity.binding.currentSongA.text =
                PlayerActivity.musicListA[PlayerActivity.songPosition].title
            PlayerActivity.binding.albumNameA.text =
                PlayerActivity.musicListA[PlayerActivity.songPosition].album
            binding.fragmentSongName.text =
                PlayerActivity.musicListA[PlayerActivity.songPosition].title

        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "CurrentPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            Glide.with(this)
                .load(PlayerActivity.musicListA[PlayerActivity.songPosition].artUri)
                .apply(
                    RequestOptions().placeholder(R.drawable.ic_baseline_music_splash).centerCrop()
                )
                .into(binding.fragmentAlbumImage)
            binding.fragmentSongName.text =
                PlayerActivity.musicListA[PlayerActivity.songPosition].title
            if (PlayerActivity.isActive) {
                binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            }
        }
    }

    private fun play() {
        binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_pause_24)
        PlayerActivity.isActive = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
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
        binding.fragmentPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        PlayerActivity.isActive = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        repeatPauseControl()
        PlayerActivity.binding.playA.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)

    }

}