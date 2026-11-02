package com.submithub.app.ui.lecturer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.submithub.app.databinding.ItemIconOptionBinding
import com.submithub.app.util.IconCatalog

class IconPickerAdapter(
    private val options: List<IconCatalog.IconOption> = IconCatalog.options,
    private val onSelected: (IconCatalog.IconOption) -> Unit
) : RecyclerView.Adapter<IconPickerAdapter.IconViewHolder>() {

    private var selectedKey: String = options.first().key

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemIconOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int = options.size

    fun selectedIconKey(): String = selectedKey

    inner class IconViewHolder(private val binding: ItemIconOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(option: IconCatalog.IconOption) {
            binding.ivIcon.setImageResource(option.resId)
            binding.tvLabel.text = option.label
            val isSelected = option.key == selectedKey
            binding.iconCircle.setBackgroundResource(
                if (isSelected) com.submithub.app.R.drawable.bg_icon_circle_selected
                else com.submithub.app.R.drawable.bg_icon_circle
            )
            binding.root.setOnClickListener {
                selectedKey = option.key
                onSelected(option)
                notifyDataSetChanged()
            }
        }
    }
}
