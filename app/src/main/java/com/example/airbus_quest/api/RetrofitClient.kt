package com.example.airbus_quest.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Use a singleton object so I only create one Retrofit instance across the app.
// Both air_pollution and weather endpoints share the same base URL.
object RetrofitClient {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    // Build the Retrofit instance with Gson converter for automatic JSON deserialization
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Expose the service interface — callers use this to make API calls
    val openWeatherService: OpenWeatherService =
        retrofit.create(OpenWeatherService::class.java)
}