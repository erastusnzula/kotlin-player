package com.erastusnzula.emuplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.erastusnzula.emuplayer.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var allSongsButton: ImageView
    private lateinit var favouriteButton: ImageView
    private lateinit var playlistButton: ImageView
//    private lateinit var settingsButton: ImageView
//    private lateinit var shuffleButton: ImageView
    private val audioRequestCode = 1


    companion object {
        lateinit var musicList: ArrayList<MusicFile>
        lateinit var musicListSearch: ArrayList<MusicFile>
        var isInSearch = false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        title = "Songs"
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggle = ActionBarDrawerToggle(this, binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (checkForPermissions()){
                playerInitialization()
                FavouriteActivity.favouriteList = ArrayList()
                val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
                val jsonString = editor.getString("FavouriteSongs", null)
                val tokenType = object : TypeToken<ArrayList<MusicFile>>(){}.type
                if (jsonString != null){
                    val data: ArrayList<MusicFile> = GsonBuilder().create().fromJson(jsonString, tokenType)
                    FavouriteActivity.favouriteList.addAll(data)
                }
            }
        }else{
            if (checkForPermissionsLower()) {
                playerInitialization()
                FavouriteActivity.favouriteList = ArrayList()
                val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
                val jsonString = editor.getString("FavouriteSongs", null)
                val tokenType = object : TypeToken<ArrayList<MusicFile>>(){}.type
                if (jsonString != null){
                    val data: ArrayList<MusicFile> = GsonBuilder().create().fromJson(jsonString, tokenType)
                    FavouriteActivity.favouriteList.addAll(data)
                }

                PlaylistActivity.musicPlaylist = MusicPlaylist()
                val jsonStringPl = editor.getString("PlaylistSongs", null)
                if (jsonStringPl != null){
                    val dataPl: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPl, MusicPlaylist::class.java)
                    PlaylistActivity.musicPlaylist = dataPl
                }
            }
        }

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navAbout->startActivity(Intent(this, AboutActivity::class.java))
                R.id.navDisplay -> startActivity(Intent(this, DisplayActivity::class.java))
                R.id.navDownload->startActivity(Intent(this, DownloadActivity::class.java))
                R.id.navSettings->startActivity(Intent(this, SettingsActivity::class.java))
            }
            true
        }
        bindButtons()
        allSongsButtonClick()
        playlistButtonClick()
        favouriteButtonClick()
//        settingsButtonClick()
//        shuffleButtonClick()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == audioRequestCode) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                permissionsAndroid13(requestCode, grantResults)
            }else{
                permissionsAndroidLower(requestCode, grantResults)
            }

        }

    }



    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isActive && PlayerActivity.musicService != null) {
            exitProtocol()
        }
    }

    override fun onResume() {
        super.onResume()
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteList)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPl = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("PlaylistSongs", jsonStringPl)
        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_bar_view, menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (file in musicList) {
                        if (file.title.lowercase().contains(userInput)) {
                            musicListSearch.add(file)
                        }
                    }
                    isInSearch = true
                    musicAdapter.updateMusicList(musicListSearch = musicListSearch)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun bindButtons() {
        allSongsButton = binding.allSongsButton
        favouriteButton = binding.favouritesButton
        playlistButton = binding.playlistButton
//        settingsButton = binding.settingsButton
//        shuffleButton = binding.shuffleButton
//        allSongsButton.setColorFilter(
//            ActivityCompat.getColor(
//                this@MainActivity,
//                R.color.activePageColor
//            )
//        )

    }

    private fun allSongsButtonClick() {
        allSongsButton.setOnClickListener {
//            binding.allSongsButton.setColorFilter(
//                ActivityCompat.getColor(
//                    this@MainActivity,
//                    R.color.activePageColor
//                )
//            )
            playlistButton.setColorFilter(
                ActivityCompat.getColor(
                    this@MainActivity,
                    R.color.mainPrimaryColor
                )
            )
        }
    }

    private fun playlistButtonClick() {
        playlistButton.setOnClickListener {
            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(intent)
        }
    }

//    private fun settingsButtonClick() {
//        settingsButton.setOnClickListener {
//            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
//            startActivity(intent)
//        }
//
//    }

    private fun favouriteButtonClick() {
        favouriteButton.setOnClickListener {
            val intent = Intent(this@MainActivity, FavouriteActivity::class.java)
            startActivity(intent)
        }

    }

//    private fun shuffleButtonClick() {
//        shuffleButton.setOnClickListener {
//            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
//            intent.putExtra("index", 0)
//            intent.putExtra("class", "MainActivity")
//            startActivity(intent)
//        }
//
//    }

    private fun playerInitialization() {
        isInSearch = false
        musicList = getMusicFiles()
        binding.musicRecyclerView.setHasFixedSize(true)
        binding.musicRecyclerView.setItemViewCacheSize(15)
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, musicList)
        binding.musicRecyclerView.adapter = musicAdapter
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun permissionsAndroid13(requestCode: Int, grantResults: IntArray) {
        if (requestCode == audioRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                playerInitialization()
                Toast.makeText(this@MainActivity, "permission granted", Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ), audioRequestCode
                )
            }

        }
    }

    private fun permissionsAndroidLower(requestCode: Int, grantResults: IntArray) {
        if (requestCode == audioRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                playerInitialization()
                Toast.makeText(this@MainActivity, "permission granted", Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), audioRequestCode
                )
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkForPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES
                ), audioRequestCode
            )
            return false
        }
        return true
    }

    private fun checkForPermissionsLower(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE

                ), audioRequestCode
            )
            return false
        }
        return true
    }

    private fun getMusicFiles(): ArrayList<MusicFile> {
        val musicFiles = ArrayList<MusicFile>()
        val filesSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this@MainActivity.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, filesSelection, null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC", null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val id =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val album =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val artist =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val pathCurrent =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val albumId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val uriArt = Uri.withAppendedPath(uri, albumId).toString()

                    val music = MusicFile(
                        id = id,
                        title = title,
                        album = album,
                        artist = artist,
                        duration = duration,
                        path = pathCurrent,
                        artUri = uriArt

                    )

                    val musicFile = File(music.path)
                    if (musicFile.exists()) {
                        musicFiles.add(music)
                    }

                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return musicFiles
    }
}