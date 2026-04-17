package com.example.airbus_quest.room

import androidx.room.*

@Dao
interface StationDao {

    @Insert
    suspend fun insert(station: Station): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<Station>)

    @Query("SELECT * FROM STATIONS")
    suspend fun getAll(): List<Station>

    @Query("SELECT COUNT(*) FROM STATIONS")
    suspend fun getCount(): Int

    @Update
    suspend fun update(station: Station)

    @Delete
    suspend fun delete(station: Station)

    @Query("DELETE FROM STATIONS")
    suspend fun deleteAll()

    @Query("UPDATE STATIONS SET lastAqi = :aqi WHERE id = :stationId")
    suspend fun updateAqi(stationId: Int, aqi: Int)
}