package com.submithub.app.ui.lecturer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.CodeEntity
import com.submithub.app.databinding.ActivityAllAssignmentsBinding

class AllAssignmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllAssignmentsBinding
    private lateinit var repository: Repository
    private lateinit var adapter: CourseCodeAdapter
    private var allCodes: List<CodeEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAssignmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        val lecturerId = intent.getStringExtra("lecturerId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        adapter = CourseCodeAdapter { code ->
            val intent = Intent(this, SubmissionsActivity::class.java)
            intent.putExtra("codeId", code.id)
            startActivity(intent)
        }
        binding.rvAssignments.layoutManager = LinearLayoutManager(this)
        binding.rvAssignments.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = applyFilter(s?.toString().orEmpty())
        })

        repository.codesByLecturer(lecturerId).observe(this) { codes ->
            allCodes = codes
            binding.tvSubtitle.text = "${codes.size} assignment(s) across all courses"
            applyFilter(binding.etSearch.text?.toString().orEmpty())
        }
    }

    private fun applyFilter(query: String) {
        val trimmed = query.trim().lowercase()
        val filtered = if (trimmed.isEmpty()) {
            allCodes
        } else {
            allCodes.filter { code ->
                code.assignmentTitle.lowercase().contains(trimmed) ||
                        code.courseTitle.lowercase().contains(trimmed) ||
                        code.courseCode.lowercase().contains(trimmed) ||
                        code.id.lowercase().contains(trimmed)
            }
        }
        adapter.submitList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}