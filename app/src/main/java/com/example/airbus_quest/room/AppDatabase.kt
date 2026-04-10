package com.example.airbus_quest.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameCharacter::class, Station::class, Report::class, AqiLog::class],
    version = 2, // I increment the version because I added characterId to AqiLog
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun stationDao(): StationDao
    abstract fun reportDao(): ReportDao
    abstract fun aqiLogDao(): AqiLogDao

    companion object {

        // I define this migration to add the characterId column to the existing AQI_LOG table.
        // I use ALTER TABLE because Room doesn't recreate the table automatically on version bump.
        // DEFAULT -1 ensures existing rows remain valid after the migration.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE AQI_LOG ADD COLUMN characterId INTEGER NOT NULL DEFAULT -1"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // I use the singleton pattern here to prevent multiple database instances
        // from being created simultaneously across different threads.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "airbus_quest_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}