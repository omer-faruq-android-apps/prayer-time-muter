package com.prayertimemuter.data

import com.prayertimemuter.models.City
import com.prayertimemuter.network.RetrofitClient

class CityRepository(
    private val api: com.prayertimemuter.network.DiyanetApiService = RetrofitClient.diyanetApiService
) {

    suspend fun listCities(country: String = "Turkey", city: String? = null): List<City> {
        return try {
            val response = api.listLocations(country = country, city = city)
            response.body().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchCities(query: String): List<City> {
        return try {
            val response = api.searchLocations(query)
            response.body().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
