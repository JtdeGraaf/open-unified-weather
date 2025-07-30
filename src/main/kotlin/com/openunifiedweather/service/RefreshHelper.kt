///**
// * This file is part of Breezy Weather.
// *
// * Breezy Weather is free software: you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as published by the
// * Free Software Foundation, version 3 of the License.
// *
// * Breezy Weather is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
// * License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
// */
//
//package com.openunifiedweather.service
//
//import com.openunifiedweather.common.extensions.getIsoFormattedDate
//import com.openunifiedweather.domain.model.location.model.*
//import com.openunifiedweather.domain.model.source.*
//import com.openunifiedweather.domain.model.weather.model.Base
//import com.openunifiedweather.domain.model.weather.wrappers.WeatherWrapper
//import java.util.*
//import java.util.concurrent.Semaphore
//import kotlin.time.Duration.Companion.days
//import kotlin.time.Duration.Companion.hours
//
//class RefreshHelper() {
////    suspend fun getLocation(
////        location: Location,
////        background: Boolean,
////    ): LocationResult {
////        if (!location.isCurrentPosition) {
////            return LocationResult(location)
////        }
////
////        var currentErrors = mutableListOf<RefreshError>()
////
////        // Getting current longitude and latitude
////        val currentLocation = try {
////            requestCurrentLocation(context, location, background)
////                .also { currentErrors = it.errors.toMutableList() }
////                .location
////        } catch (e: Throwable) {
////            e.printStackTrace()
////            currentErrors.add(RefreshError(RefreshErrorType.LOCATION_FAILED))
////            location
////        }
////
////        // Longitude and latitude incorrect? Let’s return earlier
////        if (!currentLocation.isUsable) {
////            return LocationResult(currentLocation, currentErrors)
////        }
////
////        val locationGeocoded = if (
////            location.longitude != currentLocation.longitude ||
////            location.latitude != currentLocation.latitude ||
////            location.needsGeocodeRefresh
////        ) {
////            val reverseGeocodingService = sourceManager.getReverseGeocodingSourceOrDefault(
////                location.reverseGeocodingSource ?: BuildConfig.DEFAULT_GEOCODING_SOURCE
////            )
////            try {
////                // Getting the address for this
////                requestReverseGeocoding(reverseGeocodingService, currentLocation, context).also {
////                    locationRepository.update(it)
////                }
////            } catch (e: Throwable) {
////                currentErrors.add(
////                    RefreshError(
////                        RefreshErrorType.getTypeFromThrowable(context, e, RefreshErrorType.REVERSE_GEOCODING_FAILED),
////                        reverseGeocodingService.name,
////                        SourceFeature.REVERSE_GEOCODING
////                    )
////                )
////
////                // Fallback to offline reverse geocoding
////                if (reverseGeocodingService.id != BuildConfig.DEFAULT_GEOCODING_SOURCE) {
////                    val defaultReverseGeocodingSource = sourceManager.getReverseGeocodingSourceOrDefault(
////                        BuildConfig.DEFAULT_GEOCODING_SOURCE
////                    )
////                    try {
////                        // Getting the address for this from the fallback reverse geocoding source
////                        requestReverseGeocoding(defaultReverseGeocodingSource, currentLocation, context).also {
////                            locationRepository.update(it)
////                        }
////                    } catch (e: Throwable) {
////                        /**
////                         * Returns the original location
////                         * Previously, we used to return the new coordinates without the reverse geocoding,
////                         * leading to issues when reverse geocoding fails (because the mandatory countryCode
////                         * -for some sources- would be missing)
////                         * However, if both the reverse geocoding source + the offline fallback reverse geocoding source
////                         * are failing, it safes to assume that the longitude and latitude are completely junky and
////                         * should be discarded
////                         */
////                        location
////                    }
////                } else {
////                    /**
////                     * Returns the original location
////                     * Same comment as above
////                     */
////                    location
////                }
////            }
////        } else {
////            // If no need for reverse geocoding, just return the current location which already has the info
////            currentLocation // Same as "location"
////        }
////        return LocationResult(locationGeocoded, currentErrors)
////    }
////
////    private suspend fun requestCurrentLocation(
////        location: Location,
////        background: Boolean,
////    ): LocationResult {
////        val locationSource = SettingsManager.getInstance(context).locationSource
////        val locationService = sourceManager.getLocationSourceOrDefault(locationSource)
////        val errors = mutableListOf<RefreshError>()
////        if (!context.isOnline()) {
////            errors.add(RefreshError(RefreshErrorType.NETWORK_UNAVAILABLE))
////        }
////        if (locationService.permissions.isNotEmpty()) {
////            // if needs any location permission.
////            if (!context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
////                !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
////            ) {
////                errors.add(RefreshError(RefreshErrorType.ACCESS_LOCATION_PERMISSION_MISSING))
////            }
////            if (background) {
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
////                    !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
////                ) {
////                    errors.add(RefreshError(RefreshErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING))
////                }
////            }
////        }
////        if (!LocationManagerCompat.isLocationEnabled(context.locationManager)) {
////            errors.add(RefreshError(RefreshErrorType.LOCATION_ACCESS_OFF))
////        }
////        if (errors.isNotEmpty()) {
////            return LocationResult(location, errors)
////        }
////
////        return try {
////            if (locationService is ConfigurableSource && !locationService.isConfigured) {
////                throw ApiKeyMissingException()
////            }
////            val result = locationService
////                .requestLocation(context)
////                .awaitFirstOrElse {
////                    throw LocationException()
////                }
////            val newLongitude = result.longitude.roundDecimals(6)!!
////            val newLatitude = result.latitude.roundDecimals(6)!!
////            return LocationResult(
////                if (newLatitude != location.latitude || newLongitude != location.longitude) {
////                    location.copy(
////                        latitude = newLatitude,
////                        longitude = newLongitude,
////                        timeZone = result.timeZone ?: location.timeZone,
////                        /*
////                         * Don’t keep old data as the user can have changed position
////                         * It avoids keeping old data from a reverse geocoding-compatible weather source
////                         * onto a weather source without reverse geocoding
////                         */
////                        country = result.country ?: "",
////                        countryCode = result.countryCode ?: "",
////                        admin1 = result.admin1 ?: "",
////                        admin1Code = result.admin1Code ?: "",
////                        admin2 = result.admin2 ?: "",
////                        admin2Code = result.admin2Code ?: "",
////                        admin3 = result.admin3 ?: "",
////                        admin3Code = result.admin3Code ?: "",
////                        admin4 = result.admin4 ?: "",
////                        admin4Code = result.admin4Code ?: "",
////                        city = result.city ?: "",
////                        district = result.district ?: ""
////                    )
////                } else {
////                    // Return as-is without overwriting reverse geocoding info
////                    location
////                }
////            )
////        } catch (e: Throwable) {
////            LocationResult(
////                location,
////                errors = listOf(
////                    RefreshError(
////                        RefreshErrorType.getTypeFromThrowable(context, e, RefreshErrorType.LOCATION_FAILED),
////                        locationService.name
////                    )
////                )
////            )
////        }
////    }
////
////    private suspend fun requestReverseGeocoding(
////        reverseGeocodingService: ReverseGeocodingSource,
////        currentLocation: Location,
////    ): Location {
////        if (reverseGeocodingService is ConfigurableSource && !reverseGeocodingService.isConfigured) {
////            throw ApiKeyMissingException()
////        }
////
////        return reverseGeocodingService
////            .requestReverseGeocodingLocation(context, currentLocation)
////            .map { locationList ->
////                if (locationList.isNotEmpty()) {
////                    val result = locationList[0]
////                    currentLocation.copy(
////                        cityId = result.cityId,
////                        timeZone = result.timeZone,
////                        country = result.country,
////                        countryCode = result.countryCode ?: "",
////                        admin1 = result.admin1 ?: "",
////                        admin1Code = result.admin1Code ?: "",
////                        admin2 = result.admin2 ?: "",
////                        admin2Code = result.admin2Code ?: "",
////                        admin3 = result.admin3 ?: "",
////                        admin3Code = result.admin3Code ?: "",
////                        admin4 = result.admin4 ?: "",
////                        admin4Code = result.admin4Code ?: "",
////                        city = result.city,
////                        district = result.district ?: "",
////                        needsGeocodeRefresh = false
////                    )
////                } else {
////                    throw ReverseGeocodingException()
////                }
////            }.awaitFirstOrElse {
////                throw ReverseGeocodingException()
////            }
////    }
//
//
//    suspend fun getWeather(
//        location: Location,
//        coordinatesChanged: Boolean,
//    ): WeatherResult {
//        try {
//            if (!location.isUsable || location.needsGeocodeRefresh) {
//                return WeatherResult(
//                    location.weather,
//                )
//            }
//
//            // Group data requested to sources by source
//            val featuresBySources: MutableMap<String, MutableList<SourceFeature>> = mutableMapOf()
//            with(location) {
//                listOf(
//                    Pair(forecastSource, SourceFeature.FORECAST),
//                    Pair(currentSource, SourceFeature.CURRENT),
//                    Pair(airQualitySource, SourceFeature.AIR_QUALITY),
//                    Pair(pollenSource, SourceFeature.POLLEN),
//                    Pair(minutelySource, SourceFeature.MINUTELY),
//                    Pair(alertSource, SourceFeature.ALERT),
//                    Pair(normalsSource, SourceFeature.NORMALS)
//                ).forEach {
//                    if (!it.first.isNullOrEmpty()) {
//                        if (featuresBySources.containsKey(it.first)) {
//                            featuresBySources[it.first]!!.add(it.second)
//                        } else {
//                            featuresBySources[it.first!!] = mutableListOf(it.second)
//                        }
//                    }
//                }
//            }
//
//            // Always update refresh time displayed to the user, even if just re-using cached data
//            val base = location.weather?.base?.copy(
//                refreshTime = Date()
//            ) ?: Base(
//                refreshTime = Date()
//            )
//
//            val locationParameters = location.parameters.toMutableMap()
//
//            // COMPLETE BACK TO YESTERDAY 00:00 MAX
//            // TODO: Use Calendar to handle DST
//            val yesterdayMidnight = Date(Date().time - 1.days.inWholeMilliseconds)
//                .getIsoFormattedDate(location)
//                .toDateNoHour(location.javaTimeZone)!!
//            var forecastUpdateTime = base.forecastUpdateTime
//            var currentUpdateTime = base.currentUpdateTime
//            var airQualityUpdateTime = base.airQualityUpdateTime
//            var pollenUpdateTime = base.pollenUpdateTime
//            var minutelyUpdateTime = base.minutelyUpdateTime
//            var alertsUpdateTime = base.alertsUpdateTime
//            var normalsUpdateTime = base.normalsUpdateTime
//
//
//            val weatherWrapper = if (featuresBySources.isNotEmpty()) {
//                val semaphore = Semaphore(5)
//                val sourceCalls = mutableMapOf<String, WeatherWrapper?>()
//                coroutineScope {
//                    featuresBySources
//                        .map { entry ->
//                            async {
//                                semaphore.withPermit {
//                                    val service = sourceManager.getWeatherSource(entry.key)
//                                    if (service == null) {
//                                        errors.add(RefreshError(RefreshErrorType.SOURCE_NOT_INSTALLED, entry.key))
//                                    } else {
//                                        val featuresToUpdate = entry.value
//                                            .filter {
//                                                // Remove sources that are not configured
//                                                if (service is ConfigurableSource && !service.isConfigured) {
//                                                    errors.add(
//                                                        RefreshError(
//                                                            RefreshErrorType.API_KEY_REQUIRED_MISSING,
//                                                            entry.key
//                                                        )
//                                                    )
//                                                    false
//                                                } else {
//                                                    true
//                                                }
//                                            }
//                                            .filter {
//                                                // Remove sources that no longer supports the feature
//                                                if (!service.supportedFeatures.containsKey(it)) {
//                                                    errors.add(
//                                                        RefreshError(
//                                                            RefreshErrorType.UNSUPPORTED_FEATURE,
//                                                            entry.key
//                                                        )
//                                                    )
//                                                    false
//                                                } else {
//                                                    true
//                                                }
//                                            }
//                                            .filter {
//                                                // Remove sources that no longer supports the feature for that location
//                                                if (!service.isFeatureSupportedForLocation(location, it)) {
//                                                    errors.add(
//                                                        RefreshError(
//                                                            RefreshErrorType.UNSUPPORTED_FEATURE,
//                                                            entry.key
//                                                        )
//                                                    )
//                                                    false
//                                                } else {
//                                                    true
//                                                }
//                                            }
//                                            .filter {
//                                                service !is HttpSource ||
//                                                        !isWeatherDataStillValid(
//                                                            location,
//                                                            it,
//                                                            isRestricted = !BreezyWeather.instance.debugMode &&
//                                                                    service is ConfigurableSource &&
//                                                                    service.isRestricted,
//                                                            minimumTime = languageUpdateTime
//                                                        )
//                                            }
//                                        if (featuresToUpdate.isEmpty()) {
//                                            // Setting to null will make it use previous data
//                                            sourceCalls[entry.key] = null
//                                        } else {
//                                            sourceCalls[entry.key] = try {
//                                                if (service is LocationParametersSource &&
//                                                    service.needsLocationParametersRefresh(
//                                                        location,
//                                                        coordinatesChanged,
//                                                        featuresToUpdate
//                                                    )
//                                                ) {
//                                                    locationParameters[service.id] = buildMap {
//                                                        if (locationParameters.getOrElse(service.id) { null } != null) {
//                                                            putAll(locationParameters[service.id]!!)
//                                                        }
//                                                        putAll(
//                                                            service
//                                                                .requestLocationParameters(context, location.copy())
//                                                                .awaitFirstOrElse {
//                                                                    throw WeatherException()
//                                                                }
//                                                        )
//                                                    }
//                                                }
//                                                service
//                                                    .requestWeather(
//                                                        context,
//                                                        location.copy(parameters = locationParameters),
//                                                        featuresToUpdate
//                                                    ).awaitFirstOrElse {
//                                                        featuresToUpdate.forEach {
//                                                            errors.add(
//                                                                RefreshError(
//                                                                    RefreshErrorType.DATA_REFRESH_FAILED,
//                                                                    entry.key,
//                                                                    it
//                                                                )
//                                                            )
//                                                        }
//                                                        null
//                                                    }
//                                            } catch (e: Throwable) {
//                                                e.printStackTrace()
//                                                featuresToUpdate.forEach {
//                                                    errors.add(
//                                                        RefreshError(
//                                                            RefreshErrorType.getTypeFromThrowable(
//                                                                context,
//                                                                e,
//                                                                RefreshErrorType.DATA_REFRESH_FAILED
//                                                            ),
//                                                            entry.key,
//                                                            it
//                                                        )
//                                                    )
//                                                }
//                                                null
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }.awaitAll()
//                }
//
//                for ((k, v) in sourceCalls) {
//                    v?.failedFeatures?.entries?.forEach { entry ->
//                        errors.add(
//                            RefreshError(
//                                RefreshErrorType.getTypeFromThrowable(
//                                    context,
//                                    entry.value,
//                                    RefreshErrorType.DATA_REFRESH_FAILED
//                                ),
//                                k,
//                                entry.key
//                            )
//                        )
//                    }
//                }
//
//                /**
//                 * Make sure we return data from the correct source
//                 */
//                WeatherWrapper(
//                    dailyForecast = if (location.forecastSource.isNotEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.FORECAST &&
//                                        it.source == location.forecastSource
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.forecastSource) { null }?.dailyForecast?.let {
//                                if (it.isEmpty()) {
//                                    errors.add(
//                                        RefreshError(
//                                            RefreshErrorType.INVALID_INCOMPLETE_DATA,
//                                            location.forecastSource,
//                                            SourceFeature.FORECAST
//                                        )
//                                    )
//                                    null
//                                } else {
//                                    forecastUpdateTime = Date()
//                                    it
//                                }
//                            }
//                        }
//                    } else {
//                        null
//                    } ?: location.weather?.toDailyWrapperList(yesterdayMidnight),
//                    hourlyForecast = if (location.forecastSource.isNotEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.FORECAST &&
//                                        it.source == location.forecastSource
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.forecastSource) { null }?.hourlyForecast
//                        }
//                    } else {
//                        null
//                    } ?: location.weather?.toHourlyWrapperList(yesterdayMidnight),
//                    current = if (!location.currentSource.isNullOrEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.CURRENT &&
//                                        it.source == location.currentSource!!
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.currentSource!!) { null }?.current?.let {
//                                currentUpdateTime = Date()
//                                it
//                            }
//                        }
//                    } else {
//                        null
//                    }, // Doesn't fallback to old current, as we will use forecast instead later
//                    airQuality = if (!location.airQualitySource.isNullOrEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.AIR_QUALITY &&
//                                        it.source == location.airQualitySource!!
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.airQualitySource!!) { null }?.airQuality?.let {
//                                airQualityUpdateTime = Date()
//                                it
//                            }
//                        } ?: location.weather?.toAirQualityWrapperList(yesterdayMidnight)
//                    } else {
//                        null
//                    },
//                    pollen = if (!location.pollenSource.isNullOrEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.POLLEN &&
//                                        it.source == location.pollenSource!!
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.pollenSource!!) { null }?.pollen?.let {
//                                pollenUpdateTime = Date()
//                                it
//                            }
//                        } ?: location.weather?.toPollenWrapperList(yesterdayMidnight)
//                    } else {
//                        null
//                    },
//                    minutelyForecast = if (!location.minutelySource.isNullOrEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.MINUTELY &&
//                                        it.source == location.minutelySource!!
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.minutelySource!!) { null }?.minutelyForecast?.let {
//                                minutelyUpdateTime = Date()
//                                it
//                            }
//                        } ?: location.weather?.toMinutelyWrapper()
//                    } else {
//                        null
//                    },
//                    alertList = if (!location.alertSource.isNullOrEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.ALERT &&
//                                        it.source == location.alertSource!!
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.alertSource!!) { null }?.alertList?.let {
//                                alertsUpdateTime = Date()
//                                it
//                            }
//                        } ?: location.weather?.toAlertsWrapper()
//                    } else {
//                        null
//                    },
//                    normals = if (!location.normalsSource.isNullOrEmpty()) {
//                        if (errors.any {
//                                it.feature == SourceFeature.NORMALS &&
//                                        it.source == location.normalsSource!!
//                            }
//                        ) {
//                            null
//                        } else {
//                            sourceCalls.getOrElse(location.normalsSource!!) { null }?.normals?.let {
//                                normalsUpdateTime = Date()
//                                it
//                            }
//                        } ?: getNormalsFromWeather(location)
//                    } else {
//                        null
//                    }
//                )
//            } else {
//                return WeatherResult(
//                    location.weather,
//                    listOf(RefreshError(RefreshErrorType.INVALID_LOCATION))
//                )
//            }
//
//            // COMPLETING DATA
//
//            // 1) Creates hours/days back to yesterday 00:00 if they are missing from the new refresh
//            val weatherWrapperCompleted = completeNewWeatherWithPreviousData(
//                weatherWrapper,
//                location.weather,
//                yesterdayMidnight,
//                location.airQualitySource,
//                location.pollenSource
//            )
//
//            // 2) Computes as many data as possible (weather code, weather text, dew point, feels like temp., etc)
//            val hourlyComputedMissingData = computeMissingHourlyData(
//                weatherWrapperCompleted.hourlyForecast
//            ) ?: emptyList()
//
//            // 3) Create the daily object with air quality/pollen data + computes missing data
//            val dailyForecast = completeDailyListFromHourlyList(
//                convertDailyWrapperToDailyList(weatherWrapperCompleted),
//                hourlyComputedMissingData,
//                weatherWrapperCompleted.airQuality?.hourlyForecast ?: emptyMap(),
//                weatherWrapperCompleted.pollen?.hourlyForecast ?: emptyMap(),
//                weatherWrapperCompleted.pollen?.current,
//                location
//            )
//
//            // 4) Complete UV and isDaylight + air quality in hourly
//            val hourlyForecast = completeHourlyListFromDailyList(
//                hourlyComputedMissingData,
//                dailyForecast,
//                weatherWrapperCompleted.airQuality?.hourlyForecast ?: emptyMap(),
//                location
//            )
//
//            // Example: 15:01 -> starts at 15:00, 15:59 -> starts at 15:00
//            val currentHour = hourlyForecast.firstOrNull {
//                it.date.time >= System.currentTimeMillis() - 1.hours.inWholeMilliseconds
//            }
//            val currentDay = dailyForecast.firstOrNull {
//                // Adding 23 hours just to be safe in case of DST
//                it.date.time >= yesterdayMidnight.time + 23.hours.inWholeMilliseconds
//            }
//
//            val weather = Weather(
//                base = base.copy(
//                    forecastUpdateTime = forecastUpdateTime,
//                    currentUpdateTime = currentUpdateTime,
//                    airQualityUpdateTime = airQualityUpdateTime,
//                    pollenUpdateTime = pollenUpdateTime,
//                    minutelyUpdateTime = minutelyUpdateTime,
//                    alertsUpdateTime = alertsUpdateTime,
//                    normalsUpdateTime = normalsUpdateTime
//                ),
//                current = completeCurrentFromHourlyData(
//                    weatherWrapperCompleted.current,
//                    currentHour,
//                    currentDay,
//                    weatherWrapperCompleted.airQuality?.current,
//                    location
//                ),
//                normals = completeNormalsFromDaily(weatherWrapperCompleted.normals, dailyForecast),
//                dailyForecast = dailyForecast,
//                hourlyForecast = hourlyForecast,
//                minutelyForecast = weatherWrapperCompleted.minutelyForecast ?: emptyList(),
//                alertList = weatherWrapperCompleted.alertList ?: emptyList()
//            )
//            locationRepository.insertParameters(location.formattedId, locationParameters)
//            weatherRepository.insert(location, weather)
//            return WeatherResult(weather, errors)
//        } catch (e: Throwable) {
//            e.printStackTrace()
//            return WeatherResult(
//                location.weather,
//                listOf(RefreshError(RefreshErrorType.DATA_REFRESH_FAILED))
//            )
//        }
//    }
//
//
//    companion object {
//        private const val WAIT_MINIMUM = 1
//        private const val WAIT_REGULAR = 5
//        private const val WAIT_RESTRICTED = 15
//        private const val WAIT_ONE_HOUR = 60
//
//        const val WAIT_MAIN = WAIT_REGULAR // 5 min
//        const val WAIT_MAIN_RESTRICTED = WAIT_RESTRICTED // 15 min
//        const val WAIT_CURRENT = WAIT_MINIMUM // 1 min
//        const val WAIT_CURRENT_RESTRICTED = WAIT_RESTRICTED // 15 min
//        const val WAIT_AIR_QUALITY = WAIT_REGULAR // 5 min
//        const val WAIT_AIR_QUALITY_RESTRICTED = WAIT_ONE_HOUR // 1 hour
//        const val WAIT_POLLEN = WAIT_REGULAR // 5 min
//        const val WAIT_POLLEN_RESTRICTED = WAIT_ONE_HOUR // 1 hour
//        const val WAIT_MINUTELY = WAIT_REGULAR // 5 min
//        const val WAIT_MINUTELY_ONGOING = WAIT_MINIMUM // 1 min
//        const val WAIT_MINUTELY_RESTRICTED = WAIT_RESTRICTED // 15 min
//        const val WAIT_MINUTELY_RESTRICTED_ONGOING = WAIT_REGULAR // 5 min
//        const val WAIT_ALERTS = WAIT_REGULAR // 5 min
//        const val WAIT_ALERTS_ONGOING = WAIT_MINIMUM // 1 min
//        const val WAIT_ALERTS_RESTRICTED = WAIT_ONE_HOUR // 1 hour
//        const val WAIT_ALERTS_RESTRICTED_ONGOING = WAIT_REGULAR // 5 min
//        const val WAIT_NORMALS_CURRENT = WAIT_REGULAR // 5 min
//        const val WAIT_NORMALS_CURRENT_RESTRICTED = WAIT_RESTRICTED // 15 min
//    }
//}
