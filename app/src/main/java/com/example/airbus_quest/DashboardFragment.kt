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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.airbus_quest.viewmodel.DashboardViewModel
import java.io.File

class DashboardFragment : Fragment(), LocationListener {
    private val TAG = "DashboardFragment"

    private lateinit var locationManager: LocationManager
    private lateinit var tvLocation: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvAirQuality: TextView
    private lateinit var tvAqiNumber: TextView
    private lateinit var viewAqiCircle: View
    private lateinit var ivWeatherIcon: ImageView
    private val viewModel: DashboardViewModel by viewModels()

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
        tvAqiNumber = view.findViewById(R.id.tvAqiNumber)
        viewAqiCircle = view.findViewById(R.id.viewAqiCircle)
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon)

        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Observe LiveData from the ViewModel.
        // When the Fragment is recreated (tab switch), it immediately gets the cached values.
        observeViewModel()

        // Observe locationTrackingEnabled from ViewModel —
        // reacts automatically when user changes the toggle in Settings
        viewModel.locationTrackingEnabled.observe(viewLifecycleOwner) { enabled ->
            if (enabled) {
                checkPermissionsAndStartLocation()
            } else {
                locationManager.removeUpdates(this)
                Log.d(TAG, "GPS stopped — tracking disabled from Settings")
            }
        }
    }

    // Observe each LiveData field and update the UI when it changes
    private fun observeViewModel() {
        viewModel.locationText.observe(viewLifecycleOwner) { tvLocation.text = it }

        viewModel.temperature.observe(viewLifecycleOwner) { tvTemperature.text = it }

        viewModel.airQualityText.observe(viewLifecycleOwner) { tvAirQuality.text = it }

        viewModel.aqi.observe(viewLifecycleOwner) { aqiValue ->
            tvAqiNumber.text = aqiValue.toString()
            val colorRes = when (aqiValue) {
                1 -> R.color.aqi_good
                2, 3 -> R.color.aqi_moderate
                4 -> R.color.aqi_unhealthy
                else -> R.color.aqi_hazardous
            }
            viewAqiCircle.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), colorRes)
        }

        // Use Glide to load the weather icon URL from LiveData
        viewModel.weatherIconUrl.observe(viewLifecycleOwner) { iconUrl ->
            Glide.with(requireContext()).load(iconUrl).into(ivWeatherIcon)
        }
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

        // Delegate the API calls to the ViewModel
        viewModel.fetchWeatherData(location.latitude, location.longitude)
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