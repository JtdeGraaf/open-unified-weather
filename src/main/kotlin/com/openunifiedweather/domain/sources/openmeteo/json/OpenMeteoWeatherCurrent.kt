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

package com.openunifiedweather.domain.sources.openmeteo.json

import com.fasterxml.jackson.annotation.JsonProperty


data class OpenMeteoWeatherCurrent(
    @JsonProperty("temperature_2m") val temperature: Double?,
    @JsonProperty("apparent_temperature") val apparentTemperature: Double?,
    @JsonProperty("weathercode") val weatherCode: Int?,
    @JsonProperty("windspeed_10m") val windSpeed: Double?,
    @JsonProperty("winddirection_10m") val windDirection: Double?,
    @JsonProperty("windgusts_10m") val windGusts: Double?,
    @JsonProperty("uv_index") val uvIndex: Double?,
    @JsonProperty("relativehumidity_2m") val relativeHumidity: Int?,
    @JsonProperty("dewpoint_2m") val dewPoint: Double?,
    @JsonProperty("pressure_msl") val pressureMsl: Double?,
    @JsonProperty("cloudcover") val cloudCover: Int?,
    val visibility: Double?,
    val time: Long,
)
