package com.erastusnzula.emuplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.erastusnzula.emuplayer.databinding.ActivityPlaylistBinding
import com.erastusnzula.emuplayer.databinding.AddPlaylistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var playlistAdapter: PlaylistAdapter

    companion object {
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        title = "Playlists"
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.playlistRecycler.setHasFixedSize(true)
        binding.playlistRecycler.setItemViewCacheSize(15)
        binding.playlistRecycler.layoutManager = GridLayoutManager(this@PlaylistActivity, 2)
        playlistAdapter = PlaylistAdapter(this@PlaylistActivity, playlist = musicPlaylist.ref)
        binding.playlistRecycler.adapter = playlistAdapter
        binding.addPlaylistButton.setOnClickListener { addPlaylistDialog() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addPlaylistDialog() {
        val dialog = LayoutInflater.from(this@PlaylistActivity)
            .inflate(R.layout.add_playlist, binding.root, false)
        val binder = AddPlaylistBinding.bind(dialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(dialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD") { d, _ ->
                val playlistName = binder.playlistName.text
                val owner = binder.owner.text
                if (playlistName != null && owner != null) {
                    if (playlistName.isNotEmpty() && owner.isNotEmpty()) {
                        createPlaylist(playlistName.toString(), owner.toString())
                    }
                }
                d.dismiss()
            }
            .setNegativeButton("CANCEL") { ds, _ ->
                ds.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun createPlaylist(playlistName: String, owner: String) {

        var playlistExist = false
        for (i in musicPlaylist.ref) {
            if (playlistName.equals(i.playlistName)) {
                playlistExist = true
                break
            }
        }
        if (playlistExist) {
            Toast.makeText(this, "Playlist exists", Toast.LENGTH_LONG).show()
        } else {
            val playlistTemplate = Playlist()
            playlistTemplate.playlistName = playlistName
            playlistTemplate.playlist = ArrayList()
            playlistTemplate.playlistOwner = owner
            val calender = java.util.Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MM yyyy", Locale.ENGLISH)
            playlistTemplate.createdOn = sdf.format(calender)
            musicPlaylist.ref.add(playlistTemplate)
            playlistAdapter.refreshPlaylist()
        }
    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }
}

