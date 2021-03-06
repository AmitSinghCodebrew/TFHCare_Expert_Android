package com.consultantvendor.ui.loginSignUp.insurance

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.appClientDetails
import com.consultantvendor.data.models.responses.CountryCity
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.models.responses.appdetails.AppVersion
import com.consultantvendor.data.models.responses.appdetails.Insurance
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentInsauranceBinding
import com.consultantvendor.ui.AppVersionViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.category.CategoryFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import okhttp3.RequestBody
import javax.inject.Inject


class InsuranceFragment : DaggerFragment() {

    @Inject
    lateinit var appSocket: AppSocket

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentInsauranceBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelVersion: AppVersionViewModel

    private val items = ArrayList<Insurance>()

    private var userData: UserData? = null

    private var spinnerStateAdapter: CustomSpinnerAdapter? = null

    private var spinnerCityAdapter: CustomSpinnerAdapter? = null

    private val itemsState = ArrayList<CountryCity>()

    private val itemsCity = ArrayList<CountryCity>()

    private var openFirstState = true

    private var openFirstCity = true


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_insaurance, container, false)
            rootView = binding.root

            initialise()
            setUpdateInsurance()
            listeners()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        viewModelVersion =
                ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun setAdapters() {
        spinnerStateAdapter = CustomSpinnerAdapter(requireContext(), itemsState)
        binding.spnState.adapter = spinnerStateAdapter

        spinnerCityAdapter = CustomSpinnerAdapter(requireContext(), itemsCity)
        binding.spnCity.adapter = spinnerCityAdapter

        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["type"] = CountryListType.STATE
            hashMap["country_id"] = appClientDetails.country_id ?: ""

            viewModelVersion.countryCity(hashMap)

            if (!userData?.profile?.state_id.isNullOrEmpty()) {
                val hashMap = HashMap<String, String>()
                hashMap["type"] = CountryListType.CITY
                hashMap["country_id"] = appClientDetails.country_id ?: ""
                hashMap["state_id"] = userData?.profile?.state_id ?: ""

                viewModelVersion.countryCity(hashMap)
            }
        }
    }

    /*Get and update address insurance*/
    private fun setUpdateInsurance() {
        userData = userRepository.getUser()

        if (appClientDetails.clientFeaturesKeys.isAddress == true) {
            setAdapters()
            binding.groupAddress.visible()

            binding.etAddress.setText(userData?.profile?.address ?: "")
            binding.etState.setText(userData?.profile?.state ?: "")
            binding.etCity.setText(userData?.profile?.city ?: "")

            userData?.custom_fields?.forEach {
                if (it.field_name == CustomFields.ZIP_CODE) {
                    binding.etZipCode.setText(it.field_value ?: "")
                    return@forEach
                }
            }

        } else {
            binding.groupAddress.gone()
        }

        items.addAll(appClientDetails.insurances ?: emptyList())

        if (appClientDetails.insurance == true) {
            binding.groupInsurance.visible()

            /*Check Selected Insurance*/
            if (userData?.insurance_enable == "1") {
                binding.cbYes.isChecked = true

                items.forEachIndexed { index, item ->
                    userData?.insurances?.forEachIndexed { _, insurance ->
                        if (item.id == insurance.id) {
                            items[index].isSelected = true
                            return@forEachIndexed
                        }
                    }
                }
            } else if (userData?.insurance_enable != null) {
                binding.cbNo.isChecked = true
                binding.groupInsurance.gone()
            }
        } else {
            binding.tvHaveInsurance.gone()
            binding.cbYes.gone()
            binding.cbNo.gone()
            binding.groupInsurance.gone()
        }

        val adapter = InsuranceAdapter(this, items)
        binding.rvInsurance.adapter = adapter
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnSubmit.setOnClickListener {
            binding.btnSubmit.hideKeyboard()

            checkValidations()
        }

        binding.etState.setOnClickListener {
            binding.btnSubmit.hideKeyboard()
            binding.spnState.performClick()
        }

        binding.spnState.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                if (openFirstState)
                    openFirstState = false
                else {
                    if (position == 0) {
                        binding.etState.setText("")
                    } else {
                        binding.etState.setText(itemsState.get(position).name)
                        if (isConnectedToInternet(requireContext(), true)) {
                            val hashMap = HashMap<String, String>()
                            hashMap["type"] = CountryListType.CITY
                            hashMap["country_id"] = appClientDetails.country_id ?: ""
                            hashMap["state_id"] = itemsState.get(position).id ?: ""

                            viewModelVersion.countryCity(hashMap)
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        binding.etCity.setOnClickListener {
            binding.btnSubmit.hideKeyboard()
            binding.spnCity.performClick()
        }

        binding.spnCity.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                if (openFirstCity)
                    openFirstCity = false
                else {
                    if (position == 0)
                        binding.etCity.setText("")
                    else
                        binding.etCity.setText(itemsCity.get(position).name)
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        binding.cbYes.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                binding.cbNo.isChecked = false
                binding.groupInsurance.visible()
            } else {
                binding.groupInsurance.gone()
            }
        }

        binding.cbNo.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                binding.cbYes.isChecked = false
                binding.groupInsurance.gone()
            }
        }
    }

    private fun checkValidations() {
        /*Get if insurance selected*/
        var idsInsurance = ""
        items.forEach {
            if (it.isSelected)
                idsInsurance += it.id + ","
        }

        val appSetting = prefsManager.getObject(APP_DETAILS, AppVersion::class.java)
        when {
            appSetting?.clientFeaturesKeys?.isAddress == true && binding.etAddress.text.toString().isEmpty() -> {
                binding.etAddress.showSnackBar(getString(R.string.address))
            }
            appSetting?.clientFeaturesKeys?.isAddress == true && binding.etState.text.toString().isEmpty() -> {
                binding.etState.showSnackBar(getString(R.string.select_state))
            }
            appSetting?.clientFeaturesKeys?.isAddress == true && binding.etCity.text.toString().isEmpty() -> {
                binding.etCity.showSnackBar(getString(R.string.select_city))
            }
            appSetting?.clientFeaturesKeys?.isAddress == true && binding.etZipCode.text.toString().isEmpty() -> {
                binding.etZipCode.showSnackBar(getString(R.string.zip))
            }
            appSetting?.insurance == true && (!binding.cbYes.isChecked && !binding.cbNo.isChecked) -> {
                binding.etCity.showSnackBar(getString(R.string.do_you_have_insurance))
            }
            appSetting?.insurance == true && (binding.cbYes.isChecked && idsInsurance.isEmpty()) -> {
                binding.etCity.showSnackBar(getString(R.string.select_insurance))
            }
            appSetting?.insurance == true && (!binding.cbTerm1.isChecked || !binding.cbTerm3.isChecked
                    || !binding.cbTerm3.isChecked) -> {
                binding.etCity.showSnackBar(getString(R.string.check_all_terms))
                binding.nsvInsurance.fullScroll(View.FOCUS_DOWN)
            }
            else -> {

                val stateId = if (binding.spnState.selectedItemPosition == -1 ||
                        binding.spnState.selectedItemPosition == 0) ""
                else itemsState[binding.spnState.selectedItemPosition].id

                val cityId = if (binding.spnCity.selectedItemPosition == -1 ||
                        binding.spnCity.selectedItemPosition == 0
                ) ""
                else itemsCity[binding.spnCity.selectedItemPosition].id

                val hashMap = HashMap<String, RequestBody>()
                hashMap["name"] = getRequestBody(userRepository.getUser()?.name)
                hashMap["address"] = getRequestBody(binding.etAddress.text.toString())
                hashMap["state"] = getRequestBody( stateId)
                hashMap["city"] = getRequestBody( cityId)
                hashMap["insurance_enable"] = getRequestBody(if (binding.cbYes.isChecked) "1" else "0")

                /*Get selected insurance*/
                if (binding.cbYes.isChecked) {
                    hashMap["insurances"] = getRequestBody(idsInsurance.removeSuffix(","))
                }

                /*Check if zip id is there in custom fields*/

                appClientDetails.custom_fields?.service_provider?.forEach {
                    if (it.field_name == CustomFields.ZIP_CODE) {
                        val customer = ArrayList<Insurance>()
                        val item = it
                        item.field_value = binding.etZipCode.text.toString()

                        customer.add(item)

                        hashMap["custom_fields"] = getRequestBody(Gson().toJson(customer))
                        return@forEach
                    }
                }

                viewModel.updateProfile(hashMap)
            }
        }
    }


    private fun bindObservers() {
        viewModel.updateProfile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    requireActivity().setResult(Activity.RESULT_OK)

                    if (arguments?.containsKey(UPDATE_PROFILE) == true) {
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

        viewModelVersion.countryCity.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    when (it.data?.type) {
                        CountryListType.STATE -> {
                            itemsState.clear()

                            val countryCity = CountryCity()
                            countryCity.name = getString(R.string.select_state)
                            itemsState.add(countryCity)

                            itemsState.addAll(it.data.state ?: emptyList())
                            spinnerStateAdapter?.notifyDataSetChanged()

                        }
                        CountryListType.CITY -> {
                            itemsCity.clear()

                            val countryCity = CountryCity()
                            countryCity.name = getString(R.string.select_city)
                            itemsCity.add(countryCity)

                            itemsCity.addAll(it.data.city ?: emptyList())
                            spinnerCityAdapter?.notifyDataSetChanged()
                        }
                    }
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
    }
}

object CountryListType {
    const val COUNTRY = "country"
    const val STATE = "state"
    const val CITY = "city"
}
