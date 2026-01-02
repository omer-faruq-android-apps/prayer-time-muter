package com.prayertimemuter.utils

import android.content.Context
import android.media.AudioManager

class AudioUtils(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    companion object {
        private const val PREF_NAME = "audio_settings"
        private const val KEY_PREVIOUS_RINGER_MODE = "previous_ringer_mode"
        private const val KEY_PREVIOUS_RING_VOLUME = "previous_ring_volume"
        private const val KEY_PREVIOUS_MEDIA_VOLUME = "previous_media_volume"
        private const val KEY_PREVIOUS_ALARM_VOLUME = "previous_alarm_volume"
    }
    
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    fun saveCurrentAudioState() {
        prefs.edit().apply {
            putInt(KEY_PREVIOUS_RINGER_MODE, audioManager.ringerMode)
            putInt(KEY_PREVIOUS_RING_VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_RING))
            putInt(KEY_PREVIOUS_MEDIA_VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
            putInt(KEY_PREVIOUS_ALARM_VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_ALARM))
            apply()
        }
    }
    
    fun silentDevice(mode: SilentMode = SilentMode.SILENT) {
        saveCurrentAudioState()
        audioManager.ringerMode = when (mode) {
            SilentMode.SILENT -> AudioManager.RINGER_MODE_SILENT
            SilentMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
        }
    }
    
    fun restoreAudioState() {
        val previousRingerMode = prefs.getInt(KEY_PREVIOUS_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
        val previousRingVolume = prefs.getInt(KEY_PREVIOUS_RING_VOLUME, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 2)
        val previousMediaVolume = prefs.getInt(KEY_PREVIOUS_MEDIA_VOLUME, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2)
        val previousAlarmVolume = prefs.getInt(KEY_PREVIOUS_ALARM_VOLUME, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM))
        
        audioManager.ringerMode = previousRingerMode
        audioManager.setStreamVolume(AudioManager.STREAM_RING, previousRingVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousMediaVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousAlarmVolume, 0)
    }
    
    fun isDeviceSilent(): Boolean {
        return audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT || 
               audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }
}
