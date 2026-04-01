package com.example.airbus_quest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import java.io.IOException

class SecondActivity : AppCompatActivity() {
    private val SecondTag = "SecondActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        Log.d(SecondTag, "onCreate: SecondActivity created!")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMainActivity: Button = findViewById(R.id.btnMainActivity)
        btnMainActivity.setOnClickListener {
            Log.d(SecondTag, "Click: Going to MainActivity.")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnThirdActivity: Button = findViewById(R.id.btnThirdActivity)
        btnThirdActivity.setOnClickListener {
            Log.d(SecondTag, "Click: Going to ThirdActivity.")
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }

        val btnuserIdentifier: Button = findViewById(R.id.btnUserIdentifier)
        btnuserIdentifier.setOnClickListener {
            Log.d(SecondTag, "Click: Opening User ID dialog.")
            showUserIdentifierDialog()
        }

        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) {
            showUserIdentifierDialog()
        } else {
            Toast.makeText(this, "User ID: $userIdentifier", Toast.LENGTH_LONG).show()
        }

        val tvFileContents: TextView = findViewById(R.id.tvFileContents)
        tvFileContents.text = readFileContents()
    }

    private fun showUserIdentifierDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter User Identifier")

        val input = EditText(this)
        val existing = getUserIdentifier()
        if (existing != null) {
            input.setText(existing)
        }
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val userInput = input.text.toString()
            if (userInput.isNotBlank()) {
                saveUserIdentifier(userInput)
                Toast.makeText(this, "User ID saved: $userInput", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "User ID cannot be blank", Toast.LENGTH_LONG).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            Toast.makeText(this, "Thanks and goodbye!", Toast.LENGTH_LONG).show()
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveUserIdentifier(userIdentifier: String) {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("userIdentifier", userIdentifier)
            apply()
        }
    }


    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }

    private fun readFileContents(): String {
        val fileName = "gps_coordinates.csv"
        return try {
            openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }
        } catch (e: IOException) {
            "Error reading file: ${e.message}"
        }
    }
}