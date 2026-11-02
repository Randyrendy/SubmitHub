package com.submithub.app.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ActivityStudentLoginBinding
import kotlinx.coroutines.launch

class StudentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentLoginBinding
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentLoginBinding.inflate(layoutInflater)
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
                val user = repository.loginWithAuth(UserRole.STUDENT, id, password)
                if (user != null) {
                    com.submithub.app.util.SessionManager.saveSession(this@StudentLoginActivity, user.id, user.name, UserRole.STUDENT.name)
                    val intent = Intent(this@StudentLoginActivity, StudentDashboardActivity::class.java)
                    intent.putExtra("studentId", user.id)
                    intent.putExtra("studentName", user.name)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    binding.tvError.text = "Invalid ID or password"
                    binding.tvError.visibility = android.view.View.VISIBLE
                }
            }
        }
        binding.tvGoToCreateAccount.setOnClickListener {
            val intent = Intent(this, com.submithub.app.ui.auth.CreateAccountActivity::class.java)
            intent.putExtra("role", UserRole.LECTURER.name)
            startActivity(intent)
            finish()
        }
    }
}
