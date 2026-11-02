package com.submithub.app.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.submithub.app.MainActivity
import com.submithub.app.R
import com.submithub.app.databinding.ActivitySettingsBinding
import com.submithub.app.util.ThemePrefs
import com.submithub.app.util.SessionManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (ThemePrefs.getThemeMode(this)) {
            ThemePrefs.MODE_LIGHT -> binding.radioLight.isChecked = true
            ThemePrefs.MODE_DARK -> binding.radioDark.isChecked = true
            else -> binding.radioSystem.isChecked = true
        }

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioLight -> ThemePrefs.MODE_LIGHT
                R.id.radioDark -> ThemePrefs.MODE_DARK
                else -> ThemePrefs.MODE_SYSTEM
            }
            ThemePrefs.saveThemeMode(this, mode)
            ThemePrefs.applyTheme(mode)
            recreate()
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.clearSession(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}