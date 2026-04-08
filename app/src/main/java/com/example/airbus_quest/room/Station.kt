package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabelul STATIONS stochează stațiile EMT din Madrid.
// Dataset-ul de 30-50 stații e inserat la prima lansare.
@Entity(tableName = "STATIONS")
data class Station(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Numele stației EMT
    val name: String,

    // Coordonatele GPS ale stației
    val latitude: Double,
    val longitude: Double,

    // Linia de autobuz (ex: "27", "C1")
    val busLine: String,

    // Ultimul AQI înregistrat la această stație (default -1 = necunoscut)
    val lastAqi: Int = -1
)