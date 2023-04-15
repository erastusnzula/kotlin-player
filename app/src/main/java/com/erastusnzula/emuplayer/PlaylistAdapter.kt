package com.erastusnzula.emuplayer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erastusnzula.emuplayer.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistAdapter(private val context: Context, private var playlist: ArrayList<Playlist>): RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    inner class ViewHolder(binding:PlaylistViewBinding):RecyclerView.ViewHolder(binding.root){

        val albumPhoto = binding.albumImageP
        val playlistName= binding.playlistName
        val playlistDelete = binding.playlistDelete
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.playlistName.text = playlist[position].playlistName
        holder.playlistName.isSelected = true
        holder.playlistDelete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
                .setTitle(playlist[position].playlistName)
                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("Yes"){dialog,_, ->
                    PlaylistActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){ dialog,_, ->
                    dialog.dismiss()
                }
                .setCancelable(false)
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
        if (PlaylistActivity.musicPlaylist.ref[position].playlist.size > 0){
            Glide.with(context)
                .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.player_icon).centerCrop())
                .into(holder.albumPhoto)
        }
//        Glide.with(context)
//            .load(musicList[position].artUri)
//            .apply(RequestOptions().placeholder(R.drawable.player_icon).centerCrop())
//            .into(holder.albumPhoto)
    }



    override fun getItemCount(): Int {
        return playlist.size
    }

   fun refreshPlaylist(){
        playlist = ArrayList()
        playlist.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }


}