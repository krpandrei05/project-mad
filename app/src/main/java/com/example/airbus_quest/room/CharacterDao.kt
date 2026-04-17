package com.example.airbus_quest.room

import androidx.room.*

@Dao
interface CharacterDao {
    @Insert
    suspend fun insert(character: GameCharacter): Long
    @Query("SELECT * FROM CHARACTERS ORDER BY isAlive DESC, createdAt DESC")
    suspend fun getAll(): List<GameCharacter>
    @Query("SELECT * FROM CHARACTERS WHERE isAlive = 1 LIMIT 1")
    suspend fun getActiveCharacter(): GameCharacter?
    @Update
    suspend fun update(character: GameCharacter)
    @Delete
    suspend fun delete(character: GameCharacter)
    @Query("SELECT * FROM CHARACTERS WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GameCharacter?
}