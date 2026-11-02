package com.submithub.app.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ActivityAdminLoginBinding
import kotlinx.coroutines.launch

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLoginBinding
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (id.isEmpty() || password.isEmpty()) {
                binding.tvError.text = "Please enter both ID and password"
                binding.tvError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = repository.loginWithAuth(UserRole.ADMIN, id, password)
                if (user != null) {
                    com.submithub.app.util.SessionManager.saveSession(this@AdminLoginActivity, user.id, user.name, UserRole.ADMIN.name)
                    val intent = Intent(this@AdminLoginActivity, AdminDashboardActivity::class.java)
                    intent.putExtra("adminName", user.name)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    binding.tvError.text = "Invalid ID or password"
                    binding.tvError.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}