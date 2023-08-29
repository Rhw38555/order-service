package com.example.setting

import java.time.format.DateTimeFormatter

object GlobalSettings {
    val dateFormatShort: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}