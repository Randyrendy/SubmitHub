package com.submithub.app.ui.lecturer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.submithub.app.data.Repository
import com.submithub.app.util.DateTimeUtils
import com.submithub.app.databinding.ActivityMyCoursesBinding

class MyCoursesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyCoursesBinding
    private lateinit var repository: Repository
    private lateinit var adapter: CourseAdapter
    private lateinit var lecturerId: String
    private lateinit var lecturerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        lecturerId = intent.getStringExtra("lecturerId") ?: ""
        lecturerName = intent.getStringExtra("lecturerName") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        adapter = CourseAdapter { course ->
            val intent = Intent(this, CourseAssignmentsActivity::class.java)
            intent.putExtra("lecturerId", lecturerId)
            intent.putExtra("lecturerName", lecturerName)
            intent.putExtra("courseCode", course.courseCode)
            intent.putExtra("courseTitle", course.courseTitle)
            startActivity(intent)
        }
        binding.rvCourses.layoutManager = LinearLayoutManager(this)
        binding.rvCourses.adapter = adapter

        repository.codesByLecturer(lecturerId).observe(this) { codes ->
            val grouped = codes
                .groupBy { it.courseCode to it.courseTitle }
                .map { (key, codesInCourse) ->
                    CourseSummary(
                        courseCode = key.first,
                        courseTitle = key.second,
                        department = codesInCourse.first().department,
                        assignmentCount = codesInCourse.size,
                        activeCount = codesInCourse.count { !DateTimeUtils.isPast(it.expiresAt) }
                    )
                }
                .sortedBy { it.courseCode }

            adapter.submitList(grouped)
            binding.tvSubtitle.text = "${grouped.size} course(s)"
            binding.tvEmpty.visibility = if (grouped.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}