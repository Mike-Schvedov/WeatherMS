package com.mikeschvedov.weatherms.Api

import com.mikeschvedov.weatherms.Models.CurrentWeatherData
import com.mikeschvedov.weatherms.Models.LocationData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationAPI {

    //https://api.bigdatacloud.net/data/reverse-geocode?latitude=40.780&longitude=-73.967&localityLanguage=en&key=[your api key]

    @GET("/data/reverse-geocode")
    suspend fun getLocationData(
        @Query("latitude") latitude : Double,
        @Query("longitude") longitude : Double,
        @Query("localityLanguage") language : String,
        @Query("key") key : String
    ) : Response<LocationData>

}