package com.example.airbus_quest.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface EmtService {

    // Login with X-ClientId + passKey
    @GET("v1/mobilitylabs/user/login/")
    fun login(
        @Header("email") email: String,
        @Header("password") password: String
    ): Call<EmtLoginResponse>

    // Get stops around a GPS point within a radius
    @GET("v2/transport/busemtmad/stops/arroundxy/{longitude}/{latitude}/{radius}/")
    fun getStopsAroundLocation(
        @Header("accessToken") token: String,
        @Path("longitude") longitude: Double,
        @Path("latitude") latitude: Double,
        @Path("radius") radius: Int
    ): Call<EmtAroundXYResponse>
}