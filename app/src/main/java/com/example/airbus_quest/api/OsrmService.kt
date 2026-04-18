package com.example.airbus_quest.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface OsrmService {
    // Returns a driving route between two coordinates
    @GET("route/v1/driving/{startLon},{startLat};{endLon},{endLat}?overview=full&geometries=geojson")
    fun getRoute(
        @Path("startLon") startLon: Double,
        @Path("startLat") startLat: Double,
        @Path("endLon") endLon: Double,
        @Path("endLat") endLat: Double
    ): Call<OsrmResponse>
}

data class OsrmResponse(val routes: List<OsrmRoute>)
data class OsrmRoute(val geometry: OsrmGeometry)
data class OsrmGeometry(val coordinates: List<List<Double>>)