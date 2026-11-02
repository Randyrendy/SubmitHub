package com.submithub.app.util

import android.content.Context

object SessionManager {
    private const val PREFS_NAME = "session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_ROLE = "role"
    private const val KEY_LAST_ACTIVE = "last_active"

    private const val THREE_MONTHS_MILLIS = 90L * 24 * 60 * 60 * 1000 // 90 days

    fun saveSession(context: Context, userId: String, userName: String, role: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_ROLE, role)
            .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
            .apply()
    }

    fun updateLastActive(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
            .apply()
    }

    fun getValidSession(context: Context): Session? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val userName = prefs.getString(KEY_USER_NAME, null) ?: return null
        val role = prefs.getString(KEY_ROLE, null) ?: return null
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0L)

        val expired = System.currentTimeMillis() - lastActive > THREE_MONTHS_MILLIS
        if (expired) {
            clearSession(context)
            return null
        }

        return Session(userId, userName, role)
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    data class Session(val userId: String, val userName: String, val role: String)
}