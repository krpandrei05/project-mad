package com.example.airbus_quest.api

data class EmtLoginResponse(
    val code: String,
    val description: String,
    val data: List<EmtLoginData>?
)

data class EmtLoginData(
    val accessToken: String?,
    val username: String?
)

// arroundxy returns data directly as array — no wrapper object
data class EmtAroundXYResponse(
    val code: String,
    val description: String?,
    val data: List<EmtNearbyStop>?
)

data class EmtNearbyStop(
    val stopId: Int,
    val stopName: String,
    val metersToPoint: Int,
    val geometry: EmtStopGeometry,
    val lines: List<EmtStopLine>?
)

data class EmtStopGeometry(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude]
)

data class EmtStopLine(
    val label: String,
    val nameA: String?,
    val nameB: String?
)