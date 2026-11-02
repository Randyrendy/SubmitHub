package com.submithub.app.ui.lecturer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submithub.app.data.entity.SubmissionEntity
import com.submithub.app.databinding.ItemSubmissionBinding
import com.submithub.app.util.DateTimeUtils

class SubmissionAdapter(
    private val onClick: (SubmissionEntity) -> Unit,
    private val onConfirmToggle: (SubmissionEntity) -> Unit
) : ListAdapter<SubmissionEntity, SubmissionAdapter.SubmissionViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val binding = ItemSubmissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubmissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubmissionViewHolder(private val binding: ItemSubmissionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(submission: SubmissionEntity) {
            binding.tvStudentName.text = submission.studentName
            binding.tvStudentId.text = submission.studentId
            binding.tvInitials.text = initialsFor(submission.studentName)
            binding.tvFileName.text = submission.fileName
            binding.tvSubmittedAt.text = DateTimeUtils.format(submission.submittedAt)

            if (submission.isGraded) {
                binding.tvStatusChip.text = "Graded: ${submission.totalScore}/100"
                binding.tvStatusChip.setBackgroundResource(com.submithub.app.R.drawable.bg_chip_success)
                binding.tvStatusChip.setTextColor(
                    binding.root.context.getColor(com.submithub.app.R.color.success)
                )
            } else {
                binding.tvStatusChip.text = "Pending"
                binding.tvStatusChip.setBackgroundResource(com.submithub.app.R.drawable.bg_chip_warning)
                binding.tvStatusChip.setTextColor(
                    binding.root.context.getColor(com.submithub.app.R.color.warning)
                )
            }

            updateConfirmChip(submission.isConfirmed)

            binding.tvConfirmChip.setOnClickListener {
                onConfirmToggle(submission)
            }

            binding.root.setOnClickListener { onClick(submission) }
        }

        private fun updateConfirmChip(confirmed: Boolean) {
            if (confirmed) {
                binding.tvConfirmChip.text = "Confirmed"
                binding.tvConfirmChip.setBackgroundResource(com.submithub.app.R.drawable.bg_chip_success)
                binding.tvConfirmChip.setTextColor(
                    binding.root.context.getColor(com.submithub.app.R.color.success)
                )
            } else {
                binding.tvConfirmChip.text = "Confirm"
                binding.tvConfirmChip.setBackgroundResource(com.submithub.app.R.drawable.bg_chip_warning)
                binding.tvConfirmChip.setTextColor(
                    binding.root.context.getColor(com.submithub.app.R.color.warning)
                )
            }
        }

        private fun initialsFor(name: String): String {
            val parts = name.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
                parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
                else -> "?"
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<SubmissionEntity>() {
            override fun areItemsTheSame(oldItem: SubmissionEntity, newItem: SubmissionEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SubmissionEntity, newItem: SubmissionEntity) = oldItem == newItem
        }
    }
}