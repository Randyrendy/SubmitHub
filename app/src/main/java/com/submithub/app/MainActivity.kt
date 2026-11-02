package com.submithub.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ActivityMainBinding
import com.submithub.app.ui.admin.AdminDashboardActivity
import com.submithub.app.ui.admin.AdminLoginActivity
import com.submithub.app.ui.auth.CreateAccountActivity
import com.submithub.app.ui.lecturer.LecturerDashboardActivity
import com.submithub.app.ui.student.StudentDashboardActivity
import com.submithub.app.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager.getValidSession(this)
        if (session != null) {
            val target = when (session.role) {
                UserRole.ADMIN.name -> AdminDashboardActivity::class.java
                UserRole.LECTURER.name -> LecturerDashboardActivity::class.java
                else -> StudentDashboardActivity::class.java
            }
            val intent = Intent(this, target)
            when (session.role) {
                UserRole.ADMIN.name -> intent.putExtra("adminName", session.userName)
                UserRole.LECTURER.name -> {
                    intent.putExtra("lecturerId", session.userId)
                    intent.putExtra("lecturerName", session.userName)
                }
                else -> {
                    intent.putExtra("studentId", session.userId)
                    intent.putExtra("studentName", session.userName)
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLecturer.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            intent.putExtra("role", UserRole.LECTURER.name)
            startActivity(intent)
        }
        binding.btnStudent.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            intent.putExtra("role", UserRole.STUDENT.name)
            startActivity(intent)
        }
        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
    }
}