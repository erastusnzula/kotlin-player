package com.erastusnzula.emuplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PlaylistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        setContentView(R.layout.activity_playlist)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}