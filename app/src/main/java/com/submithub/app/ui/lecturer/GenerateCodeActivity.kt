package com.submithub.app.ui.lecturer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.CodeEntity
import com.submithub.app.databinding.ActivityGenerateCodeBinding
import com.submithub.app.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class GenerateCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateCodeBinding
    private lateinit var repository: Repository
    private lateinit var iconAdapter: IconPickerAdapter
    private lateinit var lecturerId: String
    private lateinit var lecturerName: String

    private var selectedYear = -1
    private var selectedMonth = -1
    private var selectedDay = -1
    private var selectedHour = -1
    private var selectedMinute = -1

    private var lastGeneratedCode: String = ""
    private var lastCourseTitle: String = ""
    private var lastAssignmentTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        lecturerId = intent.getStringExtra("lecturerId") ?: ""
        lecturerName = intent.getStringExtra("lecturerName") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        iconAdapter = IconPickerAdapter { }
        binding.rvIcons.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvIcons.adapter = iconAdapter

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnPickTime.setOnClickListener { showTimePicker() }

        binding.btnGenerate.setOnClickListener { onGenerateClicked() }

        binding.btnDone.setOnClickListener { finish() }

        binding.btnCopyCode.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Assignment code", lastGeneratedCode))
            Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.btnShareCode.setOnClickListener {
            val shareText = "Submit your \"$lastAssignmentTitle\" for $lastCourseTitle on SubmitHub using code: $lastGeneratedCode"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share assignment code"))
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                updateDateTimeLabel()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                updateDateTimeLabel()
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateTimeLabel() {
        if (selectedYear == -1 || selectedHour == -1) return
        val millis = DateTimeUtils.calendarAt(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
        binding.tvSelectedDateTime.text = "Deadline: ${DateTimeUtils.format(millis)}"
    }

    private fun onGenerateClicked() {
        val school = binding.etSchool.text.toString().trim()
        val faculty = binding.etFaculty.text.toString().trim()
        val department = binding.etDepartment.text.toString().trim()
        val courseCode = binding.etCourseCode.text.toString().trim()
        val courseTitle = binding.etCourseTitle.text.toString().trim()
        val assignmentTitle = binding.etAssignmentTitle.text.toString().trim()

        if (school.isEmpty() || faculty.isEmpty() || department.isEmpty() ||
            courseCode.isEmpty() || courseTitle.isEmpty() || assignmentTitle.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all category fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedYear == -1 || selectedHour == -1) {
            Toast.makeText(this, "Please pick a deadline date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val expiresAt = DateTimeUtils.calendarAt(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
        if (expiresAt <= System.currentTimeMillis()) {
            Toast.makeText(this, "Deadline must be in the future", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val code = repository.generateUniqueCode()
            val entity = CodeEntity(
                id = code,
                school = school,
                faculty = faculty,
                department = department,
                courseCode = courseCode,
                courseTitle = courseTitle,
                assignmentTitle = assignmentTitle,
                iconKey = iconAdapter.selectedIconKey(),
                createdByLecturerId = lecturerId,
                createdByLecturerName = lecturerName,
                createdAt = System.currentTimeMillis(),
                expiresAt = expiresAt
            )
            repository.createCode(entity)

            lastGeneratedCode = code
            lastCourseTitle = courseTitle
            lastAssignmentTitle = assignmentTitle

            binding.tvGeneratedCode.text = code
            binding.layoutResult.visibility = android.view.View.VISIBLE
        }
    }
}