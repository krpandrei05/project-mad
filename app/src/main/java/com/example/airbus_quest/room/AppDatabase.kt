package com.example.airbus_quest.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database leagă toate entitățile și DAO-urile într-o singură bază de date.
// version = 1 — versiunea schemei. Dacă modifici entitățile, trebuie incrementat
// și adăugată o migrație. exportSchema = false — nu exportăm schema în fișiere JSON.
@Database(
    entities = [GameCharacter::class, Station::class, Report::class, AqiLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Room generează implementările acestor funcții abstracte la compilare.
    abstract fun characterDao(): CharacterDao
    abstract fun stationDao(): StationDao
    abstract fun reportDao(): ReportDao
    abstract fun aqiLogDao(): AqiLogDao

    companion object {
        // @Volatile — valoarea e mereu citită și scrisă din memoria principală,
        // nu din cache-ul thread-ului. Asigură vizibilitate între thread-uri.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern — returnează instanța existentă sau creează una nouă.
        // synchronized(this) — previne crearea mai multor instanțe în paralel.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "airbus_quest_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}