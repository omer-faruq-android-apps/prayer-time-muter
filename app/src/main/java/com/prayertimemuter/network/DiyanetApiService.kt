package com.prayertimemuter.network

import com.prayertimemuter.models.City
import com.prayertimemuter.models.PrayerTimes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiyanetApiService {

    @GET("api/diyanet/search")
    suspend fun searchLocations(
        @Query("q") query: String
    ): Response<List<City>>

    @GET("api/diyanet/locations")
    suspend fun listLocations(
        @Query("country") country: String,
        @Query("city") city: String? = null
    ): Response<List<City>>

    @GET("api/diyanet/prayertimes")
    suspend fun getPrayerTimes(
        @Query("location_id") locationId: Int
    ): Response<List<PrayerTimes>>

    companion object {
        const val BASE_URL = "https://prayertimes.api.abdus.dev/"
    }
}
