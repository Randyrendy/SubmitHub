package com.submithub.app.data.entity

data class CalendarEventEntity(
    val id: String = "",
    val lecturerId: String,
    val title: String,
    val dateMillis: Long,
    val notes: String = ""
)