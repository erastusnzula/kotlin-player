package com.erastusnzula.emuplayer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erastusnzula.emuplayer.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {
    lateinit var binding: ActivityPlaylistDetailsBinding
    lateinit var adapter: MusicAdapter
    companion object{
        var currentPlaylistPosition: Int = -1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        title="Playlist Details"
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        currentPlaylistPosition = intent.extras?.get("index") as Int
        binding.recyclerViewPD.setItemViewCacheSize(10)
        binding.recyclerViewPD.setHasFixedSize(true)
        binding.recyclerViewPD.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist, playlistDetailsB = true)
        binding.recyclerViewPD.adapter = adapter
        binding.addPD.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }

        binding.removePD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
                .setTitle("Remove songs")
                .setMessage("Do you want to remove all songs from playlist?")
                .setPositiveButton("Yes"){dialog,_, ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.playlistNamePD.text = "Playlist: ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlistName}"
        binding.totalSongsPD.text = "Total songs: ${adapter.itemCount.toString()}"
        binding.ownerPD.text = "Owner: ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlistOwner}"
        binding.createdOnPD.text= "Created on: ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createdOn}"
        if (adapter.itemCount > 0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.player_icon).centerCrop())
                .into(binding.imageViewPD)
        }
        adapter.notifyDataSetChanged()
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPl = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("PlaylistSongs", jsonStringPl)
        editor.apply()
    }
}