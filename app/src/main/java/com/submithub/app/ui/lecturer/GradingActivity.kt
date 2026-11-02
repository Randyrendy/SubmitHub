package com.submithub.app.ui.lecturer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.SubmissionEntity
import com.submithub.app.databinding.ActivityGradingBinding
import com.submithub.app.util.DateTimeUtils
import kotlinx.coroutines.launch

class GradingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGradingBinding
    private lateinit var repository: Repository
    private lateinit var submissionId: String
    private var fileUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        submissionId = intent.getStringExtra("submissionId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = updateTotal()
        }
        binding.etCorrectness.addTextChangedListener(watcher)
        binding.etCodeQuality.addTextChangedListener(watcher)
        binding.etDocumentation.addTextChangedListener(watcher)
        binding.etTesting.addTextChangedListener(watcher)

        binding.btnSaveGrade.setOnClickListener { onSaveClicked() }

        loadSubmission()
    }

    private fun loadSubmission() {
        lifecycleScope.launch {
            val submission = repository.getSubmission(submissionId) ?: run {
                Toast.makeText(this@GradingActivity, "Submission not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            bindSubmission(submission)
        }
    }

    private fun bindSubmission(submission: SubmissionEntity) {
        binding.tvStudentInfo.text =
            "${submission.studentName} — ${submission.studentId} · Submitted ${DateTimeUtils.format(submission.submittedAt)}"
        binding.tvFileName.text = submission.fileName
        fileUri = submission.fileUri

        binding.cardFile.setOnClickListener {
            if (fileUri.isNotBlank()) {
                try {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(fileUri)))
                } catch (e: Exception) {
                    Toast.makeText(this, "Couldn't open the file", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "File not available", Toast.LENGTH_SHORT).show()
            }
        }

        submission.correctnessScore?.let { binding.etCorrectness.setText(it.toString()) }
        submission.codeQualityScore?.let { binding.etCodeQuality.setText(it.toString()) }
        submission.documentationScore?.let { binding.etDocumentation.setText(it.toString()) }
        submission.testingScore?.let { binding.etTesting.setText(it.toString()) }
        submission.feedback?.let { binding.etFeedback.setText(it) }

        updateTotal()
    }

    private fun updateTotal() {
        val correctness = binding.etCorrectness.text.toString().toIntOrNull() ?: 0
        val codeQuality = binding.etCodeQuality.text.toString().toIntOrNull() ?: 0
        val documentation = binding.etDocumentation.text.toString().toIntOrNull() ?: 0
        val testing = binding.etTesting.text.toString().toIntOrNull() ?: 0
        val total = correctness + codeQuality + documentation + testing
        binding.tvTotal.text = "$total / 100"
    }

    private fun onSaveClicked() {
        val correctness = binding.etCorrectness.text.toString().toIntOrNull()
        val codeQuality = binding.etCodeQuality.text.toString().toIntOrNull()
        val documentation = binding.etDocumentation.text.toString().toIntOrNull()
        val testing = binding.etTesting.text.toString().toIntOrNull()
        val feedback = binding.etFeedback.text.toString().trim()

        binding.tvError.visibility = View.GONE

        if (correctness == null || codeQuality == null || documentation == null || testing == null) {
            binding.tvError.text = "Please enter a score for every rubric item"
            binding.tvError.visibility = View.VISIBLE
            return
        }

        if (correctness !in 0..40 || codeQuality !in 0..20 || documentation !in 0..20 || testing !in 0..20) {
            binding.tvError.text = "Scores must be within each item's max (see the /XX label)"
            binding.tvError.visibility = View.VISIBLE
            return
        }

        binding.btnSaveGrade.isEnabled = false

        lifecycleScope.launch {
            repository.gradeSubmission(
                submissionId = submissionId,
                correctnessScore = correctness,
                codeQualityScore = codeQuality,
                documentationScore = documentation,
                testingScore = testing,
                feedback = feedback
            )
            Toast.makeText(this@GradingActivity, "Grade saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}