package com.openunifiedweather.service

import com.openunifiedweather.domain.model.location.model.Location
import com.openunifiedweather.domain.model.source.SourceFeature
import com.openunifiedweather.domain.model.weather.model.Weather
import com.openunifiedweather.domain.model.weather.wrappers.WeatherWrapper
import com.openunifiedweather.domain.sources.openmeteo.OpenMeteoService
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration.Companion.hours

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
        calculateMissingData: Boolean,
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
        var weatherResult = OpenMeteoService(Retrofit.Builder())
            .requestWeather(
                Location(latitude = latitude, longitude = longitude), requestedFeatures
            ).blockingFirst()

        if (calculateMissingData) {
            weatherResult = calculateMissingData(weatherResult, latitude, longitude)
        }

        return weatherResult;
    }


    private fun calculateMissingData(
        pureWeatherResult: WeatherWrapper,
        latitude: Double,
        longitude: Double
    ): WeatherWrapper {


        // 1) Creates hours/days back to yesterday 00:00 if they are missing from the new refresh
//        val weatherWrapperCompleted = completeNewWeatherWithPreviousData(
//            weatherWrapper,
//            location.weather,
//            yesterdayMidnight,
//            location.airQualitySource,
//            location.pollenSource
//        )

        // 2) Computes as many data as possible (weather code, weather text, dew point, feels like temp., etc)
        val hourlyComputedMissingData = computeMissingHourlyData(
            pureWeatherResult.hourlyForecast
        ) ?: emptyList()

        val location = Location(latitude = latitude, longitude = longitude)

        // 3) Create the daily object with air quality/pollen data + computes missing data
        val dailyForecast = completeDailyListFromHourlyList(
            convertDailyWrapperToDailyList(pureWeatherResult),
            hourlyComputedMissingData,
            pureWeatherResult.airQuality?.hourlyForecast ?: emptyMap(),
            pureWeatherResult.pollen?.hourlyForecast ?: emptyMap(),
            pureWeatherResult.pollen?.current,
            location
        )

        // 4) Complete UV and isDaylight + air quality in hourly
        val hourlyForecast = completeHourlyListFromDailyList(
            hourlyComputedMissingData,
            dailyForecast,
            pureWeatherResult.airQuality?.hourlyForecast ?: emptyMap(),
            location
        )

        // Example: 15:01 -> starts at 15:00, 15:59 -> starts at 15:00
        val currentHour = hourlyForecast.firstOrNull {
            it.date.time >= System.currentTimeMillis() - 1.hours.inWholeMilliseconds
        }
        val currentDay = dailyForecast.firstOrNull {
            val zoneId = ZoneId.of(location.timeZone)
            val yesterdayMidnight = Date.from(
                LocalDate.now(zoneId)
                    .minusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant()
            )
            it.date.time >= yesterdayMidnight.time + 23.hours.inWholeMilliseconds
        }

        val weather = Weather(
            current = completeCurrentFromHourlyData(
                pureWeatherResult.current,
                currentHour,
                currentDay,
                pureWeatherResult.airQuality?.current,
                location
            ),
            normals = completeNormalsFromDaily(pureWeatherResult.normals, dailyForecast),
            dailyForecast = dailyForecast,
            hourlyForecast = hourlyForecast,
            minutelyForecast = pureWeatherResult.minutelyForecast ?: emptyList(),
            alertList = pureWeatherResult.alertList ?: emptyList()
        )

        return weather.toWeatherWrapper()
    }
}