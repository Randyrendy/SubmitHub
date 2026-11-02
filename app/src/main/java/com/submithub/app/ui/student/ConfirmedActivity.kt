package com.submithub.app.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.submithub.app.R
import com.submithub.app.data.Repository
import com.submithub.app.databinding.ActivityConfirmedBinding

class ConfirmedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmedBinding
    private lateinit var repository: Repository
    private var studentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        studentId = intent.getStringExtra("studentId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        repository.submissionsForStudent(studentId).observe(this) { submissions ->
            binding.containerList.removeAllViews()
            binding.tvEmpty.visibility = if (submissions.isEmpty()) View.VISIBLE else View.GONE

            submissions.sortedByDescending { it.submittedAt }.forEach { sub ->
                val row = LayoutInflater.from(this)
                    .inflate(R.layout.item_confirmed_pdf, binding.containerList, false)
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

                binding.containerList.addView(row)
            }
        }
    }
}