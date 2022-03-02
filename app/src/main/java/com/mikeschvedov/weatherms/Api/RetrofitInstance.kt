package com.mikeschvedov.weatherms.Api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: WeatherAPI by lazy {
        Retrofit.Builder()
            .baseUrl("Https://api.openweathermap.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherAPI::class.java)


    }
}