package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabelul REPORTS stochează rapoartele comunitare ale utilizatorilor.
// La v7 vor fi sincronizate și cu Firebase RTDB.
@Entity(tableName = "REPORTS")
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ID-ul stației la care s-a făcut raportul
    val stationId: Int,

    // Comentariul utilizatorului
    val comment: String,

    // Rating 1-5 stele
    val rating: Int,

    // Path-ul local al fotografiei (null dacă nu există)
    val photoPath: String? = null,

    // Timestamp raport
    val createdAt: Long = System.currentTimeMillis()
)