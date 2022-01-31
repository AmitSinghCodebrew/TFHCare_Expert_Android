package com.consultantvendor.ui.dashboard.home.prescription

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.databinding.BottomPrescriptionBinding
import com.consultantvendor.di.DaggerBottomSheetDialogFragment
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject


class BottomPrescriptionFragment(private val fragment: Fragment,private val request: Request) : DaggerBottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: BottomPrescriptionBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_prescription, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        initialise()
        listeners()
    }

    private fun initialise() {


    }

    private fun listeners() {

        binding.tvManual.setOnClickListener {
            fragment.startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, PrescriptionType.MANUAL)
                    .putExtra(EXTRA_REQUEST_ID, request),AppRequestCode.ADD_PRESCRIPTION)
            dialog?.dismiss()
        }

        binding.tvDigital.setOnClickListener {
            fragment.startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, PrescriptionType.DIGITAL)
                    .putExtra(EXTRA_REQUEST_ID, request),AppRequestCode.ADD_PRESCRIPTION)
            dialog?.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dialog?.dismiss()
        }
    }
}
