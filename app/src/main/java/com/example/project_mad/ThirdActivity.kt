package com.example.project_mad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
    }
}