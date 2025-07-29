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


/**
 * Open-Meteo weather
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenMeteoWeatherResult(
    @JsonProperty("current") val current: OpenMeteoWeatherCurrent? = null,
    @JsonProperty("daily") val daily: OpenMeteoWeatherDaily? = null,
    @JsonProperty("hourly") val hourly: OpenMeteoWeatherHourly? = null,
    @JsonProperty("minutely_15") val minutelyFifteen: OpenMeteoWeatherMinutely? = null,
    @JsonProperty("error") val error: Boolean? = null,
    @JsonProperty("reason") val reason: String? = null,
)

