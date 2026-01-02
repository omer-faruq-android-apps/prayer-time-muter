package com.prayertimemuter.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.prayertimemuter.network.RetrofitClient
import com.prayertimemuter.receivers.PrayerAlarmReceiver
import com.prayertimemuter.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PrayerAlarmManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferencesManager = PreferencesManager(context)
    private val workManager = WorkManager.getInstance(context)
    private val calendarProvider: () -> Calendar = { Calendar.getInstance() }
    
    private val backoffMinutes = listOf(30L, 60L, 120L, 240L) // 30m -> 1h -> 2h -> 4h
    private val maxAttemptsPerDay = backoffMinutes.size
    
    fun schedulePrayerAlarms(locationId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            fetchAndScheduleWithRetry(locationId)
        }
    }

    suspend fun fetchAndScheduleWithRetry(locationId: Int): Boolean {
        val success = fetchAndSchedule(locationId)
        if (success) {
            preferencesManager.lastFetchFailed = false
        } else {
            preferencesManager.lastFetchFailed = true
            scheduleRetryWork()
        }
        return success
    }

    suspend fun fetchAndSchedule(locationId: Int): Boolean {
        return try {
            val response = RetrofitClient.diyanetApiService.getPrayerTimes(locationId)
            if (response.isSuccessful) {
                val prayerTimes = response.body()
                if (!prayerTimes.isNullOrEmpty()) {
                    val todayPrayerTimes = prayerTimes.first()
                    scheduleAlarmsForToday(todayPrayerTimes)
                    preferencesManager.lastPrayerTimes = todayPrayerTimes
                    resetRetryState()
                    preferencesManager.lastFetchFailed = false
                    true
                } else {
                    preferencesManager.lastFetchFailed = true
                    false
                }
            } else {
                preferencesManager.lastFetchFailed = true
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            preferencesManager.lastFetchFailed = true
            false
        }
    }
    
    private suspend fun scheduleAlarmsForToday(prayerTimes: com.prayertimemuter.models.PrayerTimes) {
        withContext(Dispatchers.Main) {
            val today = calendarProvider()
            val isFriday = today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
            
            val prayerList = prayerTimes.asList()
            
            prayerList.forEachIndexed { index, (prayerName, prayerTime) ->
                val minutesBefore = if (isFriday && prayerName == "Öğle") {
                    preferencesManager.fridayMinutesBefore
                } else preferencesManager.getMinutesBefore(prayerName)

                val minutesAfter = if (isFriday && prayerName == "Öğle") {
                    preferencesManager.fridayMinutesAfter
                } else preferencesManager.getMinutesAfter(prayerName)
                schedulePrayersWithOffsets(prayerName, prayerTime, minutesBefore, minutesAfter, index)
            }
        }
    }
    
    private fun schedulePrayersWithOffsets(
        prayerName: String,
        prayerTime: String,
        minutesBefore: Int,
        minutesAfter: Int,
        index: Int
    ) {
        // Eğer hem önce hem sonra 0 ise hiçbir alarm kurma
        if (minutesBefore <= 0 && minutesAfter <= 0) return

        val base = parsePrayerTime(prayerTime)
        val now = Calendar.getInstance()

        val startCal = base.clone() as Calendar
        if (minutesBefore > 0) startCal.add(Calendar.MINUTE, -minutesBefore)

        val endCal = base.clone() as Calendar
        if (minutesAfter > 0) endCal.add(Calendar.MINUTE, minutesAfter)

        // Başlangıç zamanı geçmişse kurulmaz; bitiş zamanı geçmişse restore kurmaya gerek yok
        if (startCal.after(now)) {
            scheduleSilentAlarm(prayerName, startCal.timeInMillis, index * 2)
        }
        if (endCal.after(now)) {
            scheduleRestoreAlarm(prayerName, endCal.timeInMillis, index * 2 + 1)
        }
    }

    private fun scheduleSilentAlarm(prayerName: String, triggerAtMillis: Long, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_SILENT
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun scheduleRestoreAlarm(prayerName: String, triggerAtMillis: Long, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_RESTORE
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
    
    private fun parsePrayerTime(prayerTime: String): Calendar {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        val timeParts = prayerTime.split(":")
        if (timeParts.size >= 2) {
            val hour = timeParts[0].toIntOrNull() ?: 0
            val minute = timeParts[1].toIntOrNull() ?: 0
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar
    }
    
    fun cancelAllAlarms() {
        val prayerNames = listOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
        
        prayerNames.forEachIndexed { index, prayerName ->
            for (i in 0..1) {
                val requestCode = index * 2 + i
                val intent = Intent(context, PrayerAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    fun scheduleRetryWork(delayMinutes: Long = 30) {
        val todayDayOfYear = calendarProvider().get(Calendar.DAY_OF_YEAR)
        if (preferencesManager.retryAttemptDay != todayDayOfYear) {
            preferencesManager.retryAttemptDay = todayDayOfYear
            preferencesManager.retryAttemptCount = 0
        }

        val attempt = preferencesManager.retryAttemptCount
        if (attempt >= maxAttemptsPerDay) {
            notifyPermanentFailure()
            return
        }

        val delay = backoffMinutes.getOrElse(attempt) { backoffMinutes.last() }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (preferencesManager.allowMobileData) NetworkType.CONNECTED else NetworkType.UNMETERED
            )
            .build()

        val request = OneTimeWorkRequestBuilder<PrayerTimeRetryWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            RETRY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )

        preferencesManager.retryAttemptCount = attempt + 1
    }

    fun cancelRetryWork() {
        workManager.cancelUniqueWork(RETRY_WORK_NAME)
    }

    private fun resetRetryState() {
        cancelRetryWork()
        preferencesManager.retryAttemptCount = 0
        preferencesManager.retryAttemptDay = calendarProvider().get(Calendar.DAY_OF_YEAR)
    }

    private fun notifyPermanentFailure() {
        createChannelIfNeeded()
        val intent = Intent(context, com.prayertimemuter.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(context.getString(com.prayertimemuter.R.string.retry_failed_title))
            .setContentText(context.getString(com.prayertimemuter.R.string.retry_failed_body))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(com.prayertimemuter.R.string.retry_failed_body)
                )
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(com.prayertimemuter.R.string.retry_failed_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val RETRY_WORK_NAME = "prayer_times_retry"
        private const val CHANNEL_ID = "retry_status"
        private const val NOTIFICATION_ID = 2002
    }
}
