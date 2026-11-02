package com.submithub.app.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submithub.app.data.entity.UserEntity
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ItemAdminUserBinding

class AdminUserAdapter(
    private val onDelete: (UserEntity) -> Unit
) : ListAdapter<UserEntity, AdminUserAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAdminUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserEntity) {
            binding.tvName.text = user.name
            binding.tvIdentifier.text = user.identifierLabel
            binding.tvRole.text = user.role.name
            binding.ivRoleIcon.setImageResource(
                if (user.role == UserRole.LECTURER) com.submithub.app.R.drawable.ic_lecturer
                else com.submithub.app.R.drawable.ic_student
            )
            binding.btnDelete.setOnClickListener { onDelete(user) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<UserEntity>() {
            override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity) = oldItem == newItem
        }
    }
}