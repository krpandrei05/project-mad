package com.example.airbus_quest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog

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

        val btnSecondActivity: Button = findViewById(R.id.btnSecondActivity)
        btnSecondActivity.setOnClickListener {
            Log.d(ThirdTag, "Click: Going to SecondActivity.")
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

        val btnDeleteCoordinate: Button = findViewById(R.id.btnDeleteCoordinate)
        btnDeleteCoordinate.setOnClickListener {
            Log.d(ThirdTag, "Click: Delete coordinate requested.")
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Coordinate")
        builder.setMessage("Are you sure you want to delete this coordinate?")

        builder.setPositiveButton("Delete") { _, _ ->
            Log.d(ThirdTag, "Click: Delete coordinate confirmed.")
            Toast.makeText(this, "Coordinate deleted!", Toast.LENGTH_LONG).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            Log.d(ThirdTag, "Click: Delete coordinate canceled.")
            dialog.cancel()
        }
        builder.show()
    }
}