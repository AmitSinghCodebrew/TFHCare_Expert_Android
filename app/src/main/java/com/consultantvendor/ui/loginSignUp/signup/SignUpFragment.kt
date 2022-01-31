package com.consultantvendor.ui.loginSignUp.signup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.consultantvendor.R
import com.consultantvendor.appClientDetails
import com.consultantvendor.data.models.requests.SetFilter
import com.consultantvendor.data.models.responses.CountryCity
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.models.responses.appdetails.Insurance
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentSignupBinding
import com.consultantvendor.ui.AppVersionViewModel
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.category.CategoryFragment
import com.consultantvendor.ui.loginSignUp.insurance.CustomSpinnerAdapter
import com.consultantvendor.ui.loginSignUp.insurance.InsuranceAdapter
import com.consultantvendor.ui.loginSignUp.insurance.InsuranceFragment
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.fragment_signup.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject

@RuntimePermissions
class SignUpFragment : DaggerFragment(), OnDateSelected {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentSignupBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelClass: ClassesViewModel

    private lateinit var viewModelVersion: AppVersionViewModel

    private var userData: UserData? = null

    private var isUpdate = false

    private var fileToUpload: File? = null

    private var selectedDob = true

    private var openFirstGender = true

    private var spinnerGenderAdapter: CustomSpinnerAdapter? = null

    private val itemsGender = ArrayList<CountryCity>()

    private var prefrences: ArrayList<Filter>? = null

    private val itemsLanguage = ArrayList<Insurance>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setEditInformation()
            bindObservers()
            setAdpater()
        }
        return rootView
    }


    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        viewModelClass = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        viewModelVersion = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

    }

    private fun setAdpater() {
        spinnerGenderAdapter = CustomSpinnerAdapter(requireContext(), itemsGender)
        binding.spnGender.adapter = spinnerGenderAdapter

        val hashMap = HashMap<String, String>()
        hashMap["type"] = PreferencesType.ALL
        viewModelVersion.preferences(hashMap)
    }

    private fun setEditInformation() {
        editTextScroll(binding.etBio)
        userData = userRepository.getUser()

        if (arguments?.containsKey(UPDATE_PROFILE) == true) {
            binding.tvTitle.text = getString(R.string.update)
            binding.tvNext.text = getString(R.string.update)

            val title = userData?.profile?.title ?: getString(R.string.title)
            val list = resources.getStringArray(R.array.dr_title)
            binding.spnTitle.setSelection(list.indexOf(title))

            binding.etName.setText(userData?.name ?: "")
            binding.etBio.setText(userData?.profile?.bio ?: "")
            binding.etEmail.setText(userData?.email ?: "")

            if (!userData?.profile?.dob.isNullOrEmpty())
                binding.etDob.setText(DateUtils.dateFormatChange(DateFormat.DATE_FORMAT,
                        DateFormat.DATE_FORMAT_SLASH, userData?.profile?.dob ?: ""))

            if (!userData?.profile?.working_since.isNullOrEmpty())
                binding.etYears.setText(DateUtils.dateFormatChange(DateFormat.DATE_FORMAT,
                        DateFormat.DATE_FORMAT_SLASH, userData?.profile?.working_since ?: ""))

            loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

            userData?.master_preferences?.forEach {
                when (it.preference_name) {
                    PreferencesType.GENDER -> {
                        it.options?.forEachIndexed { index, filterOption ->
                            if (filterOption.isSelected)
                                binding.etGender.setText(filterOption.option_name)
                        }
                    }
                }
            }

            userData?.custom_fields?.forEach {
                if (it.field_name == CustomFields.QUALIFICATION) {
                    binding.etQualification.setText(it.field_value ?: "")
                    return@forEach
                }
            }

            binding.ilPassword.gone()
            binding.ilConfirmPassword.gone()

            isUpdate = true
        } else if (arguments?.containsKey(UPDATE_NUMBER) == true) {
            binding.ilPassword.gone()
            binding.ilConfirmPassword.gone()

            binding.etName.setText(userData?.name ?: "")
            binding.etEmail.setText(userData?.email ?: "")

            if (!userData?.name.isNullOrEmpty())
                binding.etName.isFocusable = false

            if (!userData?.email.isNullOrEmpty())
                binding.etEmail.isFocusable = false

            loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

            isUpdate = true
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            when {
                arguments?.containsKey(UPDATE_PROFILE) == true -> requireActivity().finish()
                requireActivity().supportFragmentManager.backStackEntryCount > 0 ->
                    requireActivity().supportFragmentManager.popBackStack()
                else -> requireActivity().finish()
            }
        }

        binding.etDob.setOnClickListener {
            selectedDob = true
            binding.etDob.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, (System.currentTimeMillis() - 36000), null)
        }
        binding.etYears.setOnClickListener {
            selectedDob = false
            binding.etDob.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, (System.currentTimeMillis() - 36000), null)
        }

        binding.tvNext.setOnClickListener {
            checkValidation()
        }

        binding.etTitle.setOnClickListener {
            binding.spnTitle.performClick()
        }

        binding.ivPic.setOnClickListener {
            getStorageWithPermissionCheck()
        }

        binding.spnTitle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>,
                                        selectedItemView: View?, position: Int, id: Long) {
                binding.etTitle.setText(binding.spnTitle.selectedItem.toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {

            }
        }

        binding.etGender.setOnClickListener {
            binding.etGender.hideKeyboard()
            binding.spnGender.performClick()
        }

        binding.spnGender.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when {
                    openFirstGender -> openFirstGender = false
                    position == 0 -> binding.etGender.setText("")
                    else -> binding.etGender.setText(itemsGender[position].name)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })
    }

    private fun checkValidation() {
        when {
            binding.spnTitle.selectedItemPosition == 0 -> {
                binding.etTitle.showSnackBar(getString(R.string.enter_title))
            }
            binding.etName.text.toString().trim().isEmpty() -> {
                binding.etName.showSnackBar(getString(R.string.enter_name))
            }
            (!isUpdate && binding.etEmail.text.toString().trim().isEmpty()) -> {
                binding.etEmail.showSnackBar(getString(R.string.enter_email))
            }
            (binding.etEmail.text.toString().trim().isNotEmpty() &&
                    !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches()) -> {
                binding.etEmail.showSnackBar(getString(R.string.enter_correct_email))
            }
            (!isUpdate && binding.etPassword.text.toString().length < 8) -> {
                binding.etPassword.showSnackBar(getString(R.string.enter_password))
            }
            binding.etDob.text.toString().isEmpty() -> {
                binding.etDob.showSnackBar(getString(R.string.select_dob))
            }
            binding.etYears.text.toString().isEmpty() -> {
                binding.etYears.showSnackBar(getString(R.string.since_working))
            }
            binding.etQualification.text.toString().isEmpty() -> {
                binding.etQualification.showSnackBar(getString(R.string.qualifications))
            }
            binding.etGender.text.toString().isEmpty() -> {
                binding.etGender.showSnackBar(getString(R.string.select_gender))
            }
            binding.etLanguages.text.toString().trim().isEmpty() -> {
                binding.etLanguages.showSnackBar(getString(R.string.choose_language))
            }
            binding.etBio.text.toString().trim().isEmpty() -> {
                binding.etBio.showSnackBar(getString(R.string.enter_bio))
            }
            isConnectedToInternet(requireContext(), true) -> {

                val hashMap = HashMap<String, RequestBody>()
                hashMap["title"] = getRequestBody(binding.etTitle.text.toString())
                hashMap["name"] = getRequestBody(binding.etName.text.toString().trim())
                hashMap["dob"] = getRequestBody(DateUtils.dateFormatChange(DateFormat.DATE_FORMAT_SLASH,
                        DateFormat.DATE_FORMAT, binding.etDob.text.toString()))
                hashMap["working_since"] = getRequestBody(DateUtils.dateFormatChange(DateFormat.DATE_FORMAT_SLASH,
                        DateFormat.DATE_FORMAT, binding.etYears.text.toString()))
                hashMap["bio"] = getRequestBody(binding.etBio.text.toString().trim())

                if (fileToUpload != null && fileToUpload?.exists() == true) {
                    hashMap["type"] = getRequestBody("img")

                    val body: RequestBody = fileToUpload?.asRequestBody("image/*".toMediaType())!!

                    hashMap["profile_image\"; fileName=\"" + fileToUpload?.name] = body
                }


                val filterArray = ArrayList<SetFilter>()

                var setFilter: SetFilter
                prefrences?.forEach {
                    setFilter = SetFilter()
                    setFilter.preference_id = it.id

                    when (it.preference_name) {
                        PreferencesType.GENDER -> {
                            if (binding.spnGender.selectedItemPosition != 0) {
                                setFilter.option_ids = ArrayList()
                                setFilter.option_ids?.add(itemsGender[spnGender.selectedItemPosition].id
                                        ?: "")
                                filterArray.add(setFilter)
                            }
                        }
                        PreferencesType.LANGUAGES -> {
                            setFilter.option_ids = ArrayList()
                            itemsLanguage.forEach {
                                if (it.isSelected)
                                    setFilter.option_ids?.add(it.id ?: "")

                            }
                            filterArray.add(setFilter)
                        }
                    }
                }

                hashMap["master_preferences"] = getRequestBody(Gson().toJson(filterArray))


                appClientDetails.custom_fields?.service_provider?.forEach {
                    if (it.field_name == CustomFields.QUALIFICATION) {
                        val customer = ArrayList<Insurance>()
                        val item = it
                        item.field_value = binding.etQualification.text.toString()

                        customer.add(item)

                        hashMap["custom_fields"] = getRequestBody(Gson().toJson(customer))
                        return@forEach
                    }
                }

                /*Update profile or register*/
                when {
                    arguments?.containsKey(UPDATE_NUMBER) == true -> {
                        hashMap["email"] = getRequestBody(binding.etEmail.text.toString().trim())
                        viewModel.updateProfile(hashMap)
                    }
                    arguments?.containsKey(UPDATE_PROFILE) == true -> {
                        hashMap["email"] = getRequestBody(binding.etEmail.text.toString().trim())
                        viewModel.updateProfile(hashMap)
                    }
                    else -> {
                        hashMap["email"] = getRequestBody(binding.etEmail.text.toString().trim())
                        hashMap["password"] = getRequestBody(binding.etPassword.text.toString().trim())
                        hashMap["user_type"] = getRequestBody(APP_TYPE)
                        viewModel.register(hashMap)
                    }
                }
            }
        }
    }


    private fun bindObservers() {
        viewModel.register.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)
                    /*If need to move to phone number*/

                    val fragment = LoginFragment()
                    val bundle = Bundle()
                    bundle.putBoolean(UPDATE_NUMBER, true)
                    fragment.arguments = bundle

                    replaceFragment(requireActivity().supportFragmentManager,
                            fragment, R.id.container)

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModel.updateProfile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    requireActivity().setResult(Activity.RESULT_OK)

                    if (appClientDetails.insurance == true || appClientDetails.clientFeaturesKeys.isAddress == true) {
                        val fragment = InsuranceFragment()
                        val bundle = Bundle()
                        if (arguments?.containsKey(UPDATE_PROFILE) == true)
                            bundle.putBoolean(UPDATE_PROFILE, true)
                        fragment.arguments = bundle

                        replaceFragment(requireActivity().supportFragmentManager,
                                fragment, R.id.container)
                    } else if (arguments?.containsKey(UPDATE_PROFILE) == true) {
                        requireActivity().finish()
                    } else
                        replaceFragment(requireActivity().supportFragmentManager,
                                CategoryFragment(), R.id.container)

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModelVersion.preferences.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    prefrences = ArrayList()
                    prefrences?.addAll(it.data?.preferences ?: emptyList())

                    prefrences?.forEach {
                        when (it.preference_name) {
                            PreferencesType.GENDER -> {
                                itemsGender.clear()

                                var countryCity = CountryCity()
                                countryCity.name = getString(R.string.select_gender)
                                itemsGender.add(countryCity)

                                it.options?.forEach {
                                    countryCity = CountryCity()
                                    countryCity.id = it.id
                                    countryCity.name = it.option_name

                                    itemsGender.add(countryCity)
                                }

                                spinnerGenderAdapter?.notifyDataSetChanged()

                            }
                            PreferencesType.LANGUAGES -> {
                                itemsLanguage.clear()

                                var item: Insurance
                                it.options?.forEach {
                                    item = Insurance()
                                    item.id = it.id
                                    item.name = it.option_name

                                    itemsLanguage.add(item)
                                }

                                itemsLanguage.forEachIndexed { indexInsurance, insurance ->
                                    userData?.master_preferences?.forEach {
                                        if (it.preference_name == PreferencesType.LANGUAGES) {
                                            it.options?.forEachIndexed { index, filterOption ->
                                                if (filterOption.isSelected && insurance.id == filterOption.id)
                                                    itemsLanguage[indexInsurance].isSelected = true
                                            }
                                        }
                                    }
                                }
                                setLanguageNames()
                                val adapter = InsuranceAdapter(this, itemsLanguage)
                                binding.rvLanguages.adapter = adapter

                            }
                        }
                    }
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })
    }

    fun setLanguageNames() {
        var name = ""
        itemsLanguage.forEach {
            if (it.isSelected)
                name += it.name + ", "
        }
        binding.etLanguages.setText(name.removeSuffix(", "))
    }

    override fun onDateSelected(date: String) {
        if (selectedDob)
            binding.etDob.setText(date)
        else
            binding.etYears.setText(date)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == AppRequestCode.IMAGE_PICKER) {
                val docPaths = ArrayList<Uri>()
                docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                        ?: emptyList())

                fileToUpload = File(getPathUri(requireContext(), docPaths[0]))
                Glide.with(requireContext()).load(fileToUpload).into(binding.ivPic)

            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        selectImages(this,requireActivity())
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireContext(), R.string.media_permission, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
                requireContext(), R.string.media_permission
        )
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
                requireContext(), R.string.media_permission
        )
    }
}
