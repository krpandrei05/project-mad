package com.example.airbus_quest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.File

class DashboardFragment : Fragment(), LocationListener {
    private val TAG = "DashboardFragment"

    private lateinit var locationManager: LocationManager
    private lateinit var tvLocation: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvAirQuality: TextView
    private lateinit var switchLocation: SwitchMaterial

    private val locationPermissionCode = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: DashboardFragment ready")

        tvLocation = view.findViewById(R.id.tvLocation)
        tvTemperature = view.findViewById(R.id.tvTemperature)
        tvAirQuality = view.findViewById(R.id.tvAirQuality)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        switchLocation = view.findViewById(R.id.switchLocation)

        val savedState = requireActivity()
            .getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            .getBoolean("locationTrackingEnabled", false)

        switchLocation.isChecked = savedState
        if (savedState) {
            checkPermissionsAndStartLocation()
        }

        switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d(TAG, "Switch ON: starting GPS")
                checkPermissionsAndStartLocation()
                Toast.makeText(requireContext(), "Location tracking enabled", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Switch OFF: stopping GPS")
                locationManager.removeUpdates(this)
                Toast.makeText(requireContext(), "Location tracking disabled", Toast.LENGTH_SHORT).show()
            }
            saveSwitchState(isChecked)
        }
    }

    private fun saveSwitchState(isEnabled: Boolean) {
        requireActivity()
            .getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("locationTrackingEnabled", isEnabled)
            .apply()
    }

    private fun checkPermissionsAndStartLocation() {
        val hasFine = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionCode
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                5f,
                this
            )
            Log.d(TAG, "GPS started")
        }
    }

    override fun onLocationChanged(location: Location) {
        val timestamp = System.currentTimeMillis()

        val lat = "%.4f".format(location.latitude)
        val long = "%.4f".format(location.longitude)
        val alt = "%.1f".format(location.altitude)

        Log.i(TAG, "Location updated: timestamp=$timestamp, lat=$lat, long=$long, alt=$alt")

        saveCoordinatesToFile(location.latitude, location.longitude, location.altitude, timestamp)
        val toastText = "New location: ${location.latitude}, ${location.longitude}"
        Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()

        tvLocation.text = "Lat: $lat, Long: $long"

        // Placeholder
        tvTemperature.text = "--°C"
        tvAirQuality.text = "Waiting for AQI data"
    }

    private fun saveCoordinatesToFile(latitude: Double, longitude: Double, altitude: Double, timestamp: Long) {
        val fileName = "gps_coordinates.csv"
        val file = File(requireContext().filesDir, fileName)

        val formattedLat = "%.4f".format(latitude)
        val formattedLong = "%.4f".format(longitude)
        val formattedAlt = "%.2f".format(altitude)

        file.appendText("$timestamp;$formattedLat;$formattedLong;$formattedAlt\n")
        Log.d(TAG, "CSV saved: $timestamp;$formattedLat;$formattedLong;$formattedAlt")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationManager.removeUpdates(this)
        Log.d(TAG, "GPS stopped (fragment destroyed)")
    }

    @Deprecated("Deprecated in API")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}