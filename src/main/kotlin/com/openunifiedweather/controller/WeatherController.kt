package com.openunifiedweather.controller

import com.openunifiedweather.domain.model.location.model.Location
import com.openunifiedweather.domain.model.source.SourceFeature
import com.openunifiedweather.domain.model.weather.wrappers.WeatherWrapper
import com.openunifiedweather.domain.sources.openmeteo.OpenMeteoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import retrofit2.Retrofit

@RequestMapping("/api/v1/weather")
@RestController
class WeatherController {

    @GetMapping()
    fun getWeather(): ResponseEntity<WeatherWrapper> {
        // TODO: not do this like this of course
        val weatherResult = OpenMeteoService(Retrofit.Builder()).requestWeather(Location(latitude = 52.23019878158841, longitude = 5.37109365845126 ), listOf(
            SourceFeature.FORECAST) ).blockingFirst()
        return ResponseEntity.ok(weatherResult);
    }
}