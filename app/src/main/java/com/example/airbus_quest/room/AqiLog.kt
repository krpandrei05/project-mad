package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabelul AQI_LOG stochează istoricul valorilor AQI
// înregistrate la coordonatele GPS ale utilizatorului.
@Entity(tableName = "AQI_LOG")
data class AqiLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Valoarea AQI (1-5 conform OpenWeather)
    val aqiValue: Int,

    // Coordonatele unde s-a înregistrat AQI-ul
    val latitude: Double,
    val longitude: Double,

    // Timestamp înregistrare
    val timestamp: Long = System.currentTimeMillis()
)