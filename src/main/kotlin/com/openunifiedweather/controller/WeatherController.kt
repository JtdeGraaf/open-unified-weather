package com.openunifiedweather.controller

import com.openunifiedweather.domain.model.weather.wrappers.WeatherWrapper
import com.openunifiedweather.service.WeatherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/weather")
@RestController
class WeatherController(
    val weatherService: WeatherService
) {

    @GetMapping()
    fun getWeather(
        @RequestParam(required = true) latitude: Double,
        @RequestParam(required = true) longitude: Double,
        @RequestParam(required = false) forecast: Boolean,
        @RequestParam(required = false) current: Boolean,
        @RequestParam(required = false) airQuality: Boolean,
        @RequestParam(required = false) pollen: Boolean,
        @RequestParam(required = false) minutely: Boolean,
        @RequestParam(required = false) normals: Boolean,
        @RequestParam(required = false) reverseGeocoding: Boolean,
    ): ResponseEntity<WeatherWrapper> {
        val result = weatherService.getWeatherForecast(
            latitude = latitude,
            longitude = longitude,
            forecast = forecast,
            current = current,
            airQuality = airQuality,
            pollen = pollen,
            minutely = minutely,
            normals = normals,
            reverseGeocoding = reverseGeocoding,
        )
        return ResponseEntity.ok(result);
    }
}