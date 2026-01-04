package com.prayertimemuter.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogUtils {
    private const val FILE_NAME = "prayer_events.log"
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun append(context: Context, message: String) {
        val prefs = PreferencesManager(context)
        if (!prefs.loggingEnabled) return
        runCatching {
            val file = File(context.filesDir, FILE_NAME)
            val ts = sdf.format(Date())
            file.appendText("[$ts] $message\n")
        }
    }

    fun filePath(context: Context): String {
        return File(context.filesDir, FILE_NAME).absolutePath
    }
}
