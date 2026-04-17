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
                val aqi = if (station.lastAqi == -1) "?" else station.lastAqi.toString()
                tvSheetAqiNum.text = aqi
                if (station.lastAqi != -1) {
                    viewSheetAqi.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), colorRes)
                } else {
                    viewSheetAqi.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.aqi_unknown)
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