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
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.airbus_quest.room.Station
import com.example.airbus_quest.viewmodel.MapViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.lifecycle.lifecycleScope
import com.example.airbus_quest.api.RetrofitClient
import com.example.airbus_quest.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.database.FirebaseDatabase

class MapFragment : Fragment(), LocationListener {

    private val TAG = "MapFragment"
    private val viewModel: MapViewModel by viewModels()

    private lateinit var map: MapView
    private lateinit var bottomSheetStation: CardView
    private lateinit var tvStationName: TextView
    private lateinit var viewSheetAqi: View
    private lateinit var tvSheetAqiNum: TextView
    private lateinit var locationManager: LocationManager
    private var userLocationMarker: Marker? = null
    private lateinit var tvSheetDetails: TextView
    private lateinit var tvSheetReport: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().userAgentValue = "com.example.airbus_quest"
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osm", Context.MODE_PRIVATE)
        )

        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(14.0)
        map.controller.setCenter(GeoPoint(40.4168, -3.7038))

        bottomSheetStation = view.findViewById(R.id.bottomSheetStation)
        tvStationName = view.findViewById(R.id.tvStationName)
        viewSheetAqi = view.findViewById(R.id.viewSheetAqi)
        tvSheetAqiNum = view.findViewById(R.id.tvSheetAqiNum)
        tvSheetDetails = view.findViewById(R.id.tvSheetDetails)
        tvSheetReport = view.findViewById(R.id.tvSheetReport)
        tvSheetReport.text = ""

        // Close bottom sheet on tap outside or on close button
        view.findViewById<View>(R.id.btnCloseSheet).setOnClickListener {
            bottomSheetStation.visibility = View.GONE
        }

        // FAB centers map on user's last known location
        view.findViewById<FloatingActionButton>(R.id.fabMyLocation).setOnClickListener {
            userLocationMarker?.position?.let {
                map.controller.animateTo(it)
                map.controller.setZoom(16.0)
            } ?: run {
                map.controller.setCenter(GeoPoint(40.4168, -3.7038))
                map.controller.setZoom(14.0)
            }
        }

        // Observe stations from ViewModel and add colored markers
        viewModel.stations.observe(viewLifecycleOwner) { stations ->
            // Keep user location marker — only remove station markers
            map.overlays.removeIf { it is Marker && it != userLocationMarker }
            addStationMarkers(stations)
            map.invalidate()
        }
        viewModel.loadStations()

        // Start GPS for user location marker
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val hasFine = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 5f, this
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        val userPoint = GeoPoint(location.latitude, location.longitude)

        // Remove old user marker and add updated one
        userLocationMarker?.let { map.overlays.remove(it) }

        val marker = Marker(map)
        marker.position = userPoint
        marker.title = "My location"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = ContextCompat.getDrawable(
            requireContext(), android.R.drawable.ic_menu_mylocation
        )
        map.overlays.add(marker)
        userLocationMarker = marker
        map.invalidate()

        Log.d(TAG, "User location updated: ${location.latitude}, ${location.longitude}")
    }

    private fun addStationMarkers(stations: List<Station>) {
        for (station in stations) {
            val marker = Marker(map)
            marker.position = GeoPoint(station.latitude, station.longitude)
            marker.title = station.name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            val colorRes = when (station.lastAqi) {
                1 -> R.color.aqi_good
                2, 3 -> R.color.aqi_moderate
                4 -> R.color.aqi_unhealthy
                5 -> R.color.aqi_hazardous
                else -> R.color.aqi_unknown
            }
            marker.icon?.setTint(ContextCompat.getColor(requireContext(), colorRes))

            marker.setOnMarkerClickListener { _, _ ->
                tvStationName.text = station.name
                val lines = station.allLines.ifBlank { station.busLine }
                tvSheetDetails.text = if (lines.isNotBlank()) "Lines: $lines" else ""
                tvSheetAqiNum.text = "..."
                viewSheetAqi.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.aqi_unknown)
                fetchAqiForStation(station)
                fetchReportsForStation(station.name)
                bottomSheetStation.visibility = View.VISIBLE
                true
            }

            map.overlays.add(marker)
        }
        Log.d(TAG, "Added ${stations.size} station markers to map")
    }

    private fun fetchAqiForStation(station: Station) {
        val prefs = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("API_KEY", "4f4354bbc1ef7c530c57dbf3a0bbb5ea")
            ?: "4f4354bbc1ef7c530c57dbf3a0bbb5ea"

        RetrofitClient.openWeatherService
            .getAirPollution(station.latitude, station.longitude, apiKey)
            .enqueue(object : retrofit2.Callback<com.example.airbus_quest.api.AirPollutionResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.example.airbus_quest.api.AirPollutionResponse>,
                    response: retrofit2.Response<com.example.airbus_quest.api.AirPollutionResponse>
                ) {
                    if (response.isSuccessful) {
                        val aqiValue = response.body()?.list?.firstOrNull()?.main?.aqi ?: return
                        tvSheetAqiNum.text = aqiValue.toString()
                        val colorRes = when (aqiValue) {
                            1 -> R.color.aqi_good
                            2, 3 -> R.color.aqi_moderate
                            4 -> R.color.aqi_unhealthy
                            else -> R.color.aqi_hazardous
                        }
                        viewSheetAqi.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), colorRes)
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            AppDatabase.getDatabase(requireContext())
                                .stationDao().updateAqi(station.id, aqiValue)
                        }
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.example.airbus_quest.api.AirPollutionResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "AQI fetch failed for station ${station.name}: ${t.message}")
                }
            })
    }

    private fun fetchReportsForStation(stationName: String) {
        tvSheetReport.text = "Loading reports..."
        val key = stationName.replace(".", "_")
        FirebaseDatabase.getInstance("https://airbus-quest-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child("reports")
            .child(key)
            .limitToLast(1) // Show only the latest report
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    if (!snapshot.exists()) {
                        tvSheetReport.text = "No community reports yet."
                        return
                    }
                    snapshot.children.lastOrNull()?.let { child ->
                        val rating = child.child("rating").getValue(Int::class.java) ?: 0
                        val comment = child.child("comment").getValue(String::class.java) ?: ""
                        val userId = child.child("userId").getValue(String::class.java) ?: "unknown"
                        val stars = "★".repeat(rating) + "☆".repeat(5 - rating)
                        tvSheetReport.text = "$stars \"$comment\" — ${userId.take(8)}"
                        Log.d(TAG, "Report loaded for $stationName: $rating stars")
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    tvSheetReport.text = "Could not load reports."
                    Log.e(TAG, "RTDB error: ${error.message}")
                }
            })
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        startLocationUpdates()
        viewModel.loadStations()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        locationManager.removeUpdates(this)
    }

    @Deprecated("Deprecated in API")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}