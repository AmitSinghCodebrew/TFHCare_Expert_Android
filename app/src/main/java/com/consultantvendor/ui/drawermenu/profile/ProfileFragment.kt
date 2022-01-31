package com.consultantvendor.ui.drawermenu.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.SaveAddress
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.Config
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentProfileBinding
import com.consultantvendor.ui.dashboard.home.AppointmentViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import permissions.dispatcher.*
import javax.inject.Inject

class ProfileFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentProfileBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var viewModelLogin: LoginViewModel

    private var userData: UserData? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
            rootView = binding.root

            initialise()
            setUserProfile()
            hiApiDoctorDetail()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        viewModelLogin = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        val userAddress = prefsManager.getObject(USER_ADDRESS, SaveAddress::class.java)
        if (userAddress != null) {
            binding.tvLocation.text = userAddress.locationName
            binding.tvLocation.visible()
        } else
            binding.tvLocation.gone()
    }

    private fun hiApiDoctorDetail() {
        if (isConnectedToInternet(requireContext(), true)) {
            viewModelLogin.profile()
        }
    }

    private fun setUserProfile() {
        userData = userRepository.getUser()

        binding.tvName.text = getDoctorName(userData)
        binding.tvBioV.text = userData?.profile?.bio ?: getString(R.string.na)
        binding.tvEmailV.text = userData?.email ?: getString(R.string.na)
        binding.tvPhoneV.text = "${userData?.country_code ?: ""} ${userData?.phone ?: ""}"
        binding.tvDOBV.text = userData?.profile?.dob ?: getString(R.string.na)
        binding.tvDesc.text = userData?.categoryData?.name ?: getString(R.string.na)

        binding.tvRating.text = getString(R.string.s_s_reviews,
                getUserRating(userData?.totalRating),
                userData?.reviewCount)

        if (userData?.consultationCount.isNullOrEmpty() || userData?.consultationCount == "0") {
            binding.tvPatient.gone()
            binding.tvPatientV.gone()
        } else {
            binding.tvPatient.visible()
            binding.tvPatientV.visible()
            binding.tvPatientV.text = userData?.consultationCount ?: getString(R.string.na)
        }


        binding.tvExperienceV.text =
                "${getAge(userData?.profile?.working_since)} ${getString(R.string.years)}"
        binding.tvReviewsV.text = userData?.reviewCount ?: getString(R.string.na)

        if (!userData?.profile?.dob.isNullOrEmpty())
            binding.tvDOBV.text = DateUtils.dateFormatChange(DateFormat.DATE_FORMAT,
                    DateFormat.MON_DAY_YEAR, userData?.profile?.dob ?: "")

        loadImage(binding.ivPic, userData?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvSetPrefrences.hideShowView(userData?.filters?.isNotEmpty() == true)
        binding.tvDocuments.hideShowView(userData?.categoryData?.is_additionals == true)

    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.tvEdit.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_PROFILE, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvPhoneUpdate.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_NUMBER, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvSetAvailability.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(CATEGORY_PARENT_ID, userData?.categoryData)
                    .putExtra(UPDATE_AVAILABILITY, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvSetPrefrences.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(CATEGORY_PARENT_ID, userData?.categoryData)
                    .putExtra(UPDATE_PREFRENCES, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvDocuments.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(CATEGORY_PARENT_ID, userData?.categoryData)
                    .putExtra(DocumentsFragment.UPDATE_DOCUMENTS, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvUpdateCategory.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_CATEGORY, true), AppRequestCode.PROFILE_UPDATE)
        }


        binding.ivPic.setOnClickListener {
            val itemImages = java.util.ArrayList<String>()
            itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${userRepository.getUser()?.profile_image}")
            viewImageFull(context as Activity, itemImages, 0)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.PROFILE_UPDATE) {
                setUserProfile()
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }
    }


    private fun bindObservers() {
        viewModelLogin.profile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    setUserProfile()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(false)
                }
            }
        })
    }
}
