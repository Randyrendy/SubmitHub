package com.submithub.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val displayFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    fun format(millis: Long): String = displayFormat.format(Date(millis))

    fun isPast(millis: Long): Boolean = System.currentTimeMillis() > millis

    fun timeRemaining(millis: Long): String {
        val diff = millis - System.currentTimeMillis()
        if (diff <= 0) return "Expired"
        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff / (1000 * 60 * 60)) % 24
        val minutes = (diff / (1000 * 60)) % 60
        return when {
            days > 0 -> "$days d $hours h left"
            hours > 0 -> "$hours h $minutes m left"
            else -> "${minutes.coerceAtLeast(1)} min left"
        }
    }

    fun calendarAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
