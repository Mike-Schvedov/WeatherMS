package com.mikeschvedov.weatherms.Api

import com.mikeschvedov.weatherms.Models.RainAndForcast.Forcast
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RainAndForcastAPI {

   // https://api.openweathermap.org/data/2.5/onecall?lat=33.44&lon=-94.04&exclude=hourly,daily&appid={API key}

    @GET("/data/2.5/onecall")
    suspend fun getRainAndForcast(
        @Query("lat") latitude : Double,
        @Query("lon") longitude : Double,
        @Query("units") units : String,
        @Query("lang") language : String,
        @Query("exclude") exclude : String,
        @Query("appid") API_key : String
    ) : Response<Forcast>
}