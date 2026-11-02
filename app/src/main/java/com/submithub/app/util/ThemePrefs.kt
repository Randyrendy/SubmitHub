package com.submithub.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemePrefs {
    private const val PREFS_NAME = "settings"
    private const val KEY_THEME = "theme_mode"

    const val MODE_LIGHT = "LIGHT"
    const val MODE_DARK = "DARK"
    const val MODE_SYSTEM = "SYSTEM"

    fun saveThemeMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode)
            .apply()
    }

    fun getThemeMode(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, MODE_SYSTEM) ?: MODE_SYSTEM
    }

    fun applyTheme(mode: String) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}