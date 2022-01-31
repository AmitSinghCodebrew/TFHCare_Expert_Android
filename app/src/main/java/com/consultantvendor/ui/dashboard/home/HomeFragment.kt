package com.consultantvendor.ui.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.consultantvendor.R
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentHomeBinding
import com.consultantvendor.ui.adapter.CommonFragmentPagerAdapter
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class HomeFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentHomeBinding

    private var rootView: View? = null

    private lateinit var adapter: CommonFragmentPagerAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
            rootView = binding.root

            initialise()
            listeners()
        }
        return rootView
    }


    private fun initialise() {

        adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
        val titles = arrayOf(getString(R.string.all_Requests))

        titles.forEachIndexed { index, s ->

            adapter.addTab(titles[index],  AppointmentFragment())
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1

        binding.tabLayout.setupWithViewPager(binding.viewPager)

    }

    private fun listeners() {

    }
}