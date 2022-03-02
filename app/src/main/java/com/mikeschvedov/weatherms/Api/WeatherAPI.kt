package com.mikeschvedov.weatherms.Api

import com.mikeschvedov.weatherms.Models.CurrentWeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    //api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}

    @GET("/data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude : Double,
        @Query("lon") longitude : Double,
        @Query("units") units : String,
        @Query("appid") API_key : String
    ) : Response<CurrentWeatherData>
}