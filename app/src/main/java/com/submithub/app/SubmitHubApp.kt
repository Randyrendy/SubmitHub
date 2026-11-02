package com.submithub.app

import android.app.Application
import com.submithub.app.util.ThemePrefs

class SubmitHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePrefs.applyTheme(ThemePrefs.getThemeMode(this))
    }
}