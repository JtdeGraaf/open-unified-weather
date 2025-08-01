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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenMeteoWeatherDaily(
    @JsonProperty("time") val time: List<Long>,
    @JsonProperty("temperature_2m_max") val temperatureMax: Array<Double?>?,
    @JsonProperty("temperature_2m_min") val temperatureMin: Array<Double?>?,
    @JsonProperty("apparent_temperature_max") val apparentTemperatureMax: Array<Double?>?,
    @JsonProperty("apparent_temperature_min") val apparentTemperatureMin: Array<Double?>?,
    @JsonProperty("sunshine_duration") val sunshineDuration: Array<Double?>?,
    @JsonProperty("uv_index_max") val uvIndexMax: Array<Double?>?,
)
