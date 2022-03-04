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

    val api2: LocationAPI by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.bigdatacloud.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationAPI::class.java)
    }

    val api3: RainAndForcastAPI by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RainAndForcastAPI::class.java)
    }
}