package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AQI_LOG")
data class AqiLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // I use characterId to link each AQI record to the active character.
    // Default is -1, meaning no character is associated (recorded before K6).
    val characterId: Int = -1,

    val aqiValue: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)