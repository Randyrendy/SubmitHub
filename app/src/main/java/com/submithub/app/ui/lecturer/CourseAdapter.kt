package com.submithub.app.ui.lecturer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submithub.app.databinding.ItemCourseBinding

data class CourseSummary(
    val courseCode: String,
    val courseTitle: String,
    val department: String,
    val assignmentCount: Int,
    val activeCount: Int
)

class CourseAdapter(
    private val onClick: (CourseSummary) -> Unit
) : ListAdapter<CourseSummary, CourseAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(course: CourseSummary) {
            binding.tvCourseCode.text = course.courseCode
            binding.tvCourseTitle.text = course.courseTitle
            binding.tvDepartment.text = course.department
            binding.tvAssignmentCount.text = "${course.assignmentCount} assignment(s)"
            binding.tvActiveCount.text = "${course.activeCount} active"
            binding.btnViewDetails.setOnClickListener { onClick(course) }
            binding.root.setOnClickListener { onClick(course) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CourseSummary>() {
            override fun areItemsTheSame(oldItem: CourseSummary, newItem: CourseSummary) =
                oldItem.courseCode == newItem.courseCode
            override fun areContentsTheSame(oldItem: CourseSummary, newItem: CourseSummary) =
                oldItem == newItem
        }
    }
}