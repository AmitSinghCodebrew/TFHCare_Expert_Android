package com.consultantvendor.ui.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.consultantvendor.R
import com.consultantvendor.databinding.FragmentWalkthroughDetailBinding
import com.consultantvendor.utils.POSITION
import dagger.android.support.DaggerFragment


class WalkthroughDetailFragment : DaggerFragment() {

    private lateinit var binding: FragmentWalkthroughDetailBinding

    private var rootView: View? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_walkthrough_detail, container, false)
            rootView = binding.root

            initialise()
        }
        return rootView
    }

    private fun initialise() {
        when (arguments?.getInt(POSITION)) {
            0 -> {
                binding.ivImage.setImageResource(R.drawable.ic_1)
                binding.tvTitle.text = getString(R.string.walkthrough_1)
                binding.tvDesc.text = getString(R.string.walk_through_desc_1)
            }
            1 -> {
                binding.ivImage.setImageResource(R.drawable.ic_2)
                binding.tvTitle.text = getString(R.string.walkthrough_2)
                binding.tvDesc.text = getString(R.string.walk_through_desc_2)
            }
            2 -> {
                binding.ivImage.setImageResource(R.drawable.ic_3)
                binding.tvTitle.text = getString(R.string.walkthrough_3)
                binding.tvDesc.text = getString(R.string.walk_through_desc_3)
            }
            3 -> {
                binding.ivImage.setImageResource(R.drawable.ic_4)
                binding.tvTitle.text = getString(R.string.walkthrough_4)
                binding.tvDesc.text = getString(R.string.walk_through_desc_4)
            }
        }
    }
}
