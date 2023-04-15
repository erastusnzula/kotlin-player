package com.erastusnzula.emuplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erastusnzula.emuplayer.databinding.SongViewBinding

class MusicAdapter(
    private val context: Context,
    private var musicList: ArrayList<MusicFile>,
    private var playlistDetailsB: Boolean = false,
    private var selection: Boolean = false
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {
    inner class ViewHolder(binding: SongViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songName
        val albumName = binding.albumName
        val albumPhoto = binding.albumImage
        val songDuration = binding.songDuration
        val moreOptions = binding.moreOptions
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SongViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.title.text = musicList[position].title
        holder.albumName.text = musicList[position].album.capitalize()
        holder.songDuration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.player_icon).centerCrop())
            .into(holder.albumPhoto)
        when {
            playlistDetailsB -> {
                holder.root.setOnClickListener {
                    when (musicList[position].id) {
                        PlayerActivity.currentPlayingID -> {
                            sendIntents(
                                reference = "CurrentPlaying",
                                filePosition = PlayerActivity.songPosition
                            )
                //                    holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.activePageColor))
                        }
                        else -> {
                            sendIntents(reference = "PlaylistDetailsAdapter", filePosition = position)
                        }
                    }

                }
            }
            selection -> {
                holder.root.setOnClickListener {
                    if (addSong(musicList[position])){
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.mainPrimaryColor))
                    }else{
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
            }
            else -> {
                holder.root.setOnClickListener {
                    when {
                        MainActivity.isInSearch -> {
                            sendIntents(reference = "MusicAdapterSearch", filePosition = position)
                        }
                        musicList[position].id == PlayerActivity.currentPlayingID -> {
                            sendIntents(
                                reference = "CurrentPlaying",
                                filePosition = PlayerActivity.songPosition
                            )
//                    holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.activePageColor))
                        }
                        else -> {
                            sendIntents(reference = "MusicAdapter", filePosition = position)
                        }
                    }
                }
                holder.moreOptions.setOnClickListener {
                    ContextCompat.startActivity(
                        context,
                        Intent(context, DisplayActivity::class.java),
                        null
                    )
                }
            }
        }

    }

    private fun addSong(song: MusicFile): Boolean {
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist.forEachIndexed { index, musicFile ->
            if (song.id == musicFile.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist.add(song)
        return true
    }


    override fun getItemCount(): Int {
        return musicList.size
    }

    fun updateMusicList(musicListSearch: ArrayList<MusicFile>) {
        musicList = ArrayList()
        musicList.addAll(musicListSearch)
        notifyDataSetChanged()
    }

    private fun sendIntents(reference: String, filePosition: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", filePosition)
        intent.putExtra("class", reference)
        ContextCompat.startActivity(context, intent, null)
    }

    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist
        notifyDataSetChanged()
    }
}