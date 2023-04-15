package com.erastusnzula.emuplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erastusnzula.emuplayer.databinding.FavouriteViewBinding

class FavouriteAdapter(private val context: Context, private var musicList: ArrayList<MusicFile>): RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {
    inner class ViewHolder(binding:FavouriteViewBinding):RecyclerView.ViewHolder(binding.root){
        val title = binding.songNameF
        val albumName = binding.albumNameF
        val albumPhoto = binding.albumImageF
        val songDuration = binding.songDurationF
        val moreOptions = binding.moreOptionsF
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FavouriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.title.text = musicList[position].title
        holder.albumName.text = musicList[position].album.capitalize()
        holder.songDuration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.player_icon).centerCrop())
            .into(holder.albumPhoto)
        holder.root.setOnClickListener {
            when{
                MainActivity.isInSearch ->{
                    sendIntents(reference = "MusicAdapterSearch", filePosition = position)
                }
                musicList[position].id == PlayerActivity.currentPlayingID->{
                    sendIntents(reference = "CurrentPlaying", filePosition = PlayerActivity.songPosition)
//                    holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.activePageColor))
                }
                else ->{
                    sendIntents(reference = "FavouriteAdapter", filePosition=position)
                }
            }
        }
        holder.moreOptions.setOnClickListener {
            ContextCompat.startActivity(context,Intent(context, PlaylistActivity::class.java), null)
        }
    }



    override fun getItemCount(): Int {
        return musicList.size
    }

    private fun sendIntents(reference:String,filePosition: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", filePosition)
        intent.putExtra("class", reference)
        ContextCompat.startActivity(context, intent, null)
    }
}