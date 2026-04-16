package com.example.airbus_quest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

class GameEngine(private val context: Context) {

    private val TAG = "GameEngine"
    private val CHANNEL_ID = "airbus_quest_channel"
    private val NOTIF_ID_AQI = 1
    private val NOTIF_ID_HP = 2

    init {
        createNotificationChannel()
    }

    // Called on every GPS update — checks nearby stations and updates HP
    fun onLocationUpdate(
        lat: Double,
        lon: Double,
        currentAqi: Int,
        onHpChanged: (Int) -> Unit,
        onGameOver: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val character = db.characterDao().getActiveCharacter() ?: return@launch
            val stations = db.stationDao().getAll()
            val radius = getDetectionRadius()

            val nearbyStation = findNearbyStation(lat, lon, stations, radius)

            if (nearbyStation != null) {
                Log.d(TAG, "Near station: ${nearbyStation.name}, AQI: $currentAqi")

                val hpDelta = when (currentAqi) {
                    1 -> +2       // Good air — recover HP
                    2 -> +1       // Fair — slight recovery
                    3 -> 0        // Moderate — neutral
                    4 -> -5       // Poor — drain HP
                    else -> -10   // Very poor — heavy drain
                }

                val newHp = (character.hp + hpDelta).coerceIn(0, 100)
                val updatedCharacter = character.copy(
                    hp = newHp,
                    stationsVisited = character.stationsVisited + 1,
                    isAlive = newHp > 0
                )
                db.characterDao().update(updatedCharacter)
                Log.d(TAG, "HP updated: ${character.hp} -> $newHp")

                // Notify user when AQI is poor and notifications are enabled
                if (currentAqi >= 4 && notificationsEnabled()) {
                    sendNotification(
                        NOTIF_ID_AQI,
                        "⚠️ Poor Air Quality",
                        "AQI $currentAqi near ${nearbyStation.name}. Your character lost HP!"
                    )
                }

                // Notify when HP is critically low
                if (newHp in 1..20 && notificationsEnabled()) {
                    sendNotification(
                        NOTIF_ID_HP,
                        "❤️ Critical HP",
                        "${character.nickname} is at $newHp HP! Find cleaner air!"
                    )
                }

                withContext(Dispatchers.Main) {
                    onHpChanged(newHp)
                    if (newHp == 0) onGameOver()
                }
            }
        }
    }

    // Haversine formula — calculates distance in meters between two GPS points
    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lon2 - lon1)
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun findNearbyStation(
        lat: Double, lon: Double,
        stations: List<Station>,
        radiusMeters: Int
    ): Station? = stations.firstOrNull {
        distanceMeters(lat, lon, it.latitude, it.longitude) <= radiusMeters
    }

    private fun getDetectionRadius(): Int {
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs.getInt("detectionRadius", 50)
    }

    private fun notificationsEnabled(): Boolean {
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs.getBoolean("notificationsEnabled", true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AirBus Quest Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Game alerts for AQI and HP status" }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }
}