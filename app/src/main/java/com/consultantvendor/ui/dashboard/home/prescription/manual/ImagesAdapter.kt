package com.consultantvendor.ui.dashboard.home.prescription.manual

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.DocImage
import com.consultantvendor.databinding.RvItemImageBinding
import com.consultantvendor.utils.gone
import com.consultantvendor.utils.loadImage
import com.consultantvendor.utils.visible


class ImagesAdapter(private val fragment: ManualPrescriptionFragment, private val items: ArrayList<DocImage>) :
        RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    val MAX_ITEM = 3

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items.isEmpty() || (position == 0 && items.size < MAX_ITEM))
            holder.bind(null)
        else if (items.size == MAX_ITEM)
            holder.bind(items[position])
        else
            holder.bind(items[position - 1])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.rv_item_image, parent, false))

    }

    override fun getItemCount(): Int = if (items.size == MAX_ITEM) items.size else items.size + 1

    inner class ViewHolder(val binding: RvItemImageBinding) :
            RecyclerView.ViewHolder(binding.root) {

        val context = binding.root.context

        init {
            binding.ivImage.setOnClickListener {
                if (items.size < MAX_ITEM && bindingAdapterPosition == 0) {
                    fragment.clickItem()
                }
            }

            binding.ivDelete.setOnClickListener {
                if (items.size == MAX_ITEM)
                    items.removeAt(bindingAdapterPosition)
                else
                    items.removeAt(bindingAdapterPosition - 1)

                notifyDataSetChanged()
            }
        }


        fun bind(item: DocImage?) = with(binding) {
            ivImage.setBackgroundResource(R.drawable.drawable_grey_stroke)
            if (items.size < MAX_ITEM && bindingAdapterPosition == 0) {
                ivImage.setImageResource(R.drawable.ic_camera)
                ivDelete.gone()
            } else {
                when {
                    item?.imageFile!=null -> {
                        Glide.with(context).load(item.imageFile).into(ivImage)
                        ivDelete.visible()
                    }
                    !item?.image.isNullOrEmpty() -> {
                        Glide.with(context).load(item).into(ivImage)
                        loadImage(binding.ivImage, item?.image, R.drawable.image_placeholder)
                        ivDelete.visible()
                    }
                    else -> {
                        ivImage.setImageResource(R.drawable.ic_camera)
                        ivDelete.gone()
                    }
                }
            }
        }
    }

}