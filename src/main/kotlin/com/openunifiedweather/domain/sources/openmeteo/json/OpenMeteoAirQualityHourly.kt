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
data class OpenMeteoAirQualityHourly(
    @JsonProperty("time") val time: List<Long>,
    @JsonProperty("pm10") val pm10: Array<Double?>?,
    @JsonProperty("pm2_5") val pm25: Array<Double?>?,
    @JsonProperty("carbon_monoxide") val carbonMonoxide: Array<Double?>?,
    @JsonProperty("nitrogen_dioxide") val nitrogenDioxide: Array<Double?>?,
    @JsonProperty("sulphur_dioxide") val sulphurDioxide: Array<Double?>?,
    @JsonProperty("ozone") val ozone: Array<Double?>?,
    @JsonProperty("alder_pollen") val alderPollen: Array<Double?>?,
    @JsonProperty("birch_pollen") val birchPollen: Array<Double?>?,
    @JsonProperty("grass_pollen") val grassPollen: Array<Double?>?,
    @JsonProperty("mugwort_pollen") val mugwortPollen: Array<Double?>?,
    @JsonProperty("olive_pollen") val olivePollen: Array<Double?>?,
    @JsonProperty("ragweed_pollen") val ragweedPollen: Array<Double?>?,
)
