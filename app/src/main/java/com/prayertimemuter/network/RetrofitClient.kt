package com.prayertimemuter.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = DiyanetApiService.BASE_URL
    
    val diyanetApiService: DiyanetApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DiyanetApiService::class.java)
    }
}
