package com.submithub.app.ui.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.submithub.app.data.Repository
import com.submithub.app.databinding.ActivityMySubmissionsBinding

class MySubmissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMySubmissionsBinding
    private lateinit var repository: Repository
    private lateinit var adapter: MySubmissionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySubmissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        val studentId = intent.getStringExtra("studentId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        adapter = MySubmissionAdapter { submission ->
            if (submission.fileUri.isNotBlank()) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(submission.fileUri)))
                } catch (e: Exception) {
                    Toast.makeText(this, "Couldn't open the file", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "File not available", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvSubmissions.layoutManager = LinearLayoutManager(this)
        binding.rvSubmissions.adapter = adapter

        repository.submissionsForStudent(studentId).observe(this) { submissions ->
            adapter.submitList(submissions)
            binding.tvEmpty.visibility = if (submissions.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}