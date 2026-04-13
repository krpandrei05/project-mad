package com.example.airbus_quest.api

// Model the JSON response from the OpenWeather Air Pollution API.
// { "list": [{ "main": { "aqi": 2 }, "components": { "pm2_5": 10.5 } }] }
data class AirPollutionResponse(val list: List<AirPollutionItem>)

data class AirPollutionItem(
    val main: AirPollutionMain,
    val components: AirPollutionComponents
)

// AQI: 1=Good, 2=Fair, 3=Moderate, 4=Poor, 5=Very Poor
data class AirPollutionMain(val aqi: Int)

data class AirPollutionComponents(
    val pm2_5: Double,
    val pm10: Double,
    val no2: Double,
    val o3: Double
)

// Model the weather endpoint response to get temperature and icon
data class WeatherResponse(
    val main: WeatherMain,
    val weather: List<WeatherCondition>,
    val name: String
)

// Note that OpenWeather returns temperature in Kelvin — I convert it in the ViewModel
data class WeatherMain(val temp: Double)

data class WeatherCondition(
    val description: String,
    val icon: String
)