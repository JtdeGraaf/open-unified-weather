/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package com.openunifiedweather.domain.model.location.model

import com.openunifiedweather.domain.model.weather.model.Weather
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Location(
    val cityId: String? = null,

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timeZone: String = TimeZone.getDefault().id,

    val customName: String? = null,
    val country: String = "",
    val countryCode: String? = null,
    val admin1: String? = null,
    val admin1Code: String? = null,
    val admin2: String? = null,
    val admin2Code: String? = null,
    val admin3: String? = null,
    val admin3Code: String? = null,
    val admin4: String? = null,
    val admin4Code: String? = null,
    val city: String = "",
    val district: String? = null,

    val weather: Weather? = null,
    val forecastSource: String = "openmeteo",
    val currentSource: String? = null,
    val airQualitySource: String? = null,
    val pollenSource: String? = null,
    val minutelySource: String? = null,
    val alertSource: String? = null,
    val normalsSource: String? = null,
    val reverseGeocodingSource: String? = null,

    val isCurrentPosition: Boolean = false,

    val needsGeocodeRefresh: Boolean = false,

    val backgroundWeatherKind: String? = null,
    val backgroundDayNightType: String? = null,

    /**
     * "accu": {"cityId": "230"}
     * "nws": {"gridId": "8", "gridX": "20", "gridY": "30"}
     * etc
     */
    val parameters: Map<String, Map<String, String>> = emptyMap(),
) {

    val javaTimeZone: TimeZone = TimeZone.getTimeZone(timeZone)

    val formattedId: String
        get() = if (isCurrentPosition) {
            CURRENT_POSITION_ID
        } else {
            String.format(Locale.US, "%f", latitude) +
                "&" +
                String.format(Locale.US, "%f", longitude) +
                "&" +
                forecastSource
        }

    val isUsable: Boolean
        // Sorry people living exactly at 0,0
        get() = latitude != 0.0 || longitude != 0.0


    fun administrationLevels(): String {
        val builder = StringBuilder()
        if (country.isNotEmpty()) {
            builder.append(country)
        }
        if (!admin1.isNullOrEmpty()) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin1)
        }
        if (!admin2.isNullOrEmpty()) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin2)
        }
        if (!admin3.isNullOrEmpty() && (!admin4.isNullOrEmpty() || admin3 != city)) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin3)
        }
        if (!admin4.isNullOrEmpty() && admin4 != city) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin4)
        }
        return builder.toString()
    }

    val cityAndDistrict: String
        get() {
            val builder = StringBuilder()
            if (city.isNotEmpty()) {
                builder.append(city)
            }
            if (!district.isNullOrEmpty()) {
                if (builder.toString().isNotEmpty()) {
                    builder.append(", ")
                }
                builder.append(district)
            }
            return builder.toString()
        }

    fun isCloseTo(location: Location): Boolean {
        if (cityId == location.cityId) {
            return true
        }
        if (isEquals(admin1, location.admin1) &&
            isEquals(admin2, location.admin2) &&
            isEquals(admin3, location.admin3) &&
            isEquals(admin4, location.admin4) &&
            isEquals(city, location.city)
        ) {
            return true
        }
        return if (isEquals(admin1, location.admin1) &&
            isEquals(admin2, location.admin2) &&
            isEquals(admin3, location.admin3) &&
            isEquals(admin4, location.admin4) &&
            cityAndDistrict == location.cityAndDistrict
        ) {
            true
        } else {
            distance(this, location) < (20 * 1000)
        }
    }

    companion object {

        const val CURRENT_POSITION_ID = "CURRENT_POSITION"

        fun isEquals(a: String?, b: String?): Boolean {
            return if (a.isNullOrEmpty() && b.isNullOrEmpty()) {
                true
            } else if (!a.isNullOrEmpty() && !b.isNullOrEmpty()) {
                a == b
            } else {
                false
            }
        }

        fun distance(location1: Location, location2: Location): Double {
            return distance(
                location1.latitude,
                location1.longitude,
                location2.latitude,
                location2.longitude
            )
        }

        /**
         * Adapted from https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
         *
         * Calculate distance between two points in latitude and longitude taking
         * into account height difference. Uses Haversine method as its base.
         *
         * @returns Distance in Meters
         */
        fun distance(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double,
        ): Double {
            val r = 6371 // Radius of the earth

            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = sin(latDistance / 2) *
                sin(latDistance / 2) +
                (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(lonDistance / 2) * sin(lonDistance / 2))
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            var distance = r * c * 1000 // convert to meters

            distance = distance.pow(2.0)

            return sqrt(distance)
        }
    }
}
