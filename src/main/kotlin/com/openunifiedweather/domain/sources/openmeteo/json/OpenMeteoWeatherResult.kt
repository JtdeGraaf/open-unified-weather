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


/**
 * Open-Meteo weather
 */
data class OpenMeteoWeatherResult(
    @JsonProperty("current") val current: OpenMeteoWeatherCurrent? = null,
    val daily: OpenMeteoWeatherDaily? = null,
    val hourly: OpenMeteoWeatherHourly? = null,
    @JsonProperty("minutely_15") val minutelyFifteen: OpenMeteoWeatherMinutely? = null,
    val error: Boolean? = null,
    val reason: String? = null,
)
