package com.prayertimemuter

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prayertimemuter.databinding.ActivitySettingsBinding
import com.prayertimemuter.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        
        setupToolbar()
        loadSettings()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun loadSettings() {
        binding.fajrBefore.setText(preferencesManager.getMinutesBefore("İmsak").toString())
        binding.fajrAfter.setText(preferencesManager.getMinutesAfter("İmsak").toString())
        binding.sunBefore.setText(preferencesManager.getMinutesBefore("Güneş").toString())
        binding.sunAfter.setText(preferencesManager.getMinutesAfter("Güneş").toString())
        binding.dhuhrBefore.setText(preferencesManager.getMinutesBefore("Öğle").toString())
        binding.dhuhrAfter.setText(preferencesManager.getMinutesAfter("Öğle").toString())
        binding.fridayBefore.setText(preferencesManager.fridayMinutesBefore.toString())
        binding.fridayAfter.setText(preferencesManager.fridayMinutesAfter.toString())
        when (preferencesManager.silentMode) {
            com.prayertimemuter.utils.SilentMode.SILENT -> binding.radioSilent.isChecked = true
            com.prayertimemuter.utils.SilentMode.VIBRATE -> binding.radioVibrate.isChecked = true
        }
        binding.checkboxAllowMobile.isChecked = preferencesManager.allowMobileData
        binding.asrBefore.setText(preferencesManager.getMinutesBefore("İkindi").toString())
        binding.asrAfter.setText(preferencesManager.getMinutesAfter("İkindi").toString())
        binding.maghribBefore.setText(preferencesManager.getMinutesBefore("Akşam").toString())
        binding.maghribAfter.setText(preferencesManager.getMinutesAfter("Akşam").toString())
        binding.ishaBefore.setText(preferencesManager.getMinutesBefore("Yatsı").toString())
        binding.ishaAfter.setText(preferencesManager.getMinutesAfter("Yatsı").toString())
    }
    
    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveSettings()
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun saveSettings() {
        try {
            preferencesManager.setMinutesBefore("İmsak", binding.fajrBefore.text.toString().toInt())
            preferencesManager.setMinutesAfter("İmsak", binding.fajrAfter.text.toString().toInt())
            
            preferencesManager.setMinutesBefore("Güneş", binding.sunBefore.text.toString().toInt())
            preferencesManager.setMinutesAfter("Güneş", binding.sunAfter.text.toString().toInt())
            
            preferencesManager.setMinutesBefore("Öğle", binding.dhuhrBefore.text.toString().toInt())
            preferencesManager.setMinutesAfter("Öğle", binding.dhuhrAfter.text.toString().toInt())
            preferencesManager.fridayMinutesBefore = binding.fridayBefore.text.toString().toInt()
            preferencesManager.fridayMinutesAfter = binding.fridayAfter.text.toString().toInt()
            preferencesManager.silentMode = if (binding.radioSilent.isChecked) {
                com.prayertimemuter.utils.SilentMode.SILENT
            } else {
                com.prayertimemuter.utils.SilentMode.VIBRATE
            }
            preferencesManager.allowMobileData = binding.checkboxAllowMobile.isChecked
            
            preferencesManager.setMinutesBefore("İkindi", binding.asrBefore.text.toString().toInt())
            preferencesManager.setMinutesAfter("İkindi", binding.asrAfter.text.toString().toInt())
            
            preferencesManager.setMinutesBefore("Akşam", binding.maghribBefore.text.toString().toInt())
            preferencesManager.setMinutesAfter("Akşam", binding.maghribAfter.text.toString().toInt())
            
            preferencesManager.setMinutesBefore("Yatsı", binding.ishaBefore.text.toString().toInt())
            preferencesManager.setMinutesAfter("Yatsı", binding.ishaAfter.text.toString().toInt())
            
            Toast.makeText(this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show()
            
            // Ayarlar değiştiğinde alarmları yeni ayarlarla yeniden planla
            val intent = android.content.Intent(this, com.prayertimemuter.services.PrayerTimeService::class.java).apply {
                action = com.prayertimemuter.services.PrayerTimeService.ACTION_START
            }
            startService(intent)
            
            finish()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Lütfen geçerli sayılar girin", Toast.LENGTH_SHORT).show()
        }
    }
}
