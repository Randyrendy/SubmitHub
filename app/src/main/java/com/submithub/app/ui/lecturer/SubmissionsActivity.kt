package com.submithub.app.ui.lecturer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.submithub.app.data.Repository
import com.submithub.app.databinding.ActivitySubmissionsBinding
import com.submithub.app.util.DateTimeUtils
import com.submithub.app.util.IconCatalog
import kotlinx.coroutines.launch

class SubmissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmissionsBinding
    private lateinit var repository: Repository
    private lateinit var adapter: SubmissionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        val codeId = intent.getStringExtra("codeId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        adapter = SubmissionAdapter(
            onClick = { submission ->
                val intent = android.content.Intent(this, GradingActivity::class.java)
                intent.putExtra("submissionId", submission.id)
                startActivity(intent)
            },
            onConfirmToggle = { submission ->
                lifecycleScope.launch {
                    repository.setSubmissionConfirmed(submission.id, !submission.isConfirmed)
                }
            }
        )
        binding.rvSubmissions.layoutManager = LinearLayoutManager(this)
        binding.rvSubmissions.adapter = adapter

        lifecycleScope.launch {
            val code = repository.findCode(codeId)
            if (code != null) {
                binding.ivIcon.setImageResource(IconCatalog.resFor(code.iconKey))
                binding.tvCourseTitle.text = code.courseTitle
                binding.tvAssignmentTitle.text = code.assignmentTitle
                binding.tvCodeId.text = "Code: ${code.id}"
                binding.tvDue.text = "Due: ${DateTimeUtils.format(code.expiresAt)}"
                val expired = DateTimeUtils.isPast(code.expiresAt)
                binding.tvStatus.text = if (expired) "Expired" else "Active"
                binding.tvStatus.setBackgroundResource(
                    if (expired) com.submithub.app.R.drawable.bg_chip_error else com.submithub.app.R.drawable.bg_chip_success
                )
                binding.tvStatus.setTextColor(
                    getColor(if (expired) com.submithub.app.R.color.error else com.submithub.app.R.color.success)
                )
            }
        }

        repository.submissionsForCode(codeId).observe(this) { submissions ->
            adapter.submitList(submissions)
            binding.tvCount.text = "${submissions.size} submission(s)"
            binding.tvEmpty.visibility = if (submissions.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}
