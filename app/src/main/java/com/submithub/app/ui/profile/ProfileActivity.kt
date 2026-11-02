package com.submithub.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var repository: Repository
    private lateinit var userId: String
    private lateinit var userRole: UserRole

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        userId = intent.getStringExtra("userId") ?: ""
        val roleName = intent.getStringExtra("userRole") ?: UserRole.LECTURER.name
        userRole = UserRole.valueOf(roleName)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { onSaveClicked() }

        loadProfile()
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            val users = repository.allUsers().value
            val user = users?.firstOrNull { it.id == userId }

            if (user == null) {
                // Fallback: pull straight from intent extras if the live list hasn't loaded yet
                val fallbackName = intent.getStringExtra("userName") ?: ""
                binding.tvName.text = fallbackName
                binding.tvInitials.text = initialsFor(fallbackName)
                binding.tvRole.text = userRole.name
                binding.etName.setText(fallbackName)
                binding.etId.setText(userId)
                return@launch
            }

            binding.tvName.text = user.name
            binding.tvInitials.text = initialsFor(user.name)
            binding.tvRole.text = "${user.role.name.lowercase().replaceFirstChar { it.uppercase() }} · ${user.identifierLabel}"
            binding.etName.setText(user.name)
            binding.etId.setText(user.id)
        }
    }

    private fun onSaveClicked() {
        val name = binding.etName.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        binding.tvError.visibility = View.GONE

        if (name.isEmpty()) {
            binding.tvError.text = "Name cannot be empty"
            binding.tvError.visibility = View.VISIBLE
            return
        }

        if (newPassword.isNotEmpty()) {
            if (newPassword.length < 6) {
                binding.tvError.text = "Password must be at least 6 characters"
                binding.tvError.visibility = View.VISIBLE
                return
            }
            if (newPassword != confirmPassword) {
                binding.tvError.text = "Passwords do not match"
                binding.tvError.visibility = View.VISIBLE
                return
            }
        }

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            repository.updateUserProfile(
                userId = userId,
                name = name,
                newPassword = newPassword.ifBlank { null }
            )
            Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initialsFor(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
}