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

import com.openunifiedweather.domain.model.location.model.Location

import java.util.Calendar
import java.util.Date
import java.util.TimeZone


// Makes the code more readable by not having to do a null check condition
fun Long.toDate(): Date {
    return Date(this)
}


fun Date.toCalendar(location: Location): Calendar {
    return Calendar.getInstance().also {
        it.time = this
        it.timeZone = location.javaTimeZone
    }
}

/**
 * Optimized function to get yyyy-MM-dd formatted date
 * Takes 0 ms on my device compared to 2-3 ms for getFormattedDate() (which uses SimpleDateFormat)
 * Saves about 1 second when looping through 24 hourly over a 16 day period
 */
fun Calendar.getIsoFormattedDate(): String {
    return "${this[Calendar.YEAR]}-${getMonth(twoDigits = true)}-${getDayOfMonth(twoDigits = true)}"
}

fun Calendar.getMonth(twoDigits: Boolean = false): String {
    return "${(this[Calendar.MONTH] + 1).let { month ->
        if (twoDigits && month.toString().length < 2) "0$month" else month
    }}"
}

fun Calendar.getDayOfMonth(twoDigits: Boolean = false): String {
    return "${this[Calendar.DAY_OF_MONTH].let { day ->
        if (twoDigits && day.toString().length < 2) "0$day" else day
    }}"
}

fun Date.getIsoFormattedDate(location: Location): String {
    return toCalendar(location).getIsoFormattedDate()
}

fun Date.toCalendarWithTimeZone(zone: TimeZone): Calendar {
    return Calendar.getInstance().also {
        it.time = this
        it.timeZone = zone
    }
}
