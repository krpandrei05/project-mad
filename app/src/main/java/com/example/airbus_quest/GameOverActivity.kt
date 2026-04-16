package com.example.airbus_quest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.airbus_quest.room.AppDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameOverActivity : AppCompatActivity() {

    private val TAG = "GameOverActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        Log.d(TAG, "Game over screen shown")

        val tvSurvivedDays: TextView = findViewById(R.id.tvStatDays)
        val tvVisitedStations: TextView = findViewById(R.id.tvStatStations)
        val btnNewCharacter: MaterialButton = findViewById(R.id.btnNewCharacter)

        // Load dead character stats from Room
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val character = db.characterDao().getAll().firstOrNull { !it.isAlive }

            withContext(Dispatchers.Main) {
                character?.let {
                    tvSurvivedDays.text = getString(R.string.survived_days, it.dayCount)
                    tvVisitedStations.text = getString(R.string.visited_stations, it.stationsVisited)
                }
            }
        }

        btnNewCharacter.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_new_title))
                .setMessage(getString(R.string.confirm_new_msg))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    // Navigate to CreateCharacterActivity and clear the back stack
                    val intent = Intent(this, CreateCharacterActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }
}