package com.mikeschvedov.weatherms

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.mikeschvedov.weatherms.Api.RetrofitInstance
import com.mikeschvedov.weatherms.Models.Weather
import com.mikeschvedov.weatherms.databinding.ActivityMainBinding
import com.mikeschvedov.weatherms.util.Constants
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.roundToInt

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        var deviceLongitude: Double
        var deviceLatitude: Double

        // Setting the progress bar to be visible
        binding.progressBar.isVisible = true

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){


                //-------------- GET COORDINATES OF THE DEVICE --------------//

                val task : Task<Location> = fusedLocationProviderClient.lastLocation

                if(ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ){
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
                    return@repeatOnLifecycle
                }
                task.addOnSuccessListener {
                    if(it != null){
                        Log.e("Main", "success")

                        // AFTER SUCCESSFULLY GETTING DEVICE'S COORDINATES, WE MAKE THE API CALL (USING THE COORDINATES)
                        lifecycleScope.launch  {
                            getWeather(it.latitude, it.longitude)
                            getLocation(it.latitude, it.longitude)
                        }
                    }
                }

            }

        }
    }

    private suspend fun getWeather(latitude: Double, longitude: Double) {

        val response = try {
            RetrofitInstance.api.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                units = "metric",
                language = "he",
                API_key = Constants.OPEN_WEATHER_API_KEY
            )
        } catch (e: IOException) {
            Log.e(TAG, "IOException, you might not have internet connection")
            return
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException, unexpected response")
            return
        }
        if (response.isSuccessful && response.body() != null) {

            //GETTING CITY AND COUNTRY USING THE COORDINATES

            // SETTING PROGRESS BAR TO INVISIBLE
            binding.progressBar.isVisible = false

            // FORMATTING THE TEMPERATURE AND BINDING TO VIEW
            val temp : Int = response.body()!!.main.temp.roundToInt()
            binding.temperatureTxtview.text = "${temp}°"

            // GETTING AND BINDING THE WEATHER DESCRIPTION
            val status : String = response.body()!!.weather[0].description
            binding.statusTxtview.text = "${status}"

            // GETTING AND BINDING THE HUMIDITY
            val humidity : Int = response.body()!!.main.humidity
            var humidity_str = "${humidity}%"
            binding.humidityTxtview.text = "לחות: ${humidity_str}"

            // GETTING AND BINDING CHANCE OF RAIN
           // val rain : Int = response.body()!!
           // binding.rainTxtview.text = ""
/*



            // BINDING DAY 1
            binding.day1Date.text = ""
            binding.imageviewDay1.drawable
            binding.tempDay1.text = ""

            // BINDING DAY 2
            binding.day2Date.text = ""
            binding.imageviewDay2.drawable
            binding.tempDay2.text = ""

            // BINDING DAY 3
            binding.day3Date.text = ""
            binding.imageviewDay3.drawable
            binding.tempDay3.text = ""*/

        } else {
            Log.e(TAG, "Response not successful")
        }

    }


    private suspend fun getLocation(latitude: Double, longitude: Double) {

        val response = try {
            RetrofitInstance.api2.getLocationData(
                latitude = latitude,
                longitude = longitude,
                language = "he",
                key = Constants.BIG_DATA_API_KEY
            )
        } catch (e: IOException) {
            Log.e(TAG, "IOException, you might not have internet connection (getLocation)")
            return
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException, unexpected response (getLocation)")
            return
        }
        if (response.isSuccessful && response.body() != null) {

            // GETTING AND BINDING CITY AND COUNTRY NAMES
            val city : String = response.body()!!.city
            val country : String = response.body()!!.countryName

            Log.e("LocationAPI", "${city}, ${country}")
            binding.cityTxtview.text = "${city}, ${country}"

        } else {
            Log.e(TAG, "Response not successful (getLocation)")
        }

    }



}