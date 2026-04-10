package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "STATIONS")
data class Station(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val busLine: String,
    val lastAqi: Int = -1
)