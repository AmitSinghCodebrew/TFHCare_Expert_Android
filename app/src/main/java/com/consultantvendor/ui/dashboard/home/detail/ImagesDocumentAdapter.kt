package com.consultantvendor.ui.dashboard.home.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.DocImage
import com.consultantvendor.data.network.Config
import com.consultantvendor.databinding.RvItemImageBinding
import com.consultantvendor.utils.*


class ImagesDocumentAdapter(private val fragment: Fragment, private val items: ArrayList<DocImage>)
    : RecyclerView.Adapter<ImagesDocumentAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.rv_item_image, parent, false))

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: RvItemImageBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            val heightOfImage = pxFromDp(fragment.requireContext(), 100f).toInt()
            binding.ivImage.layoutParams.height = heightOfImage
            binding.ivImage.layoutParams.width = heightOfImage

            binding.ivImage.setOnClickListener {
                val item = items[adapterPosition]
                when (item.type) {
                    DocType.IMAGE -> {
                        val itemImages = ArrayList<String>()
                        itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${item.image}")
                        viewImageFull(fragment.requireActivity(), itemImages, adapterPosition)
                    }
                    DocType.PDF -> {
                        val link = "${Config.imageURL}${ImageFolder.PDF}${item.image}"
                        openPdf(fragment.requireActivity(), link)
                    }
                }
            }
        }


        fun bind(item: DocImage?) = with(binding) {

            if (item?.type == DocType.PDF) {
                ivImage.setBackgroundResource(R.drawable.ic_pdf)
                Glide.with(binding.root.context).load("").into(ivImage)
            } else
                loadImage(binding.ivImage, item?.image, R.drawable.image_placeholder)
        }
    }

}
