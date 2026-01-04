package com.prayertimemuter.utils

import android.content.Context
import android.content.SharedPreferences
import com.prayertimemuter.models.City

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "prayer_time_muter_prefs"
        private const val KEY_SELECTED_CITY_ID = "selected_city_id"
        private const val KEY_SELECTED_COUNTRY = "selected_country"
        private const val KEY_SELECTED_CITY = "selected_city"
        private const val KEY_SELECTED_REGION = "selected_region"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_FAJR_BEFORE = "fajr_before"
        private const val KEY_FAJR_AFTER = "fajr_after"
        private const val KEY_SUN_BEFORE = "sun_before"
        private const val KEY_SUN_AFTER = "sun_after"
        private const val KEY_DHUHR_BEFORE = "dhuhr_before"
        private const val KEY_DHUHR_AFTER = "dhuhr_after"
        private const val KEY_ASR_BEFORE = "asr_before"
        private const val KEY_ASR_AFTER = "asr_after"
        private const val KEY_MAGHRIB_BEFORE = "maghrib_before"
        private const val KEY_MAGHRIB_AFTER = "maghrib_after"
        private const val KEY_ISHA_BEFORE = "isha_before"
        private const val KEY_ISHA_AFTER = "isha_after"
        private const val KEY_FRIDAY_BEFORE = "friday_before"
        private const val KEY_FRIDAY_AFTER = "friday_after"
        private const val KEY_SILENT_MODE = "silent_mode"
        private const val KEY_ALLOW_MOBILE_DATA = "allow_mobile_data"
        private const val KEY_RETRY_ATTEMPT_COUNT = "retry_attempt_count"
        private const val KEY_RETRY_ATTEMPT_DAY = "retry_attempt_day"
        private const val KEY_LAST_PRAYER_TIMES = "last_prayer_times"
        private const val KEY_PERMISSIONS_GRANTED = "permissions_granted"
        private const val KEY_LAST_FETCH_FAILED = "last_fetch_failed"
        private const val KEY_LOGGING_ENABLED = "logging_enabled"
        
        private const val DEFAULT_MINUTES_BEFORE = 5
        private const val DEFAULT_MINUTES_AFTER = 30
        private const val DEFAULT_FRIDAY_BEFORE = 10
        private const val DEFAULT_FRIDAY_AFTER = 45
        private val DEFAULT_SILENT_MODE = SilentMode.SILENT.name
        private const val DEFAULT_ALLOW_MOBILE_DATA = true
        private const val DEFAULT_RETRY_COUNT = 0
        private const val DEFAULT_RETRY_DAY = -1
        private const val DEFAULT_LAST_FETCH_FAILED = false
        private const val DEFAULT_SUN_BEFORE = 30
        private const val DEFAULT_SUN_AFTER = 0
    }
    
    var selectedCity: City?
        get() {
            val id = prefs.getInt(KEY_SELECTED_CITY_ID, -1)
            val country = prefs.getString(KEY_SELECTED_COUNTRY, null)
            val city = prefs.getString(KEY_SELECTED_CITY, null)
            val region = prefs.getString(KEY_SELECTED_REGION, null)
            return if (id != -1 && country != null && city != null && region != null) {
                City(id, country, city, region)
            } else null
        }
        set(city) {
            val editor = prefs.edit()
            if (city != null) {
                editor.putInt(KEY_SELECTED_CITY_ID, city.id)
                editor.putString(KEY_SELECTED_COUNTRY, city.country)
                editor.putString(KEY_SELECTED_CITY, city.city)
                editor.putString(KEY_SELECTED_REGION, city.region)
            } else {
                editor.remove(KEY_SELECTED_CITY_ID)
                editor.remove(KEY_SELECTED_COUNTRY)
                editor.remove(KEY_SELECTED_CITY)
                editor.remove(KEY_SELECTED_REGION)
            }
            editor.apply()
        }
    
    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()
    
    var arePermissionsGranted: Boolean
        get() = prefs.getBoolean(KEY_PERMISSIONS_GRANTED, false)
        set(value) = prefs.edit().putBoolean(KEY_PERMISSIONS_GRANTED, value).apply()
    
    fun getMinutesBefore(prayerName: String): Int {
        val key = "${prayerName.lowercase()}_before"
        val default = if (prayerName.equals("Güneş", ignoreCase = true) || prayerName.equals("Sun", ignoreCase = true)) {
            DEFAULT_SUN_BEFORE
        } else DEFAULT_MINUTES_BEFORE
        return prefs.getInt(key, default)
    }
    
    fun setMinutesBefore(prayerName: String, minutes: Int) {
        prefs.edit().putInt("${prayerName.lowercase()}_before", minutes).apply()
    }
    
    fun getMinutesAfter(prayerName: String): Int {
        val key = "${prayerName.lowercase()}_after"
        val default = if (prayerName.equals("Güneş", ignoreCase = true) || prayerName.equals("Sun", ignoreCase = true)) {
            DEFAULT_SUN_AFTER
        } else DEFAULT_MINUTES_AFTER
        return prefs.getInt(key, default)
    }
    
    fun setMinutesAfter(prayerName: String, minutes: Int) {
        prefs.edit().putInt("${prayerName.lowercase()}_after", minutes).apply()
    }

    var fridayMinutesBefore: Int
        get() = prefs.getInt(KEY_FRIDAY_BEFORE, DEFAULT_FRIDAY_BEFORE)
        set(value) = prefs.edit().putInt(KEY_FRIDAY_BEFORE, value).apply()

    var fridayMinutesAfter: Int
        get() = prefs.getInt(KEY_FRIDAY_AFTER, DEFAULT_FRIDAY_AFTER)
        set(value) = prefs.edit().putInt(KEY_FRIDAY_AFTER, value).apply()

    var silentMode: SilentMode
        get() = SilentMode.valueOf(prefs.getString(KEY_SILENT_MODE, DEFAULT_SILENT_MODE) ?: DEFAULT_SILENT_MODE)
        set(value) = prefs.edit().putString(KEY_SILENT_MODE, value.name).apply()

    var allowMobileData: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_MOBILE_DATA, DEFAULT_ALLOW_MOBILE_DATA)
        set(value) = prefs.edit().putBoolean(KEY_ALLOW_MOBILE_DATA, value).apply()

    var retryAttemptCount: Int
        get() = prefs.getInt(KEY_RETRY_ATTEMPT_COUNT, DEFAULT_RETRY_COUNT)
        set(value) = prefs.edit().putInt(KEY_RETRY_ATTEMPT_COUNT, value).apply()

    var retryAttemptDay: Int
        get() = prefs.getInt(KEY_RETRY_ATTEMPT_DAY, DEFAULT_RETRY_DAY)
        set(value) = prefs.edit().putInt(KEY_RETRY_ATTEMPT_DAY, value).apply()

    var lastPrayerTimes: com.prayertimemuter.models.PrayerTimes?
        get() {
            val stored = prefs.getString(KEY_LAST_PRAYER_TIMES, null) ?: return null
            val parts = stored.split("|")
            return if (parts.size == 7) {
                com.prayertimemuter.models.PrayerTimes(
                    date = parts[0],
                    fajr = parts[1],
                    sun = parts[2],
                    dhuhr = parts[3],
                    asr = parts[4],
                    maghrib = parts[5],
                    isha = parts[6]
                )
            } else null
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_LAST_PRAYER_TIMES).apply()
            } else {
                val serialized = listOf(
                    value.date,
                    value.fajr,
                    value.sun,
                    value.dhuhr,
                    value.asr,
                    value.maghrib,
                    value.isha
                ).joinToString("|")
                prefs.edit().putString(KEY_LAST_PRAYER_TIMES, serialized).apply()
            }
        }

    var lastFetchFailed: Boolean
        get() = prefs.getBoolean(KEY_LAST_FETCH_FAILED, DEFAULT_LAST_FETCH_FAILED)
        set(value) = prefs.edit().putBoolean(KEY_LAST_FETCH_FAILED, value).apply()

    var loggingEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOGGING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_LOGGING_ENABLED, value).apply()
    
    fun getAllPrayerSettings(): Map<String, Pair<Int, Int>> {
        val prayers = listOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
        return prayers.associate { prayer ->
            prayer to Pair(getMinutesBefore(prayer), getMinutesAfter(prayer))
        }
    }
}
