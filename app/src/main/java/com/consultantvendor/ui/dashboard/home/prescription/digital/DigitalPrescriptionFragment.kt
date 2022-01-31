package com.consultantvendor.ui.dashboard.home.prescription.digital

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
import com.consultantvendor.data.models.requests.AddPrescription
import com.consultantvendor.data.models.requests.DigitalPrescription
import com.consultantvendor.data.models.requests.Doases
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentDigitalPrescriptionBinding
import com.consultantvendor.ui.dashboard.home.prescription.AddPrescriptionViewModel
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class DigitalPrescriptionFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: FragmentDigitalPrescriptionBinding

    private var rootView: View? = null

    private lateinit var addPrescriptionViewModel: AddPrescriptionViewModel

    private lateinit var progressDialog: ProgressDialog

    private var doseadAdapter: ItemDoasesAdapter? = null

    private var itemDoases = ArrayList<Doases>()

    private var itemPrescription = ArrayList<DigitalPrescription>()

    private var prescriptionAdapter: ItemPrescriptionAdapter? = null

    private var request: Request? = null

    private var addPrescription: AddPrescription? = null

    private var editPosition = -1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_digital_prescription, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            setEditPrescriptionData()
            listeners()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        addPrescriptionViewModel = ViewModelProvider(this, viewModelFactory)[AddPrescriptionViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        editTextScroll(binding.etPrescriptionNotes)
        request = requireActivity().intent.getSerializableExtra(EXTRA_REQUEST_ID) as Request

        binding.tvName.text = request?.from_user?.name
        binding.tvAge.text = "${getAge(request?.from_user?.profile?.dob)} ${getString(R.string.years_old)}"
        loadImage(binding.ivPic, request?.from_user?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvAppointmentV.text = "${DateUtils.dateTimeFormatFromUTC(DateFormat.MON_YEAR_FORMAT, request?.bookingDateUTC)} · " +
                "${DateUtils.dateTimeFormatFromUTC(DateFormat.TIME_FORMAT, request?.bookingDateUTC)}"

    }

    private fun setEditPrescriptionData() {
        if (request?.pre_scription != null) {
            val prescription = request?.pre_scription
            binding.etPrescriptionNotes.setText(prescription?.pre_scription_notes)

            itemPrescription.clear()
            itemPrescription.addAll(prescription?.medicines ?: emptyList())
            prescriptionAdapter?.notifyDataSetChanged()

            if (itemPrescription.isNotEmpty()) {
                binding.tvPrescriptions.visible()
                binding.rvPrescriptions.visible()
            }
        }
    }

    private fun setAdapter() {

        itemDoases = ArrayList()
        var doase = Doases()
        doase.time = getString(R.string.breakfast)
        doase.checked = true
        itemDoases.add(doase)

        doase = Doases()
        doase.time = getString(R.string.lunch)
        itemDoases.add(doase)

        doase = Doases()
        doase.time = getString(R.string.dinner)
        itemDoases.add(doase)

        doseadAdapter = ItemDoasesAdapter(this, itemDoases)
        binding.rvDoasesTiming.adapter = doseadAdapter

        prescriptionAdapter = ItemPrescriptionAdapter(this, itemPrescription)
        binding.rvPrescriptions.adapter = prescriptionAdapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.spnDosagesType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>,
                                        selectedItemView: View?, position: Int, id: Long) {
                doseadAdapter?.notifyDataSetChanged()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {

            }
        }

        binding.tvAdd.setOnClickListener {
            binding.tvAdd.hideKeyboard()
            when {
                binding.etMedicineName.text.toString().trim().isEmpty() -> {
                    binding.etMedicineName.showSnackBar(getString(R.string.medicine_name))
                }
                binding.spnDuration.selectedItemPosition == 0 -> {
                    binding.tvDuration.showSnackBar(getString(R.string.duration))
                }
                binding.spnDosagesType.selectedItemPosition == 0 -> {
                    binding.tvDosagesType.showSnackBar(getString(R.string.dosage_type))
                }
                else -> {
                    var addItem = false
                    itemDoases.forEachIndexed { index, doases ->
                        if (doases.checked == true) {
                            addItem = true
                            if (doases.dose_value.isNullOrEmpty()) {
                                addItem = false
                                binding.etMedicineName.showSnackBar(getString(R.string.select_doasages_for, doases.time))
                                return@setOnClickListener
                            }
                        }
                    }

                    if (addItem) {
                        val prescription = DigitalPrescription()
                        prescription.medicine_name = binding.etMedicineName.text.toString().trim()
                        prescription.duration = binding.spnDuration.selectedItem.toString()
                        prescription.dosage_type = binding.spnDosagesType.selectedItem.toString()
                        prescription.dosage_timing = ArrayList()

                        itemDoases.forEach {
                            val digitalDose: Doases
                            if (it.checked == true) {
                                digitalDose = Doases()
                                digitalDose.time = it.time
                                digitalDose.with = it.with
                                digitalDose.dose_value = it.dose_value
                                digitalDose.checked = null

                                prescription.dosage_timing?.add(digitalDose)
                            }
                        }

                        /*If edit item*/
                        if (binding.tvAdd.text == getString(R.string.edit))
                            itemPrescription.set(editPosition, prescription)
                        else
                            itemPrescription.add(prescription)
                        prescriptionAdapter?.notifyDataSetChanged()

                        editPosition = -1
                        requireActivity().longToast(getString(R.string.prescription_added))

                        if (itemPrescription.size == 1) {
                            binding.tvPrescriptions.visible()
                            binding.rvPrescriptions.visible()
                        }

                        /*Clear item*/
                        binding.tvReset.performClick()

                    } else {
                        binding.etMedicineName.showSnackBar(getString(R.string.select_dosage_timings))
                    }
                }
            }
        }

        binding.tvReset.setOnClickListener {
            binding.tvAdd.hideKeyboard()
            editPosition = -1

            binding.tvAdd.text = getString(R.string.add)
            binding.etMedicineName.setText("")
            binding.spnDuration.setSelection(0)
            binding.spnDosagesType.setSelection(0)

            itemDoases.forEachIndexed { index, doases ->
                itemDoases[index].checked = index == 0
                itemDoases[index].with = getString(R.string.before)
                itemDoases[index].dose_value = ""
            }
            doseadAdapter?.notifyDataSetChanged()
        }

        binding.tvDone.setOnClickListener {
            binding.tvAdd.hideKeyboard()
            when {
                itemPrescription.isEmpty() -> {
                    binding.etMedicineName.showSnackBar(getString(R.string.add_digital_prescription))
                }
                binding.etPrescriptionNotes.text.toString().trim().isEmpty() -> {
                    binding.tvDosagesType.showSnackBar(getString(R.string.add_notes))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    addPrescription = AddPrescription()
                    addPrescription?.request_id = request?.id
                    addPrescription?.type = PrescriptionType.DIGITAL

                    addPrescription?.pre_scription_notes = binding.etPrescriptionNotes.text.toString().trim()
                    addPrescription?.pre_scriptions = ArrayList()
                    addPrescription?.pre_scriptions?.addAll(itemPrescription)

                    addPrescriptionViewModel.prescreptions(addPrescription ?: AddPrescription())
                }
            }
        }
    }

    fun deletePrescription(pos: Int) {
        itemPrescription.removeAt(pos)
        prescriptionAdapter?.notifyDataSetChanged()

        if (editPosition == pos) {
            binding.tvReset.performClick()
        }

        if (itemPrescription.isEmpty())
            binding.tvPrescriptions.gone()
    }

    fun editPrescription(pos: Int) {
        binding.tvAdd.hideKeyboard()

        editPosition = pos

        binding.tvAdd.text = getString(R.string.edit)

        val item = itemPrescription[editPosition]
        binding.etMedicineName.setText(item.medicine_name)

        val duration = resources.getStringArray(R.array.duration)
        binding.spnDuration.setSelection(duration.indexOf(item.duration))

        val dose_type = resources.getStringArray(R.array.dose_type)
        binding.spnDosagesType.setSelection(dose_type.indexOf(item.dosage_type))

        item.dosage_timing?.forEachIndexed { index, doases ->
            itemDoases.forEachIndexed { indexInternal, doasesInternal ->
                if (doases.time == doasesInternal.time) {
                    itemDoases[indexInternal].checked = true
                    itemDoases[indexInternal].with = doases.with
                    itemDoases[indexInternal].dose_value = doases.dose_value

                    return@forEachIndexed
                }
            }
        }
        doseadAdapter?.notifyDataSetChanged()

    }

    private fun bindObservers() {
        addPrescriptionViewModel.prescreptions.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()

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