package com.erastusnzula.emuplayer

import android.media.MediaMetadataRetriever
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class MusicFile(
    val id: String, val title: String, val album: String, val artist: String,
    val duration: Long = 0, val path: String, val artUri:String, var isCurrent: Boolean=false
)

class Playlist {
    lateinit var playlistName: String
    lateinit var playlist: ArrayList<MusicFile>
    lateinit var playlistOwner: String
    lateinit var createdOn: String
}

class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

fun formatDuration(duration: Long): String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS)-minutes*TimeUnit.SECONDS.convert(
        1 , TimeUnit.MINUTES
    ))
    return String.format("%02d:%02d", minutes,seconds)
}

fun getMusicArt(path: String): ByteArray?{
    val retriever=MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.isShuffle){
        if(!PlayerActivity.repeat){
            if (increment) {
                if (PlayerActivity.musicListA.size - 1 == PlayerActivity.songPosition) {
                    PlayerActivity.songPosition = 0

                } else {
                    ++PlayerActivity.songPosition
                }

            } else {
                if (0 == PlayerActivity.songPosition) {
                    PlayerActivity.songPosition = PlayerActivity.musicListA.size - 1
                } else {
                    --PlayerActivity.songPosition
                }

            }
        }
    }else{
        PlayerActivity.songPosition = (Math.random() * (PlayerActivity.musicListA.size-1)).toInt()
    }


}

fun repeatPauseControl(){
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
}

fun repeatPlayControl(){
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
}

fun exitProtocol(){
    if(PlayerActivity.musicService != null){
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(0)

}

fun checkIfFavourite(id:String): Int{
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteList.forEachIndexed { index, musicFile ->
        if (id == musicFile.id){
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1

}

fun checkIfFileExist(musicList: ArrayList<MusicFile>): ArrayList<MusicFile>{
    musicList.forEachIndexed { index, musicFile ->
        val file = File(musicFile.path)
        if (!file.exists()){
            musicList.removeAt(index)
        }
    }
    return musicList
}
