package com.submithub.app.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.submithub.app.R
import com.submithub.app.data.Repository
import com.submithub.app.databinding.ActivitySubmittedBinding

class SubmittedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmittedBinding
    private lateinit var repository: Repository
    private var studentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmittedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        studentId = intent.getStringExtra("studentId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        repository.submissionsForStudent(studentId).observe(this) { submissions ->
            binding.containerList.removeAllViews()
            binding.tvEmpty.visibility = if (submissions.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

            submissions.sortedByDescending { it.submittedAt }.forEach { sub ->
                val row = LayoutInflater.from(this)
                    .inflate(R.layout.item_submitted_pdf, binding.containerList, false)
                row.findViewById<TextView>(R.id.tvFileName).text = sub.fileName
                row.findViewById<TextView>(R.id.tvPageCount).text =
                    if (sub.pageCount > 0) "${sub.pageCount} Pages" else "Page count unavailable"
                binding.containerList.addView(row)
            }
        }
    }
}