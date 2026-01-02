package com.prayertimemuter.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class NotificationUtils(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_ID = "prayer_times_channel"
        const val CHANNEL_NAME = "Namaz Vakitleri"
        const val CHANNEL_DESCRIPTION = "Namaz vakitleri bildirimleri"
    }
    
    fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
