package com.example.airbus_quest

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.airbus_quest.viewmodel.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class MapFragment : Fragment() {
    private val TAG = "MapFragment"
    private lateinit var map: MapView
    private val viewModel: MapViewModel by viewModels()

    private val campusMap = mapOf(
        "Tennis" to GeoPoint(40.38779608214728, -3.627687914352839),
        "Futsal outdoors" to GeoPoint(40.38788595319803, -3.627048250272035),
        "Fashion and design school" to GeoPoint(40.3887315224542, -3.628643539758645),
        "Topography school" to GeoPoint(40.38926842612264, -3.630067893975619),
        "Telecommunications school" to GeoPoint(40.38956358584258, -3.629046081389352),
        "ETSISI" to GeoPoint(40.38992125672989, -3.6281366497769714),
        "Library" to GeoPoint(40.39037466191718, -3.6270256763598447),
        "CITSEM" to GeoPoint(40.389855884803005, -3.626782180787362)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Map ready")

        Configuration.getInstance().userAgentValue = "com.example.airbus_quest"
        Configuration.getInstance().load(
            requireContext().applicationContext,
            requireContext().getSharedPreferences("osm", Context.MODE_PRIVATE)
        )

        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(18.0)

        val startPoint = GeoPoint(40.38992125672989, -3.6281366497769714)
        map.controller.setCenter(startPoint)

        val myMarker = Marker(map)
        myMarker.position = startPoint
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        myMarker.icon = ContextCompat.getDrawable(
            requireContext(), android.R.drawable.star_on
        ) as BitmapDrawable
        myMarker.title = "My current location"

        map.overlays.add(myMarker)

        addRouteMarkers()

        // Observe stations from ViewModel — cached after first load
        viewModel.stations.observe(viewLifecycleOwner) { stations ->
            Log.d(TAG, "Stations available: ${stations.size}")
            // I will use these in Commit 4 for AQI markers
        }
        viewModel.loadStations()

        Log.d(TAG, "Map configured with ${campusMap.size} markers")
    }

    private fun addRouteMarkers() {
        val polyline = Polyline()
        polyline.setPoints(campusMap.values.toList())
        polyline.outlinePaint.color = Color.BLUE
        polyline.outlinePaint.strokeWidth = 10f

        map.overlays.add(polyline)

        for ((name, point) in campusMap) {
            val marker = Marker(map)
            marker.position = point
            marker.title = name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.icon = ContextCompat.getDrawable(
                requireContext(), android.R.drawable.ic_menu_compass
            ) as BitmapDrawable
            map.overlays.add(marker)
        }
        map.invalidate()
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