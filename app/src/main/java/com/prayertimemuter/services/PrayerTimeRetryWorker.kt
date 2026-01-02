package com.prayertimemuter.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prayertimemuter.utils.PreferencesManager

class PrayerTimeRetryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)
        val city = prefs.selectedCity ?: return Result.failure()

        val manager = PrayerAlarmManager(applicationContext)
        val success = manager.fetchAndSchedule(city.id)
        return if (success) Result.success() else Result.retry()
    }
}
