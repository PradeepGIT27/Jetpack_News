package com.example.newsapp.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dateStr: String): String {
    return try {
        val parsed = ZonedDateTime.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        parsed.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}
