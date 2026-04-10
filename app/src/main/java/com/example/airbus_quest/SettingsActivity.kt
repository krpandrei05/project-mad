package com.example.airbus_quest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        Log.d(TAG, "onCreate: SettingsActivity created!")

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarSettings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val editTextUserIdentifier: EditText = findViewById(R.id.editTextUserIdentifier)
        editTextUserIdentifier.setText(sharedPreferences.getString("userIdentifier", ""))
        editTextUserIdentifier.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newId = editTextUserIdentifier.text.toString()
                if (newId.isNotBlank()) {
                    sharedPreferences.edit().putString("userIdentifier", newId).apply()
                    Toast.makeText(this, "Username saved: $newId", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "User ID updated: $newId")
                }
            }
        }

        val switchLocationTracking: SwitchMaterial = findViewById(R.id.switchLocationTracking)
        switchLocationTracking.isChecked = sharedPreferences.getBoolean("locationTrackingEnabled", false)
        switchLocationTracking.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("locationTrackingEnabled", isChecked).apply()
            val msg = if (isChecked) "Location tracking enabled" else "Location tracking disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Location tracking: $isChecked")
        }

        val switchNotifications: SwitchMaterial = findViewById(R.id.switchNotifications)
        switchNotifications.isChecked = sharedPreferences.getBoolean("notificationsEnabled", true)
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notificationsEnabled", isChecked).apply()
            Toast.makeText(this, "Notifications: $isChecked", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Notifications: $isChecked")
        }

        val toggleTempUnit: MaterialButtonToggleGroup = findViewById(R.id.toggleTempUnit)
        val savedUnit = sharedPreferences.getString("temperatureUnit", "celsius")
        if (savedUnit == "fahrenheit")
            toggleTempUnit.check(R.id.btnFahrenheit)
        else
            toggleTempUnit.check(R.id.btnCelsius)

        toggleTempUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val unit = if (checkedId == R.id.btnCelsius) "celsius" else "fahrenheit"
                val unitLabel = if (checkedId == R.id.btnCelsius) "Celsius" else "Fahrenheit"
                sharedPreferences.edit().putString("temperatureUnit", unit).apply()
                Toast.makeText(this, "Temperature unit set to $unitLabel", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Temperature unit: $unit")
            }
        }

        val seekRadius: SeekBar = findViewById(R.id.seekRadius)
        val tvRadiusValue: TextView = findViewById(R.id.tvRadiusValue)
        val savedRadius = sharedPreferences.getInt("detectionRadius", 50)
        seekRadius.progress = savedRadius
        tvRadiusValue.text = getString(R.string.setting_radius_format, savedRadius)
        seekRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvRadiusValue.text = getString(R.string.setting_radius_format, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: 50
                sharedPreferences.edit().putInt("detectionRadius", progress).apply()
                Log.d(TAG, "Detection radius: ${progress}m")
                Toast.makeText(this@SettingsActivity, "Radius set to ${progress}m", Toast.LENGTH_SHORT).show()
            }
        })

        val btnClearData: MaterialButton = findViewById(R.id.btnClearData)
        btnClearData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_clear_title))
                .setMessage(getString(R.string.confirm_clear_msg))
                .setPositiveButton(getString(R.string.confirm_clear_yes)) { _, _ ->
                    sharedPreferences.edit().clear().apply()
                    Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "All data cleared")
                    finish()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }
}