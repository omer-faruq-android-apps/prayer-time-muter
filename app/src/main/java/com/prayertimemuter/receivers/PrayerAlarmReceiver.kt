package com.prayertimemuter.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.prayertimemuter.R
import com.prayertimemuter.services.MuteForegroundService
import com.prayertimemuter.services.PrayerTimeService
import com.prayertimemuter.services.PrayerAlarmManager
import com.prayertimemuter.utils.AudioUtils
import com.prayertimemuter.utils.NotificationUtils
import com.prayertimemuter.utils.PreferencesManager
import com.prayertimemuter.utils.LogUtils

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
        // Start short-lived foreground to survive OEM kills during mute window
        val fgIntent = Intent(context, MuteForegroundService::class.java).apply {
            action = MuteForegroundService.ACTION_START
        }
        context.startForegroundServiceCompat(fgIntent)

        val audioUtils = AudioUtils(context)
        val prefs = PreferencesManager(context)
        audioUtils.silentDevice(prefs.silentMode)
        LogUtils.append(context, "Muted for $prayerName mode=${prefs.silentMode}")
    }
    
    private fun handleRestoreAction(context: Context, prayerName: String) {
        val audioUtils = AudioUtils(context)
        audioUtils.restoreAudioState()
        LogUtils.append(context, "Restored audio after $prayerName")

        // Stop foreground when restore finishes
        val stopIntent = Intent(context, MuteForegroundService::class.java).apply {
            action = MuteForegroundService.ACTION_STOP
        }
        context.startForegroundServiceCompat(stopIntent)
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

// Small helper to call startForegroundService only on O+; startService otherwise
private fun Context.startForegroundServiceCompat(intent: Intent) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}
