package com.prayertimemuter.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.prayertimemuter.R
import com.prayertimemuter.services.PrayerTimeService
import com.prayertimemuter.services.PrayerAlarmManager
import com.prayertimemuter.utils.AudioUtils
import com.prayertimemuter.utils.NotificationUtils
import com.prayertimemuter.utils.PreferencesManager

class PrayerAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        
        when (action) {
            ACTION_SILENT -> handleSilentAction(context, prayerName)
            ACTION_RESTORE -> handleRestoreAction(context, prayerName)
        }
        
        scheduleNextDayAlarms(context)
    }
    
    private fun handleSilentAction(context: Context, prayerName: String) {
        val audioUtils = AudioUtils(context)
        val prefs = PreferencesManager(context)
        audioUtils.silentDevice(prefs.silentMode)
        
    }
    
    private fun handleRestoreAction(context: Context, prayerName: String) {
        val audioUtils = AudioUtils(context)
        audioUtils.restoreAudioState()
    }
    
    private fun scheduleNextDayAlarms(context: Context) {
        val preferencesManager = PreferencesManager(context)
        val selectedCity = preferencesManager.selectedCity
        
        if (selectedCity != null && preferencesManager.isServiceEnabled) {
            val prayerAlarmManager = PrayerAlarmManager(context)
            prayerAlarmManager.schedulePrayerAlarms(selectedCity.id)
        }
    }
    
    companion object {
        const val ACTION_SILENT = "ACTION_SILENT"
        const val ACTION_RESTORE = "ACTION_RESTORE"
        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
    }
}
