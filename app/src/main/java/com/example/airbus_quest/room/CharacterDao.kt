package com.example.airbus_quest.room

import androidx.room.*

// DAO (Data Access Object) — interfața prin care facem CRUD pe tabelul CHARACTERS.
// Room generează automat implementarea la compilare (de aceea avem nevoie de kotlin-kapt).
@Dao
interface CharacterDao {

    // INSERT — inserează un nou personaj și returnează ID-ul generat
    @Insert
    suspend fun insert(character: GameCharacter): Long

    // SELECT ALL — returnează toți caracterele, cei vii primii
    @Query("SELECT * FROM CHARACTERS ORDER BY isAlive DESC, createdAt DESC")
    suspend fun getAll(): List<GameCharacter>

    // SELECT active character — primul personaj viu
    @Query("SELECT * FROM CHARACTERS WHERE isAlive = 1 LIMIT 1")
    suspend fun getActiveCharacter(): GameCharacter?

    // UPDATE — actualizează un personaj existent (HP, stații, etc.)
    @Update
    suspend fun update(character: GameCharacter)

    // DELETE — șterge un personaj din tabel
    @Delete
    suspend fun delete(character: GameCharacter)
}