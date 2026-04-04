package com.example.airbus_quest

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException

class ThirdActivity : AppCompatActivity() {
    private val ThirdTag = "ThirdActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_third)

        Log.d(ThirdTag, "onCreate: ThirdActivity created!")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val timestamp = intent.getStringExtra("timestamp") ?: ""
        val latitude  = intent.getStringExtra("latitude")  ?: ""
        val longitude = intent.getStringExtra("longitude") ?: ""
        val altitude  = intent.getStringExtra("altitude")  ?: ""

        Log.d(ThirdTag, "Received: ts=$timestamp, lat=$latitude, lon=$longitude, alt=$altitude")

        // Utilizatorul poate vedea datele clar.
        val etTimestamp: EditText = findViewById(R.id.etTimestamp)
        val etLatitude:  EditText = findViewById(R.id.etLatitude)
        val etLongitude: EditText = findViewById(R.id.etLongitude)
        val etAltitude:  EditText = findViewById(R.id.etAltitude)

        etTimestamp.setText(timestamp)
        etLatitude.setText(latitude)
        etLongitude.setText(longitude)
        etAltitude.setText(altitude)

        val btnDeleteCoordinate: Button = findViewById(R.id.btnDeleteCoordinate)
        btnDeleteCoordinate.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Coordinate")
                .setMessage(
                    "Are you sure you want to delete this coordinate?\n\n" +
                            "📍 Timestamp: $timestamp\n" +
                            "📍 Latitude: $latitude\n" +
                            "📍 Longitude: $longitude\n" +
                            "📍 Altitude: $altitude"
                )
                .setPositiveButton("Delete") { _, _ ->
                    deleteCoordinate(timestamp)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val btnBack: Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun deleteCoordinate(timestamp: String) {
        val fileName = "gps_coordinates.csv"
        val file = File(filesDir, fileName)

        try {
            val allLines = file.readLines()

            val filteredLines = allLines.filter { line ->
                line.split(";")[0] != timestamp
            }

            file.writeText(filteredLines.joinToString("\n"))

            if (filteredLines.isNotEmpty()) {
                file.appendText("\n")
            }

            Toast.makeText(this, "Coordinate deleted!", Toast.LENGTH_SHORT).show()
            Log.d(ThirdTag, "Deleted coordinate with timestamp: $timestamp")

            finish()

        } catch (e: IOException) {
            Toast.makeText(this, "Error deleting coordinate: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(ThirdTag, "Error deleting: ${e.message}")
        }
    }
}