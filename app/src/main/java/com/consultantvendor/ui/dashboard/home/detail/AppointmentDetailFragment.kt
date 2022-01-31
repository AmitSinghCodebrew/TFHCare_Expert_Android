package com.consultantvendor.ui.dashboard.home.detail

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.DocImage
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PushType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentAppointmentDetailsBinding
import com.consultantvendor.ui.adapter.CheckItemAdapter
import com.consultantvendor.ui.calling.CallingActivity
import com.consultantvendor.ui.chat.chatdetail.ChatDetailActivity
import com.consultantvendor.ui.dashboard.home.AppointmentViewModel
import com.consultantvendor.ui.dashboard.home.prescription.BottomPrescriptionFragment
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class AppointmentDetailFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentAppointmentDetailsBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var request: Request

    private var isReceiverRegistered = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_appointment_details, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            hitApi()
        }
        return rootView
    }


    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        binding.clLoader.setBackgroundResource(R.color.colorWhite)

    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["request_id"] = requireActivity().intent.getStringExtra(EXTRA_REQUEST_ID) ?: ""
            viewModel.requestDetail(hashMap)
        }
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvAccept.setOnClickListener {
            proceedRequest()
        }

        binding.tvAddPrescription.setOnClickListener {
            proceedRequest()
        }

        binding.tvCancel.setOnClickListener {
            cancelAppointment()
        }

        binding.tvMarkComplete.setOnClickListener {
            showMarkCompleteDialog()
        }

        binding.tvViewMap.setOnClickListener {
            val address = request.extra_detail
            mapIntent(requireActivity(), address?.service_address ?: "",
                    address?.lat?.toDouble() ?: 0.0,
                    address?.long?.toDouble() ?: 0.0)
        }
    }

    private fun bindObservers() {
        viewModel.requestDetail.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.setBackgroundResource(0)
                    binding.clLoader.gone()

                    request = it.data?.request_detail ?: Request()
                    setData()

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })

        viewModel.acceptRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()
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

        viewModel.startRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()

                    when (request.main_service_type) {
                        ConsultType.CHAT -> {
                            requireActivity().longToast(getString(R.string.starting_chat))

                            startActivity(Intent(requireActivity(), ChatDetailActivity::class.java)
                                    .putExtra(USER_ID, request.from_user?.id)
                                    .putExtra(USER_NAME, request.from_user?.name)
                                    .putExtra(EXTRA_REQUEST_ID, request.id)
                                    .putExtra(EXTRA_IS_FIRST, true)
                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                        }
                        ConsultType.AUDIO_CALL, ConsultType.VIDEO_CALL -> {
                            requireActivity().longToast(getString(R.string.starting_call))

                            startActivity(Intent(requireContext(), CallingActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra(EXTRA_REQUEST_ID, request))
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

        viewModel.callStatus.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()

                    if (request.status != CallAction.START_SERVICE) {
                        request.status = CallAction.START
                        /* startActivityForResult(Intent(requireActivity(), AppointmentStatusActivity::class.java)
                                 .putExtra(EXTRA_REQUEST_ID, request), AppRequestCode.APPOINTMENT_DETAILS)*/
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

        viewModel.cancelRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()
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

    private fun setData() {
        binding.tvAccept.visible()
        binding.tvCancel.hideShowView(request.canCancel)
        binding.tvMarkComplete.gone()
        binding.tvAddPrescription.gone()

        binding.tvName.text = request.from_user?.name
        binding.tvBookingPriceV.text = getCurrency(request.price)
        binding.tvServiceTypeV.text = request.service_type
        loadImage(binding.ivPic, request.from_user?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvBookingDateV.text = DateUtils.dateTimeFormatFromUTC(DateFormat.MON_YEAR_FORMAT, request.bookingDateUTC)
        binding.tvBookingTimeV.text = DateUtils.dateTimeFormatFromUTC(DateFormat.TIME_FORMAT, request.bookingDateUTC)

        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        if (request.is_prescription == true)
            binding.tvAddPrescription.text = getString(R.string.prescriptions)
        else
            binding.tvAddPrescription.text = getString(R.string.add_prescription)

        when (request.main_service_type) {
            ConsultType.HOME_VISIT -> {
                if (request.extra_detail?.service_address != null) {
                    binding.tvLocation.visible()
                    binding.tvViewMap.visible()
                    binding.tvLocation.text = request.extra_detail?.service_address
                }
            }
        }

        when (request.status) {
            CallAction.PENDING -> {
                binding.tvStatus.text = getString(R.string.new_request)
                binding.tvAccept.text = getString(R.string.accept_request)
            }
            CallAction.ACCEPT -> {
                binding.tvStatus.text = getString(R.string.accepted)
                binding.tvAccept.text = getString(R.string.start_request)
                binding.tvCancel.gone()

                when (request.main_service_type) {
                    ConsultType.AUDIO_CALL, ConsultType.VIDEO_CALL ->
                        binding.tvMarkComplete.visible()
                }
            }
            CallAction.INPROGRESS -> {
                binding.tvStatus.text = getString(R.string.inprogess)
                binding.tvCancel.gone()
                binding.tvAccept.gone()

                binding.tvMarkComplete.visible()
            }
            CallAction.START -> {
                binding.tvStatus.text = getString(R.string.inprogess)
                binding.tvAccept.text = getString(R.string.track_status)
                binding.tvCancel.gone()
                binding.tvMarkComplete.visible()
            }
            CallAction.REACHED -> {
                binding.tvStatus.text = getString(R.string.reached_destination)
                binding.tvAccept.text = getString(R.string.track_status)
                binding.tvCancel.gone()
            }
            CallAction.START_SERVICE -> {
                binding.tvStatus.text = getString(R.string.started)
                binding.tvAccept.gone()
                binding.tvCancel.gone()
                binding.tvMarkComplete.visible()
            }
            CallAction.COMPLETED -> {
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorGreen))
                binding.tvStatus.text = getString(R.string.completed)
                binding.tvAccept.gone()
                binding.tvCancel.gone()
                binding.tvAddPrescription.visible()
            }
            CallAction.FAILED -> {
                binding.tvAccept.gone()
                binding.tvStatus.text = getString(R.string.no_show)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorCancel))
                binding.tvCancel.gone()
            }
            CallAction.CANCELED -> {
                binding.tvStatus.text = getString(R.string.canceled)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorCancel))
                binding.tvAccept.gone()
                binding.tvCancel.gone()
            }
            CallAction.CANCEL_SERVICE -> {
                binding.tvStatus.text = getString(R.string.canceled_service)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorCancel))
                binding.tvCancel.gone()
                binding.tvAccept.gone()
            }
            else -> {
                binding.tvStatus.text = getString(R.string.new_request)
            }
        }


        /*Second Opinion*/
        binding.tvMedicalRecord.text = request.second_oponion?.title ?: ""
        binding.tvMedicalRecord.hideShowView(binding.tvMedicalRecord.text.isNotEmpty())

        val array = request.second_oponion?.images?.split(",") ?: emptyList()
        val adapter = ImagesUploadedAdapter(this, ArrayList(array))
        binding.rvMedicalRecord.adapter = adapter

        binding.tvMedicalRecordT.hideShowView(binding.tvMedicalRecord.text.isNotEmpty() && ArrayList(array).isNotEmpty())

        /*Symptom*/
        binding.tvSymptomDec.text = request.symptom_details
        binding.tvSymptomDec.hideShowView(binding.tvSymptomDec.text.isNotEmpty())

        val symptomImages = ArrayList<DocImage>()
        symptomImages.addAll(request.symptom_images ?: emptyList())
        val adapterSymptomImage = ImagesDocumentAdapter(this, symptomImages)
        binding.rvSymptomDoc.adapter = adapterSymptomImage
        binding.rvSymptomDoc.hideShowView(symptomImages.isNotEmpty())

        binding.rvSymptomListing.layoutManager = GridLayoutManager(requireContext(), 3)
        val items = ArrayList<Filter>()
        items.addAll(request.symptoms ?: emptyList())
        val adapterSymptom = CheckItemAdapter(this, true, items)
        binding.rvSymptomListing.adapter = adapterSymptom

        binding.tvSymptom.hideShowView(binding.tvSymptomDec.text.isNotEmpty() || items.isNotEmpty())
    }


    private fun proceedRequest() {
        when (request.status) {
            CallAction.PENDING -> {
                showAcceptRequestDialog()
            }
            CallAction.ACCEPT -> {
                showInitiateRequestDialog()
            }
            CallAction.COMPLETED -> {
                if (request.is_prescription == true) {
                    if (!request.pre_scription?.type.isNullOrEmpty()) {
                        val popup = PopupMenu(requireContext(), binding.tvAddPrescription)
                        popup.menuInflater.inflate(R.menu.menu_prescription, popup.menu)

                        popup.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.item_view -> {
                                    val link = getString(R.string.pdf_link, request.id, APP_UNIQUE_ID)
                                    openPdf(requireActivity(), link,true)
                                }
                                R.id.item_edit -> {
                                    startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                                            .putExtra(PAGE_TO_OPEN, request.pre_scription?.type)
                                            .putExtra(EXTRA_REQUEST_ID, request), AppRequestCode.ADD_PRESCRIPTION)
                                }
                            }
                            true
                        }

                        popup.show()
                    }
                } else {
                    val fragment = BottomPrescriptionFragment(this, request)
                    fragment.show(requireActivity().supportFragmentManager, fragment.tag)
                }
            }
            CallAction.START, CallAction.REACHED -> {
                /*startActivityForResult(Intent(requireActivity(), AppointmentStatusActivity::class.java)
                        .putExtra(EXTRA_REQUEST_ID, request), AppRequestCode.APPOINTMENT_DETAILS)*/
            }
            CallAction.START_SERVICE -> {
                showMarkCompleteDialog()
            }
        }
    }

    private fun showAcceptRequestDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.accept_request,
                R.string.accept_request_message, R.string.accept_request, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        hitApiAcceptRequest()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun showMarkCompleteDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.mark_complete,
                R.string.mark_complete_message, R.string.mark_complete, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        hitApiCompleteRequest()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun showInitiateRequestDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.start_request,
                R.string.start_request_message, R.string.start_request, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        hitApiStartRequest()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun hitApiAcceptRequest() {
        if (isConnectedToInternet(requireActivity(), true)) {
            val hashMap = HashMap<String, Any>()
            hashMap["request_id"] = request.id ?: ""

            viewModel.acceptRequest(hashMap)
        }
    }


    private fun hitApiCompleteRequest() {
        if (isConnectedToInternet(requireActivity(), true)) {
            val hashMap = java.util.HashMap<String, Any>()
            hashMap["request_id"] = request.id ?: ""
            hashMap["status"] = CallAction.COMPLETED

            viewModel.callStatus(hashMap)
        }
    }

    private fun hitApiStartRequest() {
        if (isConnectedToInternet(requireActivity(), true)) {
            when (request.main_service_type) {
                ConsultType.HOME_VISIT -> {
                    val hashMap = HashMap<String, Any>()
                    hashMap["request_id"] = request.id ?: ""
                    hashMap["status"] = CallAction.START

                    viewModel.callStatus(hashMap)
                }
                else -> {
                    val hashMap = HashMap<String, Any>()
                    hashMap["request_id"] = request.id ?: ""

                    viewModel.startRequest(hashMap)
                }
            }
        }
    }

    private fun cancelAppointment() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(),
                R.string.cancel_appointment,
                R.string.cancel_appointment_msg,
                R.string.cancel_appointment,
                R.string.cancel,
                false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        if (isConnectedToInternet(requireContext(), true)) {
                            val hashMap = HashMap<String, String>()
                            hashMap["request_id"] = request.id ?: ""
                            viewModel.cancelRequest(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppRequestCode.ADD_PRESCRIPTION, AppRequestCode.APPOINTMENT_DETAILS -> {
                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.REQUEST_COMPLETED)
            intentFilter.addAction(PushType.CANCELED_REQUEST)
            intentFilter.addAction(PushType.PATIENT_ADDED_SYMPTOMS)
            intentFilter.addAction(PushType.REQUEST_FAILED)
            intentFilter.addAction(PushType.COMPLETED)
            LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(refreshData, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshData)
            isReceiverRegistered = false
        }
    }

    private val refreshData = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                PushType.REQUEST_COMPLETED, PushType.COMPLETED,PushType.PATIENT_ADDED_SYMPTOMS,
                PushType.CANCELED_REQUEST, PushType.REQUEST_FAILED -> {
                    if (request.id == intent.getStringExtra(EXTRA_REQUEST_ID))
                        hitApi()
                }
            }
        }
    }

}



