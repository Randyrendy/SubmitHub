package com.submithub.app.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submithub.app.data.entity.CodeEntity
import com.submithub.app.databinding.ItemAdminCodeBinding
import com.submithub.app.util.DateTimeUtils
import com.submithub.app.util.IconCatalog

class AdminCodeAdapter(
    private val onDelete: (CodeEntity) -> Unit
) : ListAdapter<CodeEntity, AdminCodeAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminCodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAdminCodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(code: CodeEntity) {
            binding.ivIcon.setImageResource(IconCatalog.resFor(code.iconKey))
            binding.tvCourseTitle.text = code.courseTitle
            binding.tvAssignmentTitle.text = code.assignmentTitle
            binding.tvLecturer.text = "By ${code.createdByLecturerName}"
            binding.tvCode.text = code.id

            val expired = DateTimeUtils.isPast(code.expiresAt)
            binding.tvStatus.text = if (expired) "Expired" else "Active"
            binding.tvStatus.setBackgroundResource(
                if (expired) com.submithub.app.R.drawable.bg_chip_error
                else com.submithub.app.R.drawable.bg_chip_success
            )
            binding.tvStatus.setTextColor(
                binding.root.context.getColor(
                    if (expired) com.submithub.app.R.color.error
                    else com.submithub.app.R.color.success
                )
            )

            binding.btnDelete.setOnClickListener { onDelete(code) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CodeEntity>() {
            override fun areItemsTheSame(oldItem: CodeEntity, newItem: CodeEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CodeEntity, newItem: CodeEntity) = oldItem == newItem
        }
    }
}