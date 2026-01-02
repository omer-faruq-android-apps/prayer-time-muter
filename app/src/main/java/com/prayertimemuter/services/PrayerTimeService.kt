package com.prayertimemuter.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.prayertimemuter.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrayerTimeService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var prayerAlarmManager: PrayerAlarmManager
    
    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        prayerAlarmManager = PrayerAlarmManager(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
        }
        return START_NOT_STICKY
    }
    
    private fun startService() {
        serviceScope.launch {
            // Önce eski alarmları iptal et
            prayerAlarmManager.cancelAllAlarms()
            schedulePrayerAlarms()
            stopSelf()
        }
    }
    
    private fun stopService() {
        prayerAlarmManager.cancelAllAlarms()
        stopSelf()
    }
    
    private suspend fun schedulePrayerAlarms() {
        withContext(Dispatchers.IO) {
            val selectedCity = preferencesManager.selectedCity
            if (selectedCity != null) {
                prayerAlarmManager.schedulePrayerAlarms(selectedCity.id)
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        prayerAlarmManager.cancelAllAlarms()
    }
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
