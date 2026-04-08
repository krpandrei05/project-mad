package com.example.airbus_quest.room

import androidx.room.*

@Dao
interface StationDao {

    @Insert
    suspend fun insert(station: Station): Long

    // INSERT lista de stații — folosit la prima lansare pentru dataset EMT
    @Insert
    suspend fun insertAll(stations: List<Station>)

    @Query("SELECT * FROM STATIONS")
    suspend fun getAll(): List<Station>

    // Numărul de stații din tabel — folosit pentru a verifica dacă dataset-ul e deja inserat
    @Query("SELECT COUNT(*) FROM STATIONS")
    suspend fun getCount(): Int

    @Update
    suspend fun update(station: Station)

    @Delete
    suspend fun delete(station: Station)
}