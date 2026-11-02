package com.submithub.app.ui.lecturer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.submithub.app.data.Repository
import com.submithub.app.databinding.ActivityCourseAssignmentsBinding

class CourseAssignmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseAssignmentsBinding
    private lateinit var repository: Repository
    private lateinit var adapter: CourseCodeAdapter
    private lateinit var lecturerId: String
    private lateinit var courseCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseAssignmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        lecturerId = intent.getStringExtra("lecturerId") ?: ""
        courseCode = intent.getStringExtra("courseCode") ?: ""
        val courseTitle = intent.getStringExtra("courseTitle") ?: ""

        binding.tvCourseTitle.text = courseTitle
        binding.tvCourseCode.text = courseCode
        binding.btnBack.setOnClickListener { finish() }

        adapter = CourseCodeAdapter { code ->
            val intent = Intent(this, SubmissionsActivity::class.java)
            intent.putExtra("codeId", code.id)
            startActivity(intent)
        }
        binding.rvAssignments.layoutManager = LinearLayoutManager(this)
        binding.rvAssignments.adapter = adapter

        repository.codesByLecturer(lecturerId).observe(this) { codes ->
            val filtered = codes.filter { it.courseCode == courseCode }
            adapter.submitList(filtered)
            binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}