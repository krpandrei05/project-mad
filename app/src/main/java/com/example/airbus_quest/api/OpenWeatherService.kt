package com.example.airbus_quest.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Define two OpenWeather endpoints:
// - air_pollution: returns AQI 1-5 for given coordinates
// - weather: returns temperature and icon for given coordinates
interface OpenWeatherService {

    @GET("air_pollution")
    fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Call<AirPollutionResponse>

    @GET("weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}