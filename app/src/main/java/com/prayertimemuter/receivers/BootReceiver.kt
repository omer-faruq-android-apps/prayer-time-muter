package com.prayertimemuter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prayertimemuter.services.PrayerTimeService
import com.prayertimemuter.utils.PreferencesManager

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val preferencesManager = PreferencesManager(context)
            
            if (preferencesManager.isServiceEnabled && preferencesManager.selectedCity != null) {
                val serviceIntent = Intent(context, PrayerTimeService::class.java).apply {
                    action = PrayerTimeService.ACTION_START
                }
                // Foreground notification kaldırıldığı için normal startService kullan
                context.startService(serviceIntent)
            }
        }
    }
}
