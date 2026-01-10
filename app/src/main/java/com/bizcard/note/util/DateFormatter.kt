package com.bizcard.note.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    private val displayFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    fun formatDate(date: Date?): String {
        return if (date != null) {
            displayFormat.format(date)
        } else {
            ""
        }
    }
}

