package com.consultantvendor.ui.dashboard.home.prescription.digital

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.DigitalPrescription
import com.consultantvendor.databinding.RvItemPrescriptionBinding


class ItemPrescriptionAdapter(private val fragment: DigitalPrescriptionFragment, private val items: ArrayList<DigitalPrescription>) :
        RecyclerView.Adapter<ItemPrescriptionAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.rv_item_prescription, parent, false))
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: RvItemPrescriptionBinding) :
            RecyclerView.ViewHolder(binding.root) {

        val context = binding.root.context

        init {
            binding.tvEdit.setOnClickListener {
                fragment.editPrescription(adapterPosition)
            }

            binding.tvDelete.setOnClickListener {
                fragment.deletePrescription(adapterPosition)
            }
        }

        fun bind(item: DigitalPrescription) = with(binding) {
            tvName.text = "${item.medicine_name}\n(${item.duration}) (${item.dosage_type})"
        }
    }
}
