package com.erastusnzula.emuplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.erastusnzula.emuplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectionBinding
    private lateinit var adapter: MusicAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerSel.setItemViewCacheSize(10)
        binding.recyclerSel.setHasFixedSize(true)
        binding.recyclerSel.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, MainActivity.musicList, selection = true)
        binding.recyclerSel.adapter = adapter
    }
}