package com.submithub.app.ui.student

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.SubmissionEntity
import com.submithub.app.databinding.ActivitySubmitAssignmentBinding
import com.submithub.app.util.DateTimeUtils
import com.submithub.app.util.IconCatalog
import com.submithub.app.util.SupabaseStorage
import kotlinx.coroutines.launch

class SubmitAssignmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmitAssignmentBinding
    private lateinit var repository: Repository

    private var codeId: String = ""
    private var studentId: String = ""
    private var studentName: String = ""
    private var pickedUri: Uri? = null
    private var pickedFileName: String = ""

    private val filePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pickedUri = uri
            pickedFileName = queryFileName(uri) ?: "assignment_file"
            binding.tvSelectedFile.text = pickedFileName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        codeId = intent.getStringExtra("codeId") ?: ""
        studentId = intent.getStringExtra("studentId") ?: ""
        studentName = intent.getStringExtra("studentName") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val code = repository.findCode(codeId)
            if (code != null) {
                binding.ivIcon.setImageResource(IconCatalog.resFor(code.iconKey))
                binding.tvCourse.text = "${code.courseCode} - ${code.courseTitle}"
                binding.tvAssignmentTitle.text = code.assignmentTitle
                binding.tvDue.text = "Due: ${DateTimeUtils.format(code.expiresAt)}"
            }
        }

        binding.btnPickFile.setOnClickListener {
            filePicker.launch(arrayOf("*/*"))
        }

        binding.btnSubmit.setOnClickListener { onSubmitClicked() }
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }

    private fun onSubmitClicked() {
        val uri = pickedUri
        if (uri == null) {
            Toast.makeText(this, "Please choose a file first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Checking..."

        lifecycleScope.launch {
            try {
                val code = repository.findCode(codeId)
                if (code == null) {
                    Toast.makeText(this@SubmitAssignmentActivity, "Code no longer exists", Toast.LENGTH_SHORT).show()
                    resetSubmitButton()
                    return@launch
                }
                if (DateTimeUtils.isPast(code.expiresAt)) {
                    Toast.makeText(this@SubmitAssignmentActivity, "Submission window has closed", Toast.LENGTH_SHORT).show()
                    resetSubmitButton()
                    return@launch
                }
                if (repository.hasAlreadySubmitted(codeId, studentId)) {
                    Toast.makeText(this@SubmitAssignmentActivity, "You already submitted this assignment", Toast.LENGTH_SHORT).show()
                    resetSubmitButton()
                    return@launch
                }

                binding.btnSubmit.text = "Uploading... 0%"

                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val storagePath = "$codeId/${studentId}_${System.currentTimeMillis()}_$pickedFileName"
                val downloadUrl = SupabaseStorage.uploadFile(
                    context = this@SubmitAssignmentActivity,
                    uri = uri,
                    path = storagePath,
                    mimeType = mimeType,
                    onProgress = { percent ->
                        runOnUiThread { binding.btnSubmit.text = "Uploading... $percent%" }
                    }
                )

                binding.btnSubmit.text = "Saving..."

                repository.submit(
                    SubmissionEntity(
                        codeId = codeId,
                        courseTitle = code.courseTitle,
                        assignmentTitle = code.assignmentTitle,
                        iconKey = code.iconKey,
                        studentId = studentId,
                        studentName = studentName,
                        fileName = pickedFileName,
                        fileUri = downloadUrl,
                        submittedAt = System.currentTimeMillis()
                    )
                )

                Toast.makeText(this@SubmitAssignmentActivity, "Assignment submitted!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@SubmitAssignmentActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                resetSubmitButton()
            }
        }
    }

    private fun resetSubmitButton() {
        binding.btnSubmit.isEnabled = true
        binding.btnSubmit.text = getString(com.submithub.app.R.string.btn_submit)
    }
}