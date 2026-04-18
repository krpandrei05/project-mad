package com.example.airbus_quest.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val OPENWEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val EMT_BASE_URL = "https://openapi.emtmadrid.es/"
    private const val OSRM_BASE_URL = "https://router.project-osrm.org/"

    val openWeatherService: OpenWeatherService =
        Retrofit.Builder()
            .baseUrl(OPENWEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherService::class.java)

    val emtService: EmtService =
        Retrofit.Builder()
            .baseUrl(EMT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmtService::class.java)

    val osrmService: OsrmService =
        Retrofit.Builder()
            .baseUrl(OSRM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmService::class.java)
}