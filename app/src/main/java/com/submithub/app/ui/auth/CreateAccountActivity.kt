package com.submithub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ActivityCreateAccountBinding
import com.submithub.app.ui.lecturer.LecturerLoginActivity
import com.submithub.app.ui.student.StudentLoginActivity
import kotlinx.coroutines.launch

/**
 * Shared sign-up screen for both Lecturers and Students.
 * Which role it's creating an account for is passed in via the "role" intent extra.
 */
class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var repository: Repository
    private lateinit var role: UserRole

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        val roleName = intent.getStringExtra("role") ?: UserRole.STUDENT.name
        role = UserRole.valueOf(roleName)

        if (role == UserRole.LECTURER) {
            binding.ivRoleIcon.setImageResource(com.submithub.app.R.drawable.ic_lecturer)
            binding.tvTitle.text = "Lecturer Sign Up"
            binding.tvSubtitle.text = "Create your account to start generating assignment codes"
            binding.etId.hint = "Staff ID"
        } else {
            binding.ivRoleIcon.setImageResource(com.submithub.app.R.drawable.ic_student)
            binding.tvTitle.text = "Student Sign Up"
            binding.tvSubtitle.text = "Create your account to start submitting assignments"
            binding.etId.hint = "Matric Number"
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCreateAccount.setOnClickListener { onCreateAccountClicked() }

        binding.tvGoToLogin.setOnClickListener { goToLogin() }
    }

    private fun onCreateAccountClicked() {
        val name = binding.etName.text.toString().trim()
        val id = binding.etId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        binding.tvError.visibility = View.GONE

        if (name.isEmpty() || id.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in every field")
            return
        }

        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match")
            return
        }

        binding.btnCreateAccount.isEnabled = false

        lifecycleScope.launch {
            val alreadyExists = repository.userExists(id)
            if (alreadyExists) {
                showError("This ID is already registered. Please login instead.")
                binding.btnCreateAccount.isEnabled = true
                return@launch
            }

            val identifierLabel = if (role == UserRole.LECTURER) "Staff ID: $id" else "Matric No: $id"

            try {
                repository.registerUserWithAuth(
                    role = role,
                    id = id,
                    name = name,
                    password = password,
                    identifierLabel = identifierLabel
                )

                android.widget.Toast.makeText(
                    this@CreateAccountActivity,
                    "Account created! Please log in.",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                goToLogin()
            } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                showError("Password is too weak. Use at least 6 characters.")
                binding.btnCreateAccount.isEnabled = true
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                showError("This ID is already registered. Please login instead.")
                binding.btnCreateAccount.isEnabled = true
            } catch (e: Exception) {
                showError("Something went wrong: ${e.message}")
                binding.btnCreateAccount.isEnabled = true
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun goToLogin() {
        val target = if (role == UserRole.LECTURER) {
            LecturerLoginActivity::class.java
        } else {
            StudentLoginActivity::class.java
        }
        startActivity(Intent(this, target))
        finish()
    }
}