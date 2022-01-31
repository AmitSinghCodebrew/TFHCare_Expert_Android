package com.consultantvendor.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.databinding.RvItemCheckBinding


class CheckItemAdapter(private val fragment: Fragment, private val isMultiSelect: Boolean,
                       private val items: ArrayList<Filter>) : RecyclerView.Adapter<CheckItemAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.rv_item_check, parent, false))
    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(val binding: RvItemCheckBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Filter) = with(binding) {

            tvName.text = item.option_name ?: item.name

            if (item.isSelected) {
                tvName.setBackgroundResource(R.drawable.drawable_theme_64)
                tvName.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorWhite))
            } else {
                tvName.setBackgroundResource(R.drawable.drawable_stroke_inactive)
                tvName.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorBlack))
            }

            clMain.setOnClickListener {
                val pos = adapterPosition
                if (isMultiSelect)
                    items[pos].isSelected = !items[pos].isSelected
                else
                    items.forEachIndexed { index, filterOption ->
                        items[index].isSelected = pos == index
                    }
                notifyDataSetChanged()
            }
        }
    }
}

