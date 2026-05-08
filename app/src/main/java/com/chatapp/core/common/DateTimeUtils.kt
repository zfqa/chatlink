package com.chatapp.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun formatTimestamp(millis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - millis
        return when {
            diff < 60_000 -> "刚刚"
            diff < 86_400_000 -> timeFmt.format(Date(millis))
            else -> dateFmt.format(Date(millis))
        }
    }
}