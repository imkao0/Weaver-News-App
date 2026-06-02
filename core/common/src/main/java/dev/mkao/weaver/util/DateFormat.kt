package dev.mkao.weaver.util

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * @param inputDateTime The date-time string to be formatted, in ISO 8601 format (e.g., "2024-08-17T12:34:56Z").
 * @return A formatted date string in the localized long date format (e.g., "August 17, 2024"),
 *         or an empty string if the input is invalid or null.
 */
fun articleDateFormat(inputDateTime: String?): String {
    val inputFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val outputFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.getDefault())

    return try {
        val dateTime = OffsetDateTime.parse(inputDateTime, inputFormat)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Returns a short date format like "12 Dec".
 */
fun shortDateFormat(inputDateTime: String?): String {
    val inputFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())

    return try {
        val dateTime = OffsetDateTime.parse(inputDateTime, inputFormat)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Returns a dashed date format like "28-01-2019".
 */
fun dashedDateFormat(inputDateTime: String?): String {
    val inputFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

    return try {
        val dateTime = OffsetDateTime.parse(inputDateTime, inputFormat)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Calculates the elapsed time between the published date-time and the current date-time,
 * returning a human-readable string indicating how long ago the article was published.
 *
 * @param publishedAt The date-time string when the article was published, in ISO 8601 format.
 * @return A string representing the elapsed time, such as "2 days ago", "3 hours ago", or "Just now".
 *         Returns "Unknown time" if the input is invalid or an error occurs during parsing.
 */
fun calculateElapsedTime(publishedAt: String?): String {
    return try {
        val publishedDateTime = OffsetDateTime.parse(publishedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val currentDateTime = OffsetDateTime.now()
        val duration = Duration.between(publishedDateTime, currentDateTime)

        when {
            duration.toDays() > 0 -> "${duration.toDays()} days ago"
            duration.toHours() > 0 -> "${duration.toHours()} hours ago"
            duration.toMinutes() > 0 -> "${duration.toMinutes()} minutes ago"
            else -> "Just now"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Unknown time"
    }
}
