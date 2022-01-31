package com.consultantvendor.ui.dashboard.home.prescription.manual

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.AddPrescription
import com.consultantvendor.data.models.requests.DocImage
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentManualPrescriptionBinding
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.ui.dashboard.home.prescription.AddPrescriptionViewModel
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.consultantvendor.utils.dialogs.ProgressDialogImage
import dagger.android.support.DaggerFragment
import droidninja.filepicker.FilePickerConst
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject

@RuntimePermissions
class ManualPrescriptionFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentManualPrescriptionBinding

    private var rootView: View? = null

    private lateinit var viewModelUpload: UploadFileViewModel

    private lateinit var addPrescriptionViewModel: AddPrescriptionViewModel

    private lateinit var progressDialog: ProgressDialog

    private lateinit var progressDialogImage: ProgressDialogImage

    private var imagesAdapter: ImagesAdapter? = null

    private var itemImages = ArrayList<DocImage>()

    private var request: Request? = null

    private var addPrescription: AddPrescription? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_manual_prescription, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            setEditPrescriptionData()
            listeners()
            bindObservers()
            hitApi(true)
        }
        return rootView
    }


    private fun initialise() {
        viewModelUpload = ViewModelProvider(this, viewModelFactory)[UploadFileViewModel::class.java]
        addPrescriptionViewModel = ViewModelProvider(this, viewModelFactory)[AddPrescriptionViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
        progressDialogImage = ProgressDialogImage(requireActivity())

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
            binding.etRecordDetails.setText(prescription?.title)

            itemImages.clear()
            var docImage: DocImage
            prescription?.images?.forEach {
                docImage = DocImage()
                docImage.image = it
                itemImages.add(docImage)
            }

            imagesAdapter?.notifyDataSetChanged()
        }
    }

    private fun setAdapter() {
        imagesAdapter = ImagesAdapter(this, itemImages)
        binding.rvImages.adapter = imagesAdapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.tvDone.setOnClickListener {
            when {
                binding.etRecordDetails.text.toString().trim().isEmpty() -> {
                    binding.etRecordDetails.showSnackBar(getString(R.string.record_title))
                }
                itemImages.isEmpty() -> {
                    binding.etRecordDetails.showSnackBar(getString(R.string.select_image))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    addPrescription = AddPrescription()
                    addPrescription?.request_id = request?.id
                    addPrescription?.type = PrescriptionType.MANUAL
                    addPrescription?.title = binding.etRecordDetails.text.toString().trim()

                    addPrescription?.image = ArrayList()

                    itemImages.forEach {
                        if (it.imageFile != null) {
                            uploadFileOnServer(it)
                            return@forEach
                        } else if (!it.image.isNullOrEmpty()) {
                            addPrescription?.image?.add(it.image ?: "")
                        }
                    }

                    if (addPrescription?.image?.size ?: 0 == itemImages.size)
                        addPrescriptionViewModel.prescreptions(addPrescription
                                ?: AddPrescription())
                    //uploadFileOnServer(itemImages[0])
                }
            }
        }
    }

    private fun hitApi(firstHit: Boolean) {

    }

    private fun uploadFileOnServer(docImage: DocImage?) {
        val hashMap = java.util.HashMap<String, RequestBody>()
        hashMap["type"] = getRequestBody(docImage?.type)

        val body: RequestBody = docImage?.imageFile?.asRequestBody("image/*".toMediaType())!!
        hashMap["image\"; fileName=\"" + docImage?.imageFile?.name] = body

        viewModelUpload.uploadFile(hashMap)
    }

    private fun bindObservers() {
        viewModelUpload.uploadFile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialogImage.setLoading(false)

                    addPrescription?.image?.add(it.data?.image_name ?: "")

                    if (addPrescription?.image?.size ?: 0 < itemImages.size) {
                        for (i in (addPrescription?.image?.size ?: 0)..itemImages.size) {
                            val docImage = itemImages[addPrescription?.image?.size ?: 0]

                            if (docImage.imageFile != null) {
                                uploadFileOnServer(docImage)
                                break
                            } else if (!docImage.image.isNullOrEmpty()) {
                                addPrescription?.image?.add(docImage.image ?: "")
                            }
                            //uploadFileOnServer(itemImages[addPrescription?.image?.size ?: 0])
                        }

                        if (addPrescription?.image?.size ?: 0 == itemImages.size)
                            addPrescriptionViewModel.prescreptions(addPrescription
                                    ?: AddPrescription())
                    } else {
                        addPrescriptionViewModel.prescreptions(addPrescription ?: AddPrescription())
                    }
                }
                Status.ERROR -> {
                    progressDialogImage.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialogImage.setLoading(true)

                }
            }
        })

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

    /*Adapter item click*/
    fun clickItem() {
        getStorageWithPermissionCheck()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppRequestCode.IMAGE_PICKER -> {
                    val docPaths = ArrayList<Uri>()
                    docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                            ?: emptyList())

                    val fileToUpload = File(getPathUri(requireContext(), docPaths[0]))

                    val docImage = DocImage()
                    docImage.type = DocType.IMAGE
                    docImage.imageFile = compressImage(requireActivity(), fileToUpload)

                    itemImages.add(docImage)
                    imagesAdapter?.notifyDataSetChanged()
                }
            }

            /* if (requestCode == AppRequestCode.IMAGE_PICKER) {
                 val docPaths = ArrayList<Uri>()
                 docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                         ?: emptyList())

                 val fileToUpload = File(getPathUri(requireContext(), docPaths[0]))

                 itemImages.add(fileToUpload)
                 imagesAdapter?.notifyDataSetChanged()
             }*/
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        selectImages(this, requireActivity())
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