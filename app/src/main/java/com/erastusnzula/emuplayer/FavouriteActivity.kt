package com.erastusnzula.emuplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FavouriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUPlayer)
        setContentView(R.layout.activity_favourite)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}