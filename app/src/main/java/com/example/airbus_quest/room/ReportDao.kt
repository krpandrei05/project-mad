package com.example.airbus_quest.room

import androidx.room.*

@Dao
interface ReportDao {

    @Insert
    suspend fun insert(report: Report): Long

    @Query("SELECT * FROM REPORTS ORDER BY createdAt DESC")
    suspend fun getAll(): List<Report>

    @Query("SELECT * FROM REPORTS WHERE stationId = :stationId")
    suspend fun getByStation(stationId: Int): List<Report>

    @Update
    suspend fun update(report: Report)

    @Delete
    suspend fun delete(report: Report)
}