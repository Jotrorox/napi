package com.jotrorox.napi.converters

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

/**
 * Gets the current time in the format "yyyy-MM-dd HH:mm:ss".
 *
 * This function uses the `SimpleDateFormat` class to format the current date and time into a string.
 * The format used is "yyyy-MM-dd HH:mm:ss", which represents the year, month, day, hour, minute, and second.
 *
 * @return A string representing the current date and time.
 */
fun getCurrentTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}

/**
 * Converts a string representation of a date into a `LocalDateTime` object.
 *
 * This function uses the `SimpleDateFormat` class to parse the input string into a `Date` object.
 * The input string is expected to be in the format "yyyy-MM-dd'T'HH:mm:ss'Z'".
 * The function then converts the `Date` object into an `Instant`, adjusts it to the system's default time zone,
 * and finally converts it into a `LocalDateTime` object.
 *
 * @param date A string representing a date and time. The string is expected to be in the format "yyyy-MM-dd'T'HH:mm:ss'Z'".
 * @return A `LocalDateTime` object representing the same date and time as the input string.
 */
fun getDateFromString(date: String): LocalDateTime {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date).toInstant().atZone(TimeZone.getDefault().toZoneId())
        .toLocalDateTime()
}