package com.prayertimemuter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.prayertimemuter.databinding.ActivityMainBinding
import com.prayertimemuter.services.PrayerAlarmManager
import com.prayertimemuter.services.PrayerTimeService
import com.prayertimemuter.utils.PermissionUtils
import com.prayertimemuter.utils.PreferencesManager
import com.prayertimemuter.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        requestAllNeededPermissions()
        
        setupUI()
        updateUI()
    }
    
    private fun setupUI() {
        binding.btnSelectCity.setOnClickListener {
            startActivity(Intent(this, CitySelectionActivity::class.java))
        }
        
        binding.btnToggleService.setOnClickListener {
            toggleService()
        }
        
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnShowTimes.setOnClickListener {
            showTodayTimes()
        }

        binding.btnRefreshTimes.setOnClickListener {
            refreshTodayTimes()
        }
    }

    private fun showDndPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dnd_permission_title))
            .setMessage(getString(R.string.dnd_permission_message))
            .setPositiveButton(getString(R.string.dnd_permission_open)) { _, _ ->
                PermissionUtils.requestNotificationPolicyAccess(this)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun requestAllNeededPermissions() {
        // Bildirim izni (API 33+)
        PermissionUtils.requestRuntimePermissions(this)
        // Exact alarm (API 31+)
        if (PermissionUtils.needsExactAlarmPermission(this)) {
            PermissionUtils.requestExactAlarm(this)
        }
        // Battery optimization muafiyeti (API 23+)
        if (PermissionUtils.needsBatteryOptimizationException(this)) {
            PermissionUtils.requestBatteryOptimizationException(this)
        }
    }
    
    private fun updateUI() {
        val selectedCity = preferencesManager.selectedCity
        binding.cityText.text = selectedCity?.toString() ?: "Şehir seçilmedi"
        
        val isServiceEnabled = preferencesManager.isServiceEnabled
        if (isServiceEnabled) {
            binding.serviceStatusText.text = "Servis aktif"
            binding.serviceStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.btnToggleService.text = "Servisi Durdur"
        } else {
            binding.serviceStatusText.text = "Servis pasif"
            binding.serviceStatusText.setTextColor(getColor(android.R.color.holo_red_dark))
            binding.btnToggleService.text = "Servisi Başlat"
        }

        if (isServiceEnabled) {
            updateStalePanel()
        } else {
            binding.stalePanel.visibility = View.GONE
        }
    }
    
    private fun toggleService() {
        val selectedCity = preferencesManager.selectedCity
        
        if (selectedCity == null) {
            Toast.makeText(this, "Lütfen önce bir şehir seçin", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!PermissionUtils.hasAllPermissions(this)) {
            Toast.makeText(this, "Lütfen önce gerekli izinleri verin", Toast.LENGTH_SHORT).show()
            requestAllNeededPermissions()
            return
        }
        
        val isServiceEnabled = preferencesManager.isServiceEnabled
        
        if (isServiceEnabled) {
            stopService()
        } else {
            startService()
        }
    }
    
    private fun startService() {
        if (!PermissionUtils.hasNotificationPolicyAccess(this)) {
            showDndPermissionDialog()
            return
        }

        val intent = Intent(this, PrayerTimeService::class.java).apply {
            action = PrayerTimeService.ACTION_START
        }
        
        // Foreground notification kaldırıldığı için direkt startService kullan
        startService(intent)
        
        preferencesManager.isServiceEnabled = true
        updateUI()
        Toast.makeText(this, "Servis başlatıldı", Toast.LENGTH_SHORT).show()

        // Servis başlatıldığında otomatik veri çekmeyi dene; başarısızsa manuel butonu göster
        lifecycleScope.launch {
            val city = preferencesManager.selectedCity
            if (city != null) {
                val manager = PrayerAlarmManager(this@MainActivity)
                val success = manager.fetchAndScheduleWithRetry(city.id)
                preferencesManager.lastFetchFailed = !success
                updateStalePanel()
            }
        }
    }
    
    private fun stopService() {
        val intent = Intent(this, PrayerTimeService::class.java).apply {
            action = PrayerTimeService.ACTION_STOP
        }
        startService(intent)
        
        preferencesManager.isServiceEnabled = false
        updateUI()
        Toast.makeText(this, "Servis durduruldu", Toast.LENGTH_SHORT).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        PermissionUtils.onRequestPermissionsResult(
            requestCode = requestCode,
            permissions = permissions,
            grantResults = grantResults,
            onPermissionGranted = {
                preferencesManager.arePermissionsGranted = true
                updateUI()
                Toast.makeText(this, "Tüm izinler verildi", Toast.LENGTH_SHORT).show()
            },
            onPermissionDenied = {
                Toast.makeText(this, "Bazı izinler verilmedi, uygulama düzgün çalışmayabilir", Toast.LENGTH_LONG).show()
            }
        )
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun showTodayTimes() {
        val city = preferencesManager.selectedCity
        if (city == null) {
            Toast.makeText(this, "Lütfen önce bir şehir seçin", Toast.LENGTH_SHORT).show()
            return
        }
        val cached = preferencesManager.lastPrayerTimes
        if (cached == null) {
            Toast.makeText(this, getString(com.prayertimemuter.R.string.show_times_no_cache), Toast.LENGTH_SHORT).show()
            return
        }
        val dateLine = getString(com.prayertimemuter.R.string.show_times_date_label, cached.date)
        val message = listOf(dateLine) + cached.asList().map { (name, time) -> "$name: $time" }
        val messageText = message.joinToString("\n")
        AlertDialog.Builder(this@MainActivity)
            .setTitle(getString(com.prayertimemuter.R.string.show_times_dialog_title))
            .setMessage(messageText)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun updateStalePanel() {
        val cached = preferencesManager.lastPrayerTimes
        val fetchFailed = preferencesManager.lastFetchFailed
        if (cached == null || isStale(cached.date) || fetchFailed) {
            binding.stalePanel.visibility = View.VISIBLE
        } else {
            binding.stalePanel.visibility = View.GONE
        }
    }

    private fun isStale(dateString: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cachedDate: Date = sdf.parse(dateString) ?: return true
            val today = sdf.format(Date())
            sdf.format(cachedDate) != today
        } catch (e: Exception) {
            true
        }
    }

    private fun refreshTodayTimes() {
        val city = preferencesManager.selectedCity
        if (city == null) {
            Toast.makeText(this, "Lütfen önce bir şehir seçin", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.diyanetApiService.getPrayerTimes(city.id)
                if (response.isSuccessful) {
                    val list = response.body()
                    if (!list.isNullOrEmpty()) {
                        val today = list.first()
                        preferencesManager.lastPrayerTimes = today
                        updateStalePanel()
                        val dateLine = getString(com.prayertimemuter.R.string.show_times_date_label, today.date)
                        val message = listOf(dateLine) + today.asList().map { (name, time) -> "$name: $time" }
                        val messageText = message.joinToString("\n")
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(getString(com.prayertimemuter.R.string.show_times_dialog_title))
                            .setMessage(messageText)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                        return@launch
                    }
                }
                Toast.makeText(this@MainActivity, getString(com.prayertimemuter.R.string.show_times_error), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, getString(com.prayertimemuter.R.string.show_times_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
