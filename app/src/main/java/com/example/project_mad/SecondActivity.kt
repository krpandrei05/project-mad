package com.example.project_mad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
    }
}