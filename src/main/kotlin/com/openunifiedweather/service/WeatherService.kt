package com.openunifiedweather.service

import com.openunifiedweather.domain.model.location.model.Location
import com.openunifiedweather.domain.model.source.SourceFeature
import com.openunifiedweather.domain.model.weather.wrappers.WeatherWrapper
import com.openunifiedweather.domain.sources.openmeteo.OpenMeteoService
import org.springframework.stereotype.Service
import retrofit2.Retrofit

@Service
class WeatherService {

    fun getWeatherForecast(
        latitude: Double,
        longitude: Double,
        forecast: Boolean,
        current: Boolean,
        airQuality: Boolean,
        pollen: Boolean,
        minutely: Boolean,
        normals: Boolean,
        reverseGeocoding: Boolean,
    ): WeatherWrapper {
        val requestedFeatures = mutableListOf<SourceFeature>()
        if (forecast) requestedFeatures.add(SourceFeature.FORECAST)
        if (current) requestedFeatures.add(SourceFeature.CURRENT)
        if (airQuality) requestedFeatures.add(SourceFeature.AIR_QUALITY)
        if (pollen) requestedFeatures.add(SourceFeature.POLLEN)
        if (minutely) requestedFeatures.add(SourceFeature.MINUTELY)
        if (normals) requestedFeatures.add(SourceFeature.NORMALS)
        if (reverseGeocoding) requestedFeatures.add(SourceFeature.REVERSE_GEOCODING)

        // TODO: Factory for different sources
        val weatherResult = OpenMeteoService(Retrofit.Builder())
            .requestWeather(
                Location(latitude = latitude, longitude = longitude), requestedFeatures
            ).blockingFirst()

        return weatherResult;
    }
}