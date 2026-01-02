package com.prayertimemuter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prayertimemuter.data.CityRepository
import com.prayertimemuter.databinding.ActivityCitySelectionBinding
import com.prayertimemuter.databinding.ItemCityBinding
import com.prayertimemuter.models.City
import com.prayertimemuter.utils.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CitySelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCitySelectionBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var cityAdapter: CityAdapter
    private lateinit var cityRepository: CityRepository
    private var searchJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        cityRepository = CityRepository()
        
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        loadCities()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        cityAdapter = CityAdapter { city ->
            selectCity(city)
        }
        
        binding.citiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CitySelectionActivity)
            adapter = cityAdapter
        }
    }
    
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { s ->
            val query = s?.toString().orEmpty()
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300)
                if (query.isBlank()) {
                    loadCities()
                } else {
                    searchCities(query)
                }
            }
        }
    }
    
    private fun loadCities() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val cities = cityRepository.listCities(country = "Turkey")
            cityAdapter.updateCities(cities)
            binding.progressBar.visibility = View.GONE
        }
    }
    
    private fun searchCities(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val filteredCities = cityRepository.searchCities(query)
            cityAdapter.updateCities(filteredCities)
            binding.progressBar.visibility = View.GONE
        }
    }
    
    private fun selectCity(city: City) {
        preferencesManager.selectedCity = city
        Toast.makeText(this, "${city.toString()} seçildi", Toast.LENGTH_SHORT).show()
        // Servis açıksa yeni şehirle alarmları yeniden planla
        if (preferencesManager.isServiceEnabled) {
            val intent = Intent(this, com.prayertimemuter.services.PrayerTimeService::class.java).apply {
                action = com.prayertimemuter.services.PrayerTimeService.ACTION_START
            }
            startService(intent)
        }
        finish()
    }
}

class CityAdapter(
    private val onCityClick: (City) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {
    
    private var cities = listOf<City>()
    
    fun updateCities(newCities: List<City>) {
        cities = newCities
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(cities[position])
    }
    
    override fun getItemCount(): Int = cities.size
    
    inner class CityViewHolder(private val binding: ItemCityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: City) {
            binding.cityNameText.text = city.toString()
            binding.root.setOnClickListener {
                onCityClick(city)
            }
        }
    }
}
