package com.submithub.app.data.entity

data class CodeEntity(
    val id: String,
    val school: String,
    val faculty: String,
    val department: String,
    val courseCode: String,
    val courseTitle: String,
    val assignmentTitle: String,
    val iconKey: String,
    val createdByLecturerId: String,
    val createdByLecturerName: String,
    val createdAt: Long,
    val expiresAt: Long
)