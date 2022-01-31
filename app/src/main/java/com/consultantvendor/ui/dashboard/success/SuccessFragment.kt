package com.consultantvendor.ui.dashboard.success

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.databinding.FragmentSuccessBinding
import com.consultantvendor.ui.loginSignUp.changepassword.ChangePasswordFragment
import dagger.android.support.DaggerDialogFragment
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


class SuccessFragment(private val fragment: Fragment) : DaggerDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSuccessBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_success, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        setTextForMessage()
        initialise()
    }

    private fun setTextForMessage() {
        when (fragment) {
            is ChangePasswordFragment -> {
                binding.tvTitle.text = getString(R.string.password_changed_successfully)
            }
            else -> {
                binding.tvTitle.text = getString(R.string.success)
            }
        }
    }

    private fun initialise() {
        binding.animationView.speed = 1.5f

        requireActivity().setResult(Activity.RESULT_OK)

        Timer().schedule(3000) {
            when (fragment) {
                is ChangePasswordFragment -> {
                    requireActivity().finish()
                }
            }
            dialog?.dismiss()
        }
    }
}
