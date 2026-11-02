package com.submithub.app.util

import com.submithub.app.data.entity.UserRole

object AuthEmailHelper {
    /**
     * Firebase Auth requires an email format, but students/lecturers log in
     * with an ID (matric number / staff ID), not an email. This builds a
     * stable, valid synthetic email from their ID + role, so the same ID
     * used by both a lecturer and a student doesn't collide.
     */
    fun syntheticEmailFor(role: UserRole, id: String): String {
        val sanitized = id.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        return "${role.name.lowercase()}_$sanitized@submithub.local"
    }
}