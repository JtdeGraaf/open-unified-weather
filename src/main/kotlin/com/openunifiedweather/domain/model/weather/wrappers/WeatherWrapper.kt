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

package com.openunifiedweather.domain.model.weather.wrappers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.openunifiedweather.domain.model.source.SourceFeature
import com.openunifiedweather.domain.model.weather.model.Alert
import com.openunifiedweather.domain.model.weather.model.Minutely
import com.openunifiedweather.domain.model.weather.model.Normals

/**
 * Wrapper very similar to the object in database.
 * Helps the transition process and computing of missing data.
 */
data class WeatherWrapper(
    val dailyForecast: List<DailyWrapper>? = null,
    val hourlyForecast: List<HourlyWrapper>? = null,
    val current: CurrentWrapper? = null,
    val airQuality: AirQualityWrapper? = null,
    val pollen: PollenWrapper? = null,
    val minutelyForecast: List<Minutely>? = null,
    val alertList: List<Alert>? = null,
    val normals: Normals? = null,
    @JsonIgnore val failedFeatures: Map<SourceFeature, Throwable>? = null,
)
