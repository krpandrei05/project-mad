package com.example.airbus_quest

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.airbus_quest.api.RetrofitClient
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.viewmodel.DashboardViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private lateinit var gameEngine: GameEngine
    private lateinit var tvNickname: TextView
    private lateinit var tvHpValue: TextView
    private lateinit var tvStats: TextView
    private lateinit var viewHpFill: View
    private lateinit var rvRecommendations: androidx.recyclerview.widget.RecyclerView
    private lateinit var tvNoAlerts: TextView
    private val recommendations = mutableListOf<RecommendationItem>()
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var ivAvatar: ImageView
    private var lastLocation: Location? = null

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
        gameEngine = GameEngine(requireContext())
        tvNickname = view.findViewById(R.id.tvNickname)
        tvHpValue = view.findViewById(R.id.tvHpValue)
        tvStats = view.findViewById(R.id.tvStats)
        viewHpFill = view.findViewById(R.id.viewHpFill)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
        tvNoAlerts = view.findViewById(R.id.tvNoAlerts)
        ivAvatar = view.findViewById(R.id.ivAvatar)

        recommendationAdapter = RecommendationAdapter(requireContext(), recommendations)
        rvRecommendations.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        rvRecommendations.adapter = recommendationAdapter

        // Load active character on start
        loadActiveCharacter()

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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadActiveCharacter()
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

            updateRecommendations(aqiValue)
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
        lastLocation = location
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

        fetchNearbyEmtStops(location.latitude, location.longitude)

        // Run game logic for nearby stations
        val lastAqi = viewModel.aqi.value ?: 1
        gameEngine.onLocationUpdate(
            lat = location.latitude,
            lon = location.longitude,
            currentAqi = lastAqi,
            onHpChanged = { newHp -> updateHpUI(newHp) },
            onGameOver = {
                startActivity(Intent(requireContext(), GameOverActivity::class.java))
            }
        )
    }

    private fun saveCoordinatesToFile(latitude: Double, longitude: Double, altitude: Double, timestamp: Long) {
        val prefs = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val characterId = prefs.getInt("activeCharacterId", -1)
        val fileName = if (characterId != -1) "gps_coordinates_$characterId.csv" else "gps_coordinates.csv"
        val file = File(requireContext().filesDir, fileName)

        val formattedLat = "%.4f".format(latitude)
        val formattedLong = "%.4f".format(longitude)
        val formattedAlt = "%.2f".format(altitude)

        file.appendText("$timestamp;$formattedLat;$formattedLong;$formattedAlt\n")
        Log.d(TAG, "CSV saved: $timestamp;$formattedLat;$formattedLong;$formattedAlt")
    }

    internal fun loadActiveCharacter(saveLocation: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val prefs = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val activeId = prefs.getInt("activeCharacterId", -1)

            // Use saved active ID if present, otherwise fall back to first alive character
            val character = if (activeId != -1) {
                db.characterDao().getById(activeId)
            } else {
                db.characterDao().getActiveCharacter()
            }

            withContext(Dispatchers.Main) {
                character?.let {
                    if (saveLocation) {
                        lastLocation?.let { loc ->
                            saveCoordinatesToFile(loc.latitude, loc.longitude, loc.altitude, System.currentTimeMillis())
                        }
                    }
                    tvNickname.text = it.nickname
                    val iconRes = when (it.avatarType) {
                        "commuter"   -> android.R.drawable.ic_menu_directions
                        "cyclist"    -> android.R.drawable.ic_menu_compass
                        "pedestrian" -> android.R.drawable.ic_menu_myplaces
                        else         -> android.R.drawable.ic_menu_myplaces
                    }
                    ivAvatar.setImageResource(iconRes)
                    updateHpUI(it.hp)
                    tvStats.text = getString(R.string.day_stations_format, it.dayCount, it.stationsVisited)
                    Log.d(TAG, "Active character loaded: ${it.nickname} (id=${it.id})")
                    (requireActivity() as? MainActivity)?.updateDrawerCharacter()
                }
            }
        }
    }

    private fun updateHpUI(hp: Int) {
        tvHpValue.text = getString(R.string.hp_format, hp)

        // Scale the HP bar width proportionally to HP percentage
        viewHpFill.post {
            val parentWidth = (viewHpFill.parent as android.view.View).width
            val params = viewHpFill.layoutParams
            params.width = (parentWidth * hp / 100f).toInt()
            viewHpFill.layoutParams = params
        }

        // Change HP bar color by threshold
        val colorRes = when {
            hp > 60 -> R.color.hp_excellent
            hp > 30 -> R.color.hp_warning
            else -> R.color.hp_critical
        }
        viewHpFill.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), colorRes)
    }

    private fun fetchNearbyEmtStops(lat: Double, lon: Double) {
        val radius = 500
        // val radius = requireActivity()
        //    .getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        //    .getInt("detectionRadius", 500)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val loginResp = RetrofitClient.emtService
                    .login(Credentials.EMT_EMAIL, Credentials.EMT_PASSWORD)
                    .execute()

                val token = loginResp.body()?.data?.firstOrNull()?.accessToken ?: run {
                    Log.e(TAG, "EMT login failed")
                    return@launch
                }

                val stops = RetrofitClient.emtService
                    .getStopsAroundLocation(token, lon, lat, radius)
                    .execute()
                    .body()?.data ?: run {
                    Log.e(TAG, "EMT stops fetch failed")
                    return@launch
                }

                val db = AppDatabase.getDatabase(requireContext())
                val stations = stops.map { stop ->
                    com.example.airbus_quest.room.Station(
                        name = stop.stopName,
                        latitude = stop.geometry.coordinates[1],
                        longitude = stop.geometry.coordinates[0],
                        busLine = stop.lines?.firstOrNull()?.label ?: "EMT",
                        allLines = stop.lines?.joinToString(", ") { it.label } ?: ""
                    )
                }
                db.stationDao().insertAll(stations)
                Log.d(TAG, "EMT nearby stops saved: ${stations.size}")

            } catch (e: Exception) {
                Log.e(TAG, "EMT fetch error: ${e.message}")
            }
        }
    }

    private fun updateRecommendations(aqi: Int) {
        recommendations.clear()
        when {
            aqi >= 5 -> recommendations.add(
                RecommendationItem("Very poor air quality! Stay indoors.", aqi, "now")
            )
            aqi == 4 -> recommendations.add(
                RecommendationItem("Poor air quality near this station. Avoid prolonged exposure.", aqi, "now")
            )
            aqi == 3 -> recommendations.add(
                RecommendationItem("Moderate air quality. Sensitive groups should be cautious.", aqi, "now")
            )
            aqi <= 2 -> recommendations.add(
                RecommendationItem("Good air quality. Safe to travel!", aqi, "now")
            )
        }

        if (recommendations.isEmpty()) {
            tvNoAlerts.visibility = View.VISIBLE
            rvRecommendations.visibility = View.GONE
        } else {
            tvNoAlerts.visibility = View.GONE
            rvRecommendations.visibility = View.VISIBLE
        }
        recommendationAdapter.notifyDataSetChanged()
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