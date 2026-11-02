package com.submithub.app.ui.lecturer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.submithub.app.R
import com.submithub.app.data.Repository
import com.submithub.app.databinding.ActivityAnalyticsBinding
import kotlinx.coroutines.launch

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        val lecturerId = intent.getStringExtra("lecturerId") ?: ""

        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val data = repository.getAnalyticsData(lecturerId)

            binding.tvTotalSubmissions.text = data.totalSubmissions.toString()
            binding.tvAverageGrade.text = if (data.averageGrade != null) {
                String.format("%.1f", data.averageGrade)
            } else {
                "—"
            }
            binding.tvGradedRate.text = "${data.gradedRatePercent}%"
            binding.tvLateRate.text = "${data.lateRatePercent}%"

            renderBars(
                container = binding.containerGradeDistribution,
                emptyView = binding.tvNoGrades,
                entries = data.gradeDistribution
            )

            renderBars(
                container = binding.containerByCourse,
                emptyView = binding.tvNoSubmissions,
                entries = data.submissionsByCourse
            )
        }
    }

    private fun renderBars(container: LinearLayout, emptyView: View, entries: List<Pair<String, Int>>) {
        container.removeAllViews()
        val maxValue = entries.maxOfOrNull { it.second } ?: 0

        if (maxValue == 0) {
            emptyView.visibility = View.VISIBLE
            container.visibility = View.GONE
            return
        }

        emptyView.visibility = View.GONE
        container.visibility = View.VISIBLE

        entries.forEach { (label, count) ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_analytics_bar, container, false)
            row.findViewById<TextView>(R.id.tvLabel).text = label
            row.findViewById<TextView>(R.id.tvCount).text = count.toString()

            val barFill = row.findViewById<View>(R.id.barFill)
            val barTrack = row.findViewById<LinearLayout>(R.id.barTrack)
            val fillParams = barFill.layoutParams as LinearLayout.LayoutParams
            fillParams.weight = count.toFloat()
            barFill.layoutParams = fillParams

            val spacer = barTrack.getChildAt(1)
            val spacerParams = spacer.layoutParams as LinearLayout.LayoutParams
            spacerParams.weight = (maxValue - count).toFloat().coerceAtLeast(0f)
            spacer.layoutParams = spacerParams

            container.addView(row)
        }
    }
}