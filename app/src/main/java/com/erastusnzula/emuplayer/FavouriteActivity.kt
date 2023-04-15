package com.erastusnzula.emuplayer

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.erastusnzula.emuplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var favouriteAdapter: FavouriteAdapter

    companion object{
        var favouriteList: ArrayList<MusicFile> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        title = "Favourite Songs"
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        favouriteList = checkIfFileExist(favouriteList)
        binding.favouriteRecycler.setHasFixedSize(true)
        binding.favouriteRecycler.setItemViewCacheSize(15)
        binding.favouriteRecycler.layoutManager = LinearLayoutManager(this@FavouriteActivity)
        favouriteAdapter = FavouriteAdapter(this@FavouriteActivity, favouriteList)
        binding.favouriteRecycler.adapter = favouriteAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->finish()
        }
        return super.onOptionsItemSelected(item)
    }
}