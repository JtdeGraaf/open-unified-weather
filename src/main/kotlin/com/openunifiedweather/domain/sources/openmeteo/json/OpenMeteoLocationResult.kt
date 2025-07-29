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
 * Open Meteo geocoding
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenMeteoLocationResult(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("latitude") val latitude: Double,
    @JsonProperty("longitude") val longitude: Double,
    @JsonProperty("timezone") val timezone: String?,
    @JsonProperty("country_code") val countryCode: String?,
    @JsonProperty("country") val country: String?,
    @JsonProperty("admin1") val admin1: String?,
    @JsonProperty("admin2") val admin2: String?,
    @JsonProperty("admin3") val admin3: String?,
    @JsonProperty("admin4") val admin4: String?,
)
