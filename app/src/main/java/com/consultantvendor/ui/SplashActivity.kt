package com.consultantvendor.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.appClientDetails
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivitySplashBinding
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.utils.*
import dagger.android.support.DaggerAppCompatActivity
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var viewModel: AppVersionViewModel

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        initialise()
        listeners()
        bindObservers()
    }


    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]

        if (isConnectedToInternet(this, true)) {
            val hashMap = HashMap<String, String>()
            hashMap["app_type"] = "2"/*APP_TYPE 1: User App, 2: Doctor App*/
            hashMap["device_type"] = "2"/*ANDROID*/
            viewModel.clientDetails(hashMap)
        }
    }

    private fun listeners() {
        binding.ivPatient.setOnClickListener {
            val appPackageName = getString(R.string.app_application_id)

            try {
                startActivity(packageManager.getLaunchIntentForPackage(appPackageName))
            } catch (e: Exception) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")))
                } catch (anfe: android.content.ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE + appPackageName)))
                }
            }
        }

        binding.ivDoc.setOnClickListener {
            prefsManager.save(USER_TYPE, true)
            goNormalSteps()
        }

    }

    private fun bindObservers() {
        viewModel.clientDetails.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {

                    /*Convert features to boolean keys*/
                    val appDetails = it.data
                    /*Handle feature keys*/
                    appDetails?.client_features?.forEach {
                        when (it.name?.toLowerCase(Locale.getDefault())) {
                            ClientFeatures.ADDRESS.toLowerCase(Locale.getDefault()) ->
                                appDetails.clientFeaturesKeys.isAddress = true
                        }
                    }

                    prefsManager.save(APP_DETAILS, appDetails)
                    appClientDetails = userRepository.getAppSetting()

                    /*Check App Version*/
                    val hashMap = HashMap<String, String>()
                    hashMap["app_type"] = "2"/*APP_TYPE 1: User App, 2: Doctor App*/
                    hashMap["device_type"] = "2"/*ANDROID*/
                    hashMap["current_version"] = getVersion(this).versionCode.toString()

                    viewModel.checkAppVersion(hashMap)

                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })

        viewModel.checkAppVersion.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.update_type) {
                        AppUpdateType.HARD_UPDATE -> hardUpdate()
                        AppUpdateType.SOFT_UPDATE -> softUpdate()
                        else -> goNormalSteps()
                    }
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })
    }

    private fun hardUpdate() {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.update))
                .setMessage(getString(R.string.update_desc, getString(R.string.app_name)))
                .setPositiveButton(getString(R.string.update)) { dialog, which ->
                    updatePlayStore()
                    hardUpdate()
                }.show()
    }

    private fun updatePlayStore() {
        val appPackageName = packageName // getPackageName() from Context or Activity object
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE + appPackageName)))
        }
    }

    private fun softUpdate() {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.update))
                .setMessage(getString(R.string.update_desc, getString(R.string.app_name)))
                .setPositiveButton(getString(R.string.update)) { dialog, which ->
                    updatePlayStore()
                    softUpdate()
                }.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                    goNormalSteps()
                }.show()
    }

    private fun goNormalSteps() {
        if (!prefsManager.getBoolean(USER_TYPE, false)) {
            binding.clUserType.visible()
        } else {
            if (userRepository.isUserLoggedIn()) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.clUserType.post {
            binding.clUserType.requestFocus()
            binding.clUserType.hideKeyboard()
        }
    }

    object AppUpdateType {
        const val HARD_UPDATE = 1
        const val SOFT_UPDATE = 2
    }

    companion object {
        const val USER_TYPE = "user type"
    }

}
