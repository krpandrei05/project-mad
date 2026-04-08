package com.example.airbus_quest.room

import androidx.room.*

@Dao
interface AqiLogDao {

    @Insert
    suspend fun insert(aqiLog: AqiLog): Long

    @Query("SELECT * FROM AQI_LOG ORDER BY timestamp DESC")
    suspend fun getAll(): List<AqiLog>

    // Ultimele N înregistrări — folosit pentru grafic/istoric pe Dashboard
    @Query("SELECT * FROM AQI_LOG ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<AqiLog>

    @Delete
    suspend fun delete(aqiLog: AqiLog)
}