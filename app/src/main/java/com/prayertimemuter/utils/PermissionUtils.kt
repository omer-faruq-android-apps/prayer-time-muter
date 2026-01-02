package com.prayertimemuter.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.app.NotificationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtils {
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001

        private fun runtimePermissions(): Array<String> {
            val list = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            return list.toTypedArray()
        }

        fun hasAllPermissions(context: Context): Boolean {
            val perms = runtimePermissions()
            return perms.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun requestRuntimePermissions(activity: Activity) {
            val perms = runtimePermissions()
            if (perms.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    activity,
                    perms,
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        fun needsExactAlarmPermission(context: Context): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !(context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager).canScheduleExactAlarms()
        }

        fun requestExactAlarm(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
            }
        }

        fun needsBatteryOptimizationException(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
            val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            return !pm.isIgnoringBatteryOptimizations(context.packageName)
        }

        fun requestBatteryOptimizationException(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
            }
        }

        fun isIgnoringBatteryOptimizations(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
            val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            return pm.isIgnoringBatteryOptimizations(context.packageName)
        }

        fun hasNotificationPolicyAccess(context: Context): Boolean {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return nm.isNotificationPolicyAccessGranted
        }

        fun requestNotificationPolicyAccess(activity: Activity) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            activity.startActivity(intent)
        }

        fun openBatteryOptimizationSettings(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
        
        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit
        ) {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
    }
}
