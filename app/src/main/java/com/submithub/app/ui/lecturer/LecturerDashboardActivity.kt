package com.submithub.app.ui.lecturer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.submithub.app.R
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ActivityLecturerDashboardBinding
import com.submithub.app.ui.profile.ProfileActivity
import com.submithub.app.ui.settings.SettingsActivity
import com.submithub.app.util.DateTimeUtils
import com.submithub.app.util.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LecturerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerDashboardBinding
    private lateinit var repository: Repository
    private lateinit var lecturerId: String
    private lateinit var lecturerName: String

    override fun onResume() {
        super.onResume()
        SessionManager.updateLastActive(this)
        loadDashboardData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        lecturerId = intent.getStringExtra("lecturerId") ?: ""
        lecturerName = intent.getStringExtra("lecturerName") ?: ""

        binding.tvGreeting.text = "Welcome back, $lecturerName"
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date()).uppercase()

        setupDrawer()

        binding.btnGenerateCode.setOnClickListener {
            val intent = Intent(this, GenerateCodeActivity::class.java)
            intent.putExtra("lecturerId", lecturerId)
            intent.putExtra("lecturerName", lecturerName)
            startActivity(intent)
        }
    }

    private fun loadDashboardData() {
        if (lecturerId.isEmpty()) return
        lifecycleScope.launch {
            val data = repository.getLecturerDashboardData(lecturerId)

            binding.tvActiveCourses.text = data.stats.activeCourses.toString()
            binding.tvPendingGrading.text = data.stats.pendingGrading.toString()
            binding.tvTotalStudents.text = data.stats.totalStudents.toString()
            binding.tvSubmissionsThisWeek.text = data.stats.submissionsThisWeek.toString()

            binding.containerRecentSubmissions.removeAllViews()
            if (data.recentSubmissions.isEmpty()) {
                binding.tvNoRecentSubmissions.visibility = View.VISIBLE
            } else {
                binding.tvNoRecentSubmissions.visibility = View.GONE
                data.recentSubmissions.forEach { item ->
                    val row = LayoutInflater.from(this@LecturerDashboardActivity)
                        .inflate(R.layout.item_recent_submission, binding.containerRecentSubmissions, false)
                    row.findViewById<TextView>(R.id.tvInitials).text = initialsFor(item.studentName)
                    row.findViewById<TextView>(R.id.tvStudentName).text = item.studentName
                    row.findViewById<TextView>(R.id.tvAssignmentTitle).text = item.assignmentTitle
                    val statusView = row.findViewById<TextView>(R.id.tvStatus)
                    statusView.text = item.status
                    when (item.status) {
                        "Graded" -> {
                            statusView.setBackgroundResource(R.drawable.bg_chip_warning)
                            statusView.setTextColor(getColor(R.color.success))
                        }
                        "Late" -> {
                            statusView.setBackgroundResource(R.drawable.bg_chip_warning)
                            statusView.setTextColor(getColor(R.color.error))
                        }
                        else -> {
                            statusView.setBackgroundResource(R.drawable.bg_chip_warning)
                            statusView.setTextColor(getColor(R.color.warning))
                        }
                    }
                    binding.containerRecentSubmissions.addView(row)
                }
            }

            binding.containerUpcomingDeadlines.removeAllViews()
            if (data.upcomingDeadlines.isEmpty()) {
                binding.tvNoUpcomingDeadlines.visibility = View.VISIBLE
            } else {
                binding.tvNoUpcomingDeadlines.visibility = View.GONE
                data.upcomingDeadlines.forEach { item ->
                    val row = LayoutInflater.from(this@LecturerDashboardActivity)
                        .inflate(R.layout.item_upcoming_deadline, binding.containerUpcomingDeadlines, false)
                    row.findViewById<TextView>(R.id.tvDeadlineTitle).text = item.title
                    row.findViewById<TextView>(R.id.tvDeadlineDate).text =
                        "Closes ${DateTimeUtils.format(item.expiresAt)}"
                    binding.containerUpcomingDeadlines.addView(row)
                }
            }
        }
    }

    private fun setupDrawer() {
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        val drawer = binding.navDrawer
        drawer.tvDrawerName.text = lecturerName
        drawer.tvDrawerInitials.text = initialsFor(lecturerName)

        drawer.navDashboard.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        val comingSoon = { name: String ->
            Toast.makeText(this, "$name coming soon", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawer.navMyCourses.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, MyCoursesActivity::class.java)
            intent.putExtra("lecturerId", lecturerId)
            intent.putExtra("lecturerName", lecturerName)
            startActivity(intent)
        }
        drawer.navAssignments.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, AllAssignmentsActivity::class.java)
            intent.putExtra("lecturerId", lecturerId)
            startActivity(intent)
        }
        drawer.navGrading.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.putExtra("lecturerId", lecturerId)
            startActivity(intent)
        }
        drawer.navCalendar.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, CalendarActivity::class.java)
            intent.putExtra("lecturerId", lecturerId)
            startActivity(intent)
        }

        drawer.navSettings.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        drawer.rowProfile.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userId", lecturerId)
            intent.putExtra("userName", lecturerName)
            intent.putExtra("userRole", UserRole.LECTURER.name)
            startActivity(intent)
        }
    }

    private fun initialsFor(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
}