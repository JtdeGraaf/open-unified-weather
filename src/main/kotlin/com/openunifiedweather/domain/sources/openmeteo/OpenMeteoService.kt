/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package com.openunifiedweather.domain.sources.openmeteo

import com.openunifiedweather.common.exceptions.LocationSearchException
import io.reactivex.rxjava3.core.Observable

import com.openunifiedweather.domain.interfaces.HttpSource
import com.openunifiedweather.domain.interfaces.LocationSearchSource
import com.openunifiedweather.domain.interfaces.WeatherSource
import com.openunifiedweather.domain.model.location.model.Location
import com.openunifiedweather.domain.model.source.SourceContinent
import com.openunifiedweather.domain.model.source.SourceFeature
import com.openunifiedweather.domain.model.weather.wrappers.WeatherWrapper

import com.openunifiedweather.domain.sources.openmeteo.json.OpenMeteoAirQualityResult
import com.openunifiedweather.domain.sources.openmeteo.json.OpenMeteoWeatherResult
import org.breezyweather.common.exceptions.InvalidLocationException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

class OpenMeteoService (
    val client: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationSearchSource {

    override val id = "openmeteo"
    override val name = "Open-Meteo"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://open-meteo.com/en/terms#privacy"

    override val locationSearchAttribution = "Open-Meteo (CC BY 4.0) • GeoNames"

    private val mForecastApi: OpenMeteoForecastApi
        get() {
            return client
                .baseUrl(OPEN_METEO_FORECAST_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(OpenMeteoForecastApi::class.java)
        }
    private val mGeocodingApi: OpenMeteoGeocodingApi
        get() {
            return client
                .baseUrl(OPEN_METEO_GEOCODING_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(OpenMeteoGeocodingApi::class.java)
        }
    private val mAirQualityApi: OpenMeteoAirQualityApi
        get() {
            return client
                .baseUrl(OPEN_METEO_AIR_QUALITY_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(OpenMeteoAirQualityApi::class.java)
        }

    private val weatherAttribution = "Open-Meteo (CC BY 4.0)"
    private val airQualityAttribution = "Open-Meteo (CC BY 4.0) • CAMS ENSEMBLE data provider"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to airQualityAttribution,
        SourceFeature.POLLEN to airQualityAttribution,
        SourceFeature.MINUTELY to weatherAttribution
    )
    override val attributionLinks = mapOf(
        name to "https://open-meteo.com/",
        "CAMS ENSEMBLE data provider" to "https://confluence.ecmwf.int/display/CKB/" +
            "CAMS+Regional%3A+European+air+quality+analysis+and+forecast+data+documentation/" +
            "#CAMSRegional:Europeanairqualityanalysisandforecastdatadocumentation-" +
            "Howtoacknowledge,citeandrefertothedata"
    )
    override fun requestWeather(
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val weather = if (SourceFeature.FORECAST in requestedFeatures ||
            SourceFeature.MINUTELY in requestedFeatures ||
            SourceFeature.CURRENT in requestedFeatures
        ) {
            val daily = arrayOf(
                "temperature_2m_max",
                "temperature_2m_min",
                "apparent_temperature_max",
                "apparent_temperature_min",
                "sunshine_duration",
                "uv_index_max"
            )
            val hourly = arrayOf(
                "temperature_2m",
                "apparent_temperature",
                "precipitation_probability",
                "precipitation",
                "rain",
                "showers",
                "snowfall",
                "weathercode",
                "windspeed_10m",
                "winddirection_10m",
                "windgusts_10m",
                "uv_index",
                "is_day",
                "relativehumidity_2m",
                "dewpoint_2m",
                "pressure_msl",
                "cloudcover",
                "visibility"
            )
            val current = arrayOf(
                "temperature_2m",
                "apparent_temperature",
                "weathercode",
                "windspeed_10m",
                "winddirection_10m",
                "windgusts_10m",
                "uv_index",
                "relativehumidity_2m",
                "dewpoint_2m",
                "pressure_msl",
                "cloudcover",
                "visibility"
            )
            val minutely = arrayOf(
                // "precipitation_probability",
                "precipitation"
            )

            mForecastApi.getWeather(
                location.latitude,
                location.longitude,
                getWeatherModels(location).joinToString(",") { it.id },
                if (SourceFeature.FORECAST in requestedFeatures) {
                    daily.joinToString(",")
                } else {
                    ""
                },
                if (SourceFeature.FORECAST in requestedFeatures) {
                    hourly.joinToString(",")
                } else {
                    ""
                },
                if (SourceFeature.MINUTELY in requestedFeatures) {
                    minutely.joinToString(",")
                } else {
                    ""
                },
                if (SourceFeature.CURRENT in requestedFeatures) {
                    current.joinToString(",")
                } else {
                    ""
                },
                forecastDays = 16,
                pastDays = 1,
                windspeedUnit = "ms"
            ).onErrorResumeNext {
                if (it is HttpException &&
                    it.response()?.errorBody()?.string()
                        ?.contains("No data is available for this location") == true
                ) {
                    // Happens when user choose a model that doesn’t cover their location
                    Observable.error(InvalidLocationException())
                } else {
                    if (SourceFeature.FORECAST in requestedFeatures) {
                        failedFeatures[SourceFeature.FORECAST] = it
                    }
                    if (SourceFeature.MINUTELY in requestedFeatures) {
                        failedFeatures[SourceFeature.MINUTELY] = it
                    }
                    if (SourceFeature.CURRENT in requestedFeatures) {
                        failedFeatures[SourceFeature.CURRENT] = it
                    }
                    Observable.just(OpenMeteoWeatherResult())
                }
            }
        } else {
            Observable.just(OpenMeteoWeatherResult())
        }

        val aqi = if (SourceFeature.AIR_QUALITY in requestedFeatures ||
            SourceFeature.POLLEN in requestedFeatures
        ) {
            val airQualityHourly = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                arrayOf(
                    "pm10",
                    "pm2_5",
                    "carbon_monoxide",
                    "nitrogen_dioxide",
                    "sulphur_dioxide",
                    "ozone"
                )
            } else {
                arrayOf()
            }
            val pollenHourly = if (SourceFeature.POLLEN in requestedFeatures) {
                arrayOf(
                    "alder_pollen",
                    "birch_pollen",
                    "grass_pollen",
                    "mugwort_pollen",
                    "olive_pollen",
                    "ragweed_pollen"
                )
            } else {
                arrayOf()
            }
            val airQualityPollenHourly = airQualityHourly + pollenHourly
            mAirQualityApi.getAirQuality(
                location.latitude,
                location.longitude,
                airQualityPollenHourly.joinToString(","),
                forecastDays = 7,
                pastDays = 1
            ).onErrorResumeNext {
                if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    failedFeatures[SourceFeature.AIR_QUALITY] = it
                }
                if (SourceFeature.POLLEN in requestedFeatures) {
                    failedFeatures[SourceFeature.POLLEN] = it
                }
                Observable.just(OpenMeteoAirQualityResult())
            }
        } else {
            Observable.just(OpenMeteoAirQualityResult())
        }
        return Observable.zip(
            weather,
            aqi
        ) { weatherResult: OpenMeteoWeatherResult, airQualityResult: OpenMeteoAirQualityResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(weatherResult.daily, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(weatherResult.hourly)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(weatherResult.current)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    getAirQuality(airQualityResult.hourly)
                } else {
                    null
                },
                pollen = if (SourceFeature.POLLEN in requestedFeatures) {
                    getPollen(airQualityResult.hourly)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(weatherResult.minutelyFifteen)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Location
    override fun requestLocationSearch(
        query: String,
    ): Observable<List<Location>> {
        return mGeocodingApi.getLocations(
            query,
            count = 20,
            "en" // TODO Use user’s locale
        ).map { results ->
            if (results.results == null) {
                if (results.generationtimeMs != null && results.generationtimeMs > 0.0) {
                    emptyList()
                } else {
                    throw LocationSearchException()
                }
            } else {
                results.results.mapNotNull {
                    convert(it)
                }
            }
        }
    }

    private fun getWeatherModels(
        location: Location,
    ): List<OpenMeteoWeatherModel> {
        return location.parameters
            .getOrElse(id) { null }?.getOrElse("weatherModels") { null }
            ?.split(",")
            ?.mapNotNull {
                OpenMeteoWeatherModel.Companion.getInstance(it)
            } ?: listOf(OpenMeteoWeatherModel.BEST_MATCH)
    }

    data class WeatherModelStatus(
        val model: OpenMeteoWeatherModel,
        val enabled: Boolean,
    )


    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val OPEN_METEO_AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"
        private const val OPEN_METEO_GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        private const val OPEN_METEO_FORECAST_BASE_URL = "https://api.open-meteo.com/"
    }
}
