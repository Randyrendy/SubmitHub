package com.submithub.app.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submithub.app.data.entity.SubmissionEntity
import com.submithub.app.databinding.ItemMySubmissionBinding
import com.submithub.app.util.DateTimeUtils
import com.submithub.app.util.IconCatalog

class MySubmissionAdapter(
    private val onClick: (SubmissionEntity) -> Unit
) : ListAdapter<SubmissionEntity, MySubmissionAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMySubmissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMySubmissionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(submission: SubmissionEntity) {
            binding.ivIcon.setImageResource(IconCatalog.resFor(submission.iconKey))
            binding.tvCourseTitle.text = submission.courseTitle
            binding.tvAssignmentTitle.text = submission.assignmentTitle
            binding.tvFileName.text = submission.fileName
            binding.tvSubmittedAt.text = "Submitted ${DateTimeUtils.format(submission.submittedAt)}"
            binding.root.setOnClickListener { onClick(submission) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<SubmissionEntity>() {
            override fun areItemsTheSame(oldItem: SubmissionEntity, newItem: SubmissionEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SubmissionEntity, newItem: SubmissionEntity) = oldItem == newItem
        }
    }
}