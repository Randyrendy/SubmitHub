package com.submithub.app.ui.student

import com.submithub.app.ui.settings.SettingsActivity
import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.R
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.SubmissionEntity
import com.submithub.app.databinding.ActivityStudentDashboardBinding
import com.submithub.app.util.DateTimeUtils
import com.submithub.app.util.IconCatalog
import kotlinx.coroutines.launch

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDashboardBinding
    private lateinit var repository: Repository
    private lateinit var studentId: String
    private lateinit var studentName: String
    private var validatedCodeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        studentId = intent.getStringExtra("studentId") ?: ""
        studentName = intent.getStringExtra("studentName") ?: ""

        binding.tvGreeting.text = "Hi, $studentName"
        binding.btnValidate.setOnClickListener { validateCode() }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnSubmit.setOnClickListener {
            val codeId = validatedCodeId ?: return@setOnClickListener
            val intent = Intent(this, SubmitAssignmentActivity::class.java)
            intent.putExtra("codeId", codeId)
            intent.putExtra("studentId", studentId)
            intent.putExtra("studentName", studentName)
            startActivity(intent)
        }

        setupTabs()
        loadAllSubmissions()
    }

    private fun setupTabs() {
        binding.tabHome.setOnClickListener { selectTab(binding.tabHome, binding.containerHome) }
        binding.tabSubmitted.setOnClickListener { selectTab(binding.tabSubmitted, binding.containerSubmittedTab) }
        binding.tabConfirmed.setOnClickListener { selectTab(binding.tabConfirmed, binding.containerConfirmedTab) }
        binding.tabHistory.setOnClickListener { selectTab(binding.tabHistory, binding.containerHistoryTab) }

        // Default: Home selected
        selectTab(binding.tabHome, binding.containerHome)
    }

    private fun selectTab(tab: LinearLayout, container: View) {
        // Reset all tabs to inactive style
        listOf(binding.tabHome, binding.tabSubmitted, binding.tabConfirmed, binding.tabHistory).forEach {
            it.background = null
            val icon = it.getChildAt(0) as ImageView
            val label = it.getChildAt(1) as TextView
            icon.clearColorFilter()
            label.setTextColor(getColor(R.color.text_primary))
            label.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        // Highlight the selected tab
        tab.setBackgroundResource(R.drawable.bg_pill_primary)
        val icon = tab.getChildAt(0) as ImageView
        val label = tab.getChildAt(1) as TextView
        icon.setColorFilter(getColor(R.color.white))
        label.setTextColor(getColor(R.color.white))
        label.setTypeface(null, android.graphics.Typeface.BOLD)

        // Show only the matching content container
        listOf(
            binding.containerHome,
            binding.containerSubmittedTab,
            binding.containerConfirmedTab,
            binding.containerHistoryTab
        ).forEach { it.visibility = if (it == container) View.VISIBLE else View.GONE }
    }

    private fun loadAllSubmissions() {
        repository.submissionsForStudent(studentId).observe(this) { submissions ->
            val sorted = submissions.sortedByDescending { it.submittedAt }

            // Submitted tab
            binding.listSubmitted.removeAllViews()
            binding.tvEmptySubmitted.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
            sorted.forEach { sub ->
                val row = LayoutInflater.from(this).inflate(R.layout.item_submitted_pdf, binding.listSubmitted, false)
                row.findViewById<TextView>(R.id.tvFileName).text = sub.fileName
                row.findViewById<TextView>(R.id.tvPageCount).text =
                    if (sub.pageCount > 0) "${sub.pageCount} Pages" else "Page count unavailable"
                binding.listSubmitted.addView(row)
            }

            // Confirmed tab
            binding.listConfirmed.removeAllViews()
            binding.tvEmptyConfirmed.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
            sorted.forEach { sub ->
                val row = LayoutInflater.from(this).inflate(R.layout.item_confirmed_pdf, binding.listConfirmed, false)
                row.findViewById<TextView>(R.id.tvFileName).text = sub.fileName
                row.findViewById<TextView>(R.id.tvPageCount).text =
                    if (sub.pageCount > 0) "${sub.pageCount} Pages" else "Page count unavailable"
                val statusView = row.findViewById<TextView>(R.id.tvConfirmStatus)
                if (sub.isConfirmed) {
                    statusView.text = "Confirmed"
                    statusView.setBackgroundResource(R.drawable.bg_chip_success)
                    statusView.setTextColor(getColor(R.color.success))
                } else {
                    statusView.text = "Not Confirmed"
                    statusView.setBackgroundResource(R.drawable.bg_chip_error)
                    statusView.setTextColor(getColor(R.color.error))
                }
                binding.listConfirmed.addView(row)
            }

            // History tab
            binding.listHistory.removeAllViews()
            binding.tvEmptyHistory.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
            sorted.forEach { sub ->
                val row = LayoutInflater.from(this).inflate(R.layout.item_history_pdf, binding.listHistory, false)
                row.findViewById<TextView>(R.id.tvFileName).text = sub.fileName
                row.findViewById<TextView>(R.id.tvDateTime).text = DateTimeUtils.format(sub.submittedAt)
                row.findViewById<TextView>(R.id.tvPageCount).text =
                    if (sub.pageCount > 0) "${sub.pageCount} Pages" else "Page count unavailable"

                val confirmView = row.findViewById<TextView>(R.id.tvConfirmStatus)
                if (sub.isConfirmed) {
                    confirmView.text = "Confirmed"
                    confirmView.setBackgroundResource(R.drawable.bg_chip_success)
                    confirmView.setTextColor(getColor(R.color.success))
                } else {
                    confirmView.text = "Not Confirmed"
                    confirmView.setBackgroundResource(R.drawable.bg_chip_error)
                    confirmView.setTextColor(getColor(R.color.error))
                }
                binding.listHistory.addView(row)
            }
        }
    }

    private fun validateCode() {
        val code = binding.etCode.text.toString().trim().uppercase()
        if (code.isEmpty()) {
            Toast.makeText(this, "Enter a code first", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val found = repository.findCode(code)
            if (found == null) {
                binding.cardCategory.visibility = View.GONE
                Toast.makeText(this@StudentDashboardActivity, "Invalid code", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val expired = DateTimeUtils.isPast(found.expiresAt)
            val alreadySubmitted = repository.hasAlreadySubmitted(found.id, studentId)

            validatedCodeId = found.id
            binding.cardCategory.visibility = View.VISIBLE
            binding.ivIcon.setImageResource(IconCatalog.resFor(found.iconKey))
            binding.tvSchool.text = found.school
            binding.tvFaculty.text = found.faculty
            binding.tvDepartment.text = found.department
            binding.tvCourse.text = "${found.courseCode} - ${found.courseTitle}"
            binding.tvAssignmentTitle.text = found.assignmentTitle
            binding.tvDue.text = "Due: ${DateTimeUtils.format(found.expiresAt)}"
            binding.tvTimeLeft.text = DateTimeUtils.timeRemaining(found.expiresAt)

            when {
                expired -> {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text = "Submission closed"
                }
                alreadySubmitted -> {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text = "Already submitted"
                }
                else -> {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Submit Assignment"
                }
            }
        }
    }
}