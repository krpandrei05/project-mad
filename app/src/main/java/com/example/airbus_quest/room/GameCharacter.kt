package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity definește această clasă ca tabel în baza de date Room.
// tableName = "CHARACTERS" — numele tabelului în SQLite.
@Entity(tableName = "CHARACTERS")
data class GameCharacter(
    // @PrimaryKey(autoGenerate = true) — ID-ul e generat automat de Room
    // la fiecare insert, similar cu AUTO_INCREMENT în SQL.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Tipul de avatar ales: "commuter", "cyclist", "pedestrian"
    val avatarType: String,

    // Nickname-ul introdus de utilizator
    val nickname: String,

    // HP curent al personajului (inițial 100)
    val hp: Int = 100,

    // Numărul de stații vizitate
    val stationsVisited: Int = 0,

    // Ziua de joc (incrementat de GameEngine)
    val dayCount: Int = 1,

    // Dacă personajul este activ (HP > 0)
    val isAlive: Boolean = true,

    // Timestamp la crearea personajului
    val createdAt: Long = System.currentTimeMillis()
)