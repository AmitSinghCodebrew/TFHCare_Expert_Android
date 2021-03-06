package com.consultantvendor.ui.loginSignUp.insurance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.appdetails.Insurance
import com.consultantvendor.databinding.ItemInsuranceBinding
import com.consultantvendor.ui.loginSignUp.signup.SignUpFragment


class InsuranceAdapter(private val fragment: Fragment, private val items: ArrayList<Insurance>) :
        RecyclerView.Adapter<InsuranceAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_insurance, parent, false
                )
        )

    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(val binding: ItemInsuranceBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Insurance) = with(binding) {

            cbName.text = item.name ?: ""
            cbName.isChecked = item.isSelected
            if (item.isSelected) {
                clInsurance.setBackgroundResource(R.color.colorPrimary)
            } else {
                clInsurance.setBackgroundResource(R.color.colorWhite)
            }

            clInsurance.setOnClickListener {
                /*Single Selection*/
                /*items.forEachIndexed { index, service ->
                    items[index].isSelected = index == adapterPosition
                }*/

                /*Multiple Selection*/
                items[adapterPosition].isSelected = !items[adapterPosition].isSelected
                notifyDataSetChanged()

                if(fragment is SignUpFragment)
                    fragment.setLanguageNames()
            }

        }
    }

}
