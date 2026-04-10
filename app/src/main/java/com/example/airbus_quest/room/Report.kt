package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "REPORTS")
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stationId: Int,
    val comment: String,
    val rating: Int,
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)