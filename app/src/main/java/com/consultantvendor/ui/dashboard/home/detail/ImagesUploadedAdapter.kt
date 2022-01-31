package com.consultantvendor.ui.dashboard.home.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.network.Config
import com.consultantvendor.databinding.RvItemImageBinding
import com.consultantvendor.utils.ImageFolder
import com.consultantvendor.utils.loadImage
import com.consultantvendor.utils.pxFromDp
import com.consultantvendor.utils.viewImageFull


class ImagesUploadedAdapter(private val fragment: Fragment, private val items: ArrayList<String>)
    : RecyclerView.Adapter<ImagesUploadedAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.rv_item_image, parent, false
            )
        )

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: RvItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            val heightOfImage = pxFromDp(fragment.requireContext(), 100f).toInt()
            binding.ivImage.layoutParams.height = heightOfImage
            binding.ivImage.layoutParams.width = heightOfImage

            binding.ivImage.setOnClickListener {
                val itemImages = ArrayList<String>()
                items.forEach {
                    itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${it}")
                }
                viewImageFull(fragment.requireActivity(), itemImages, adapterPosition)
            }
        }


        fun bind(item: String?) = with(binding) {
            loadImage(binding.ivImage, item, R.drawable.image_placeholder)
        }
    }

}
