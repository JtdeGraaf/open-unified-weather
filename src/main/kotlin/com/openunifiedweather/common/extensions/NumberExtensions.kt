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

package com.openunifiedweather.common.extensions

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.floor

operator fun Int?.plus(other: Int?): Int? = if (this != null || other != null) {
    (this ?: 0) + (other ?: 0)
} else {
    null
}

operator fun Double?.plus(other: Double?): Double? = if (this != null || other != null) {
    (this ?: 0.0) + (other ?: 0.0)
} else {
    null
}

operator fun Double?.minus(other: Double?): Double? = if (this != null || other != null) {
    (this ?: 0.0) - (other ?: 0.0)
} else {
    null
}

fun Double.roundUpToNearestMultiplier(multiplier: Double): Double {
    return ceil(div(multiplier)).times(multiplier)
}

fun Double.roundDownToNearestMultiplier(multiplier: Double): Double {
    return floor(div(multiplier)).times(multiplier)
}

fun Double.roundDecimals(decimals: Int): Double? {
    return if (!isNaN()) {
        BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()
    } else {
        null
    }
}

val Array<Double>.median: Double?
    get() {
        if (isEmpty()) return null

        sort()

        return if (size % 2 != 0) {
            this[size / 2]
        } else {
            (this[(size - 1) / 2] + this[size / 2]) / 2.0
        }
    }
