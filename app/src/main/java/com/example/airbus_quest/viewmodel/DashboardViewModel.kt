package com.example.airbus_quest.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.airbus_quest.api.AirPollutionResponse
import com.example.airbus_quest.api.RetrofitClient
import com.example.airbus_quest.api.WeatherResponse
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.AqiLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "DashboardViewModel"
    private val DEFAULT_API_KEY = "4f4354bbc1ef7c530c57dbf3a0bbb5ea"

    val aqi = MutableLiveData<Int>()
    val airQualityText = MutableLiveData<String>()
    val temperature = MutableLiveData<String>()
    val weatherIconUrl = MutableLiveData<String>()
    val locationText = MutableLiveData<String>()

    // Expose locationTracking as LiveData so DashboardFragment can react to changes
    val locationTrackingEnabled = MutableLiveData<Boolean>()

    // Store the last Kelvin value so I can reformat when the unit changes
    private var lastTempKelvin: Double? = null

    private val prefs: SharedPreferences =
        application.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    // Listen for any SharedPreferences change and react accordingly
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "temperatureUnit" -> {
                // Reformat the cached temperature when the unit changes
                lastTempKelvin?.let { kelvin ->
                    temperature.postValue(formatTemperature(kelvin))
                    Log.d(TAG, "Temperature unit changed, reformatting: ${formatTemperature(kelvin)}")
                }
            }
            "locationTrackingEnabled" -> {
                val enabled = prefs.getBoolean("locationTrackingEnabled", false)
                locationTrackingEnabled.postValue(enabled)
                Log.d(TAG, "Location tracking changed: $enabled")
            }
        }
    }

    init {
        // Register the listener as soon as the ViewModel is created
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        // Post the initial value so the Fragment knows the current state on first load
        locationTrackingEnabled.postValue(
            prefs.getBoolean("locationTrackingEnabled", false)
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the listener when the ViewModel is destroyed to avoid memory leaks
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        Log.d(TAG, "ViewModel cleared, listener unregistered")
    }

    private fun getApiKey(): String =
        prefs.getString("API_KEY", DEFAULT_API_KEY) ?: DEFAULT_API_KEY

    private fun formatTemperature(kelvin: Double): String {
        val unit = prefs.getString("temperatureUnit", "celsius")
        return if (unit == "fahrenheit") {
            "%.1f°F".format((kelvin - 273.15) * 9 / 5 + 32)
        } else {
            "%.1f°C".format(kelvin - 273.15)
        }
    }

    fun fetchWeatherData(lat: Double, lon: Double) {
        val apiKey = getApiKey()
        locationText.postValue("Lat: ${"%.4f".format(lat)}, Lon: ${"%.4f".format(lon)}")
        fetchAirPollution(lat, lon, apiKey)
        fetchWeather(lat, lon, apiKey)
    }

    private fun fetchAirPollution(lat: Double, lon: Double, apiKey: String) {
        RetrofitClient.openWeatherService.getAirPollution(lat, lon, apiKey)
            .enqueue(object : Callback<AirPollutionResponse> {
                override fun onResponse(
                    call: Call<AirPollutionResponse>,
                    response: Response<AirPollutionResponse>
                ) {
                    if (response.isSuccessful) {
                        val aqiValue = response.body()?.list?.firstOrNull()?.main?.aqi ?: return
                        Log.d(TAG, "AQI received: $aqiValue")
                        aqi.postValue(aqiValue)
                        val textRes = when (aqiValue) {
                            1 -> "Air Quality: Good"
                            2, 3 -> "Air Quality: Moderate"
                            4 -> "Air Quality: Unhealthy"
                            else -> "Air Quality: Hazardous"
                        }
                        airQualityText.postValue(textRes)
                        saveAqiToRoom(aqiValue, lat, lon)
                    } else {
                        Log.e(TAG, "AQI error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<AirPollutionResponse>, t: Throwable) {
                    Log.e(TAG, "AQI call failed: ${t.message}")
                }
            })
    }

    private fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        RetrofitClient.openWeatherService.getCurrentWeather(lat, lon, apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weather = response.body() ?: return
                        lastTempKelvin = weather.main.temp
                        temperature.postValue(formatTemperature(weather.main.temp))

                        val iconCode = weather.weather.firstOrNull()?.icon
                        if (iconCode != null) {
                            weatherIconUrl.postValue(
                                "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                            )
                        }
                        Log.d(TAG, "Weather received for: ${weather.name}")
                    } else {
                        Log.e(TAG, "Weather error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e(TAG, "Weather call failed: ${t.message}")
                    temperature.postValue("--°C")
                }
            })
    }

    private fun saveAqiToRoom(aqiValue: Int, lat: Double, lon: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(getApplication())
            val activeCharacter = db.characterDao().getActiveCharacter()
            val characterId = activeCharacter?.id ?: -1
            db.aqiLogDao().insert(
                AqiLog(characterId = characterId, aqiValue = aqiValue, latitude = lat, longitude = lon)
            )
            Log.d(TAG, "AQI saved: aqi=$aqiValue, characterId=$characterId")
        }
    }
}