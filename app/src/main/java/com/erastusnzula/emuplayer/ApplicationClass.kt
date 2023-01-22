package com.erastusnzula.emuplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class ApplicationClass: Application() {
    companion object{
        const val CHANNEL_ID = "EMU"
        const val REPEAT = "Repeat"
        const val PLAY = "Play"
        const val PREVIOUS = "Previous"
        const val NEXT = "Next"
        const val EXIT = "Exit"
    }
    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannel(CHANNEL_ID, "Current Playing.",NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = "Shows the current playing song."
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
}