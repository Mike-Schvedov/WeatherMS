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
import com.mikeschvedov.weatherms.Models.RainAndForcast.Daily
import com.mikeschvedov.weatherms.databinding.ActivityMainBinding
import com.mikeschvedov.weatherms.util.Constants
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.math.roundToInt

const val TAG = "MainActivity"

var finishedAll: Int = 0

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Getting the time to set the background image
        checkTimeForBGImage()

        // Setting the loadinglayout to be visible
        binding.loadingLayout.isVisible = true


        binding.refreshButton.setOnClickListener {
            finish()
            startActivity(getIntent())
            // resetting value because it reached 3 in the last activity
            finishedAll = 0

        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {


                //-------------- GET COORDINATES OF THE DEVICE --------------//

                val task: Task<Location> = fusedLocationProviderClient.lastLocation

                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        101
                    )
                    return@repeatOnLifecycle
                }
                task.addOnSuccessListener {
                    if (it != null) {
                        Log.e("Main", "success")

                        // AFTER SUCCESSFULLY GETTING DEVICE'S COORDINATES, WE MAKE THE API CALL (USING THE COORDINATES)
                        lifecycleScope.launch {

                            println("COORDINATES: lat-${it.latitude}  long-${it.longitude}")
                            Toast.makeText(applicationContext, "${it.latitude}  long-${it.longitude}", Toast.LENGTH_LONG).show()
                            getWeather(it.latitude, it.longitude)
                            getRainAndForecast(it.latitude, it.longitude)
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

            // FORMATTING THE TEMPERATURE AND BINDING TO VIEW
            val temp: Int = response.body()!!.main.temp.roundToInt()
            binding.temperatureTxtview.text = "${temp}°"

            // GETTING AND BINDING THE WEATHER DESCRIPTION
            val status: String = response.body()!!.weather[0].description
            binding.statusTxtview.text = "${status}"

            // GETTING AND BINDING THE HUMIDITY
            val humidity: Int = response.body()!!.main.humidity
            val humidityStr = "${humidity}%"
            println("================= HUMIDITY ================ $humidityStr")
            binding.humidityTxtview.text = "לחות: ${humidityStr}"


            // ---------------------------- CHECKING IF ALL DATA WAS FETCHED and MAKE THE LOADING LAYOUT INVISIBLE --------------------------//
            finishedAll += 1
            if (finishedAll == 3) {
                binding.loadingLayout.isVisible = false
            }

        } else {
            Log.e(TAG, "Response not successful")
        }

    }

    private suspend fun getRainAndForecast(latitude: Double, longitude: Double) {

        val response = try {
            RetrofitInstance.api3.getRainAndForcast(
                latitude = latitude,
                longitude = longitude,
                units = "metric",
                language = "he",
                exclude = "minutely",
                API_key = Constants.OPEN_WEATHER_API_KEY
            )
        } catch (e: IOException) {
            Log.e(TAG, "IOException, you might not have internet connection (RainAndForcast)")
            return
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException, unexpected response (RainAndForcast)")
            return
        }
        if (response.isSuccessful && response.body() != null) {


            // ----------- GETTING AND BINDING CHANCE FOR RAIN -------------//

            var rain: Double = response.body()!!.hourly[1].pop

            // (Convert to percentage)

            rain *= 100

            val rainInt = rain.roundToInt()

            println("RAINNNNNNNN ${rainInt}")

            binding.rainTxtview.text = "סיכוי לגשם: ${rainInt}%"

            // ----------- GETTING DAILY FORECAST DATA -------------//

            // ------------------------------- Get DAY 1 (Tomorrow) -------------------------------------//
            val day1: Daily = response.body()!!.daily[1]

            // get and set date
            binding.day1Date.text = getDay(1)

            // get and set day of week
            binding.day1Dayofweek.text = getDayOfWeek(1)

            // get temp
            binding.tempDay1.text = "${day1.temp.day.roundToInt().toString()}°"

            // get and set status image
            println("WEATHER STATUS ${day1.weather[0].description}  DAY 1")

            val statusID: Int = checkStatusID(day1.weather[0].description)

            when (statusID) {

                1, 4 ->
                    binding.imageviewDay1.setImageResource(R.drawable.partly_cloud)
                2 ->
                    binding.imageviewDay1.setImageResource(R.drawable.clear_sunny)
                3, 6, 7 ->
                    binding.imageviewDay1.setImageResource(R.drawable.rain)
                5 ->
                    binding.imageviewDay1.setImageResource(R.drawable.cloudy)

                else ->
                    binding.imageviewDay1.setImageResource(R.drawable.error)
            }


            // ------------------------------- Get DAY 2 -------------------------------------//
            val day2: Daily = response.body()!!.daily[2]

            // get date
            binding.day2Date.text = getDay(2)

            // get and set day of week
            binding.day2Dayofweek.text = getDayOfWeek(2)

            // get temp
            binding.tempDay2.text = "${day2.temp.day.roundToInt().toString()}°"


            // get and set status image
            println("WEATHER STATUS ${day2.weather[0].description}  DAY 2")

            val statusID2: Int = checkStatusID(day2.weather[0].description)

            when (statusID2) {

                1, 4 ->
                    binding.imageviewDay2.setImageResource(R.drawable.partly_cloud)
                2 ->
                    binding.imageviewDay2.setImageResource(R.drawable.clear_sunny)
                3, 6, 7 ->
                    binding.imageviewDay2.setImageResource(R.drawable.rain)
                5 ->
                    binding.imageviewDay2.setImageResource(R.drawable.cloudy)

                else ->
                    binding.imageviewDay2.setImageResource(R.drawable.error)
            }


            // ------------------------------- Get DAY 3-------------------------------------//
            val day3: Daily = response.body()!!.daily[3]

            // get date
            binding.day3Date.text = getDay(3)

            // get and set day of week
            binding.day3Dayofweek.text = getDayOfWeek(3)

            // get temp
            binding.tempDay3.text = "${day3.temp.day.roundToInt().toString()}°"

            // get and set status image
            println("WEATHER STATUS ${day3.weather[0].description}  DAY 3")

            val statusID3: Int = checkStatusID(day3.weather[0].description)

            when (statusID3) {

                1, 4 ->
                    binding.imageviewDay3.setImageResource(R.drawable.partly_cloud)
                2 ->
                    binding.imageviewDay3.setImageResource(R.drawable.clear_sunny)
                3, 6, 7 ->
                    binding.imageviewDay3.setImageResource(R.drawable.rain)
                5 ->
                    binding.imageviewDay3.setImageResource(R.drawable.cloudy)

                else ->
                    binding.imageviewDay3.setImageResource(R.drawable.error)
            }

            // ---------------------------- CHECKING IF ALL DATA WAS FETCHED and MAKE THE LOADING LAYOUT INVISIBLE --------------------------//
            finishedAll += 1
            if (finishedAll == 3) {
                binding.loadingLayout.isVisible = false
            }

        } else {
            Log.e(TAG, "Response not successful (RainAndForecast)")
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
            val city: String = response.body()!!.city
            val country: String = response.body()!!.countryName

            println("DATA RECEIVED FROM API ---- CITY :${city}  COUNTRY: ${country}")

            // ------- Manually deciding location if failed to find small city/village
            if (city.isBlank()) {

                // Will return actuall city or if not found, will return the coordinates
              val newCity = getCityWithoutAPI(latitude, longitude) //returns a string

                binding.cityTxtview.text = "${newCity}, ${country}"
                println("FOUND NEW CITY : ${newCity}")
            }else{
              binding.cityTxtview.text = "${city}, ${country}"
             }

            // ---------------------------- CHECKING IF ALL DATA WAS FETCHED and MAKE THE LOADING LAYOUT INVISIBLE --------------------------//
            finishedAll += 1
            if (finishedAll == 3) {
                binding.loadingLayout.isVisible = false
            } else {
                Log.e(TAG, "Response not successful (getLocation)")
            }
        }
    }






    private fun checkStatusID(description: String): Int {

        return when (description) {

            "מעונן חלקית" -> 1
            "שמיים בהירים" -> 2
            "גשם קל" -> 3
            "עננים בודדים" -> 4
            "מעונן" -> 5
            "שברי ענן" -> 6
            "גשם בינוני" -> 7
            else -> 0

        }

    }

    private fun getDay(nextDay: Int): String {
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, nextDay)
        val tomorrow = calendar.time

        val dateFormat: DateFormat = SimpleDateFormat("dd/MM")

        return dateFormat.format(tomorrow)
    }

    private fun getDayOfWeek(nextDay: Int): String {
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_WEEK, nextDay)
        val day = calendar.time

        val dateFormat: DateFormat = SimpleDateFormat("EEE")

        return dateFormat.format(day)
    }

    private fun checkTimeForBGImage() {

        //TODO later you can place this function inside the weather api call, so you can pass the status parameter and set background according to weathe status

        // Getting the time
        val now: LocalDateTime = LocalDateTime.now()

        // Checking if it's day or night
        if (now.hour in 8..18) {    // It is day

            // Clear day
            binding.mainLayout.setBackgroundResource(R.drawable.clear)

        } else {        // It is night

            // Clear night
            binding.mainLayout.setBackgroundResource(R.drawable.clear_night)

        }

    }

    private fun getCityWithoutAPI(latitude: Double, longitude: Double): String {

        return if (checkIfMassad(latitude, longitude)) {
            "מסד"
        } else {
            "lat:${latitude}long:${longitude}"
        }

    }

    private fun checkIfMassad(latitude: Double, longitude: Double): Boolean {

        val formattedLat = (latitude * 100).toInt() / 100.00
        val formattedLong = (longitude * 100).toInt() / 100.00

        return formattedLat == 32.84 && formattedLong == 35.42 //return true if this is true

    }


}