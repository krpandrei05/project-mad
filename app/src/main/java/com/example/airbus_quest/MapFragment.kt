package com.example.airbus_quest

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.Station
import com.example.airbus_quest.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    private val TAG = "MapFragment"
    private val viewModel: MapViewModel by viewModels()

    private lateinit var map: MapView
    private lateinit var bottomSheetStation: CardView
    private lateinit var tvStationName: TextView
    private lateinit var viewSheetAqi: View
    private lateinit var tvSheetAqiNum: TextView

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
        map.controller.setCenter(GeoPoint(40.4168, -3.7038)) // Madrid center

        bottomSheetStation = view.findViewById(R.id.bottomSheetStation)
        tvStationName = view.findViewById(R.id.tvStationName)
        viewSheetAqi = view.findViewById(R.id.viewSheetAqi)
        tvSheetAqiNum = view.findViewById(R.id.tvSheetAqiNum)

        // Observe stations from ViewModel and add colored markers
        viewModel.stations.observe(viewLifecycleOwner) { stations ->
            map.overlays.clear()
            addStationMarkers(stations)
            map.invalidate()
        }
        viewModel.loadStations()

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fabMyLocation
        ).setOnClickListener {
            // Center map on Madrid — user location will be implemented with GPS
            map.controller.setCenter(GeoPoint(40.4168, -3.7038))
            map.controller.setZoom(14.0)
        }
    }

    private fun addStationMarkers(stations: List<Station>) {
        for (station in stations) {
            val marker = Marker(map)
            marker.position = GeoPoint(station.latitude, station.longitude)
            marker.title = station.name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // Color marker icon based on last known AQI
            val colorRes = when (station.lastAqi) {
                1 -> R.color.aqi_good
                2, 3 -> R.color.aqi_moderate
                4 -> R.color.aqi_unhealthy
                5 -> R.color.aqi_hazardous
                else -> R.color.aqi_unknown
            }
            marker.icon?.setTint(
                ContextCompat.getColor(requireContext(), colorRes)
            )

            // On marker tap — show bottom sheet with station details
            marker.setOnMarkerClickListener { _, _ ->
                tvStationName.text = station.name
                val aqi = if (station.lastAqi == -1) "?" else station.lastAqi.toString()
                tvSheetAqiNum.text = aqi
                if (station.lastAqi != -1) {
                    viewSheetAqi.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), colorRes)
                }
                bottomSheetStation.visibility = View.VISIBLE
                true
            }

            map.overlays.add(marker)
        }
        Log.d(TAG, "Added ${stations.size} station markers to map")
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}