package com.example.airbus_quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CHARACTERS")
data class GameCharacter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val avatarType: String,
    val nickname: String,
    val hp: Int = 100,
    val stationsVisited: Int = 0,
    val dayCount: Int = 1,
    val isAlive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)