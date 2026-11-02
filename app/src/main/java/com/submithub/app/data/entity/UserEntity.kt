package com.submithub.app.data.entity

enum class UserRole { LECTURER, STUDENT, ADMIN }

data class UserEntity(
    val id: String,
    val name: String,
    val role: UserRole,
    val password: String,
    val identifierLabel: String
)