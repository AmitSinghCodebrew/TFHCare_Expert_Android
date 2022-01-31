package com.consultantvendor.ui.dashboard.settings

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
import com.consultantvendor.appClientDetails
import com.consultantvendor.data.models.responses.Page
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.Config
import com.consultantvendor.data.network.ProviderType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentSettingsBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.webview.WebViewActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SettingsFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSettingsBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private var items = ArrayList<Page>()

    private lateinit var adapter: PagesAdapter


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            setUserProfile()

        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun setAdapter() {
        items.clear()

        items.add(Page(title = getString(R.string.account_setting), icon = R.drawable.ic_profile_setting))
        if (userRepository.getUser()?.provider_type == ProviderType.email)
            items.add(Page(title = getString(R.string.change_password), icon = R.drawable.ic_password))

        items.add(Page(title = getString(R.string.chat), icon = R.drawable.ic_chat_profile))
        //items.add(Page(title=getString(R.string.history),icon = R.drawable.ic_history_drawer))
        items.add(Page(title = getString(R.string.second_opinion), icon = R.drawable.ic_class_profile))
        items.add(Page(title = getString(R.string.notification), icon = R.drawable.ic_notification_drawer))
        items.add(Page(title = getString(R.string.invite_people), icon = R.drawable.ic_invite))

        appClientDetails.pages?.forEach {
            items.add(Page(title = it.title, slug = it.slug, icon = R.drawable.ic_info))
        }

        if (!appClientDetails.support_url.isNullOrEmpty())
            items.add(Page(title = getString(R.string.support), icon = R.drawable.ic_info))
        items.add(Page(title = getString(R.string.logout), icon = R.drawable.ic_logout))

        adapter = PagesAdapter(this, items)
        binding.rvPages.adapter = adapter

    }

    private fun setUserProfile() {
        val userData = userRepository.getUser()

        binding.tvName.text = getDoctorName(userData)
        binding.tvAge.text =
                "${getString(R.string.age)} ${getAge(userData?.profile?.dob)}"
        loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

        binding.tvVersion.text =
                getString(R.string.version, getVersion(requireActivity()).versionName)
    }

    private fun listeners() {
        binding.ivPic.setOnClickListener {
            val itemImages = java.util.ArrayList<String>()
            itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${userRepository.getUser()?.profile_image}")
            viewImageFull(context as Activity, itemImages, 0)
        }

        binding.tvName.setOnClickListener {
            goToProfile()
        }
    }

    fun itemClicked(pos: Int) {
        when (items[pos].title) {
            getString(R.string.account_setting) -> {
                goToProfile()
            }
            getString(R.string.change_password) -> {
                openScreen(DrawerActivity.CHANGE_PASSWORD)
            }
            getString(R.string.chat) -> {
                openScreen(DrawerActivity.USER_CHAT)
            }
            getString(R.string.second_opinion) -> {
                openScreen(DrawerActivity.SECOND_OPINION)
            }
            getString(R.string.history) -> {
                openScreen(DrawerActivity.HISTORY)
            }
            getString(R.string.notification) -> {
                openScreen(DrawerActivity.NOTIFICATION)
            }
            getString(R.string.invite_people) -> {
                shareDeepLink(DeepLink.INVITE, requireActivity())
            }
            getString(R.string.support) -> {
                startActivity(Intent(binding.root.context, WebViewActivity::class.java)
                        .putExtra(WebViewActivity.LINK_TITLE, getString(R.string.support))
                        .putExtra(WebViewActivity.SUPPORT_MODULE, appClientDetails.support_url))
            }
            getString(R.string.logout) -> {
                showLogoutDialog()
            }
            else -> {
                startActivity(Intent(binding.root.context, WebViewActivity::class.java)
                        .putExtra(WebViewActivity.LINK_TITLE, items[pos].title)
                        .putExtra(WebViewActivity.LINK_URL, items[pos].slug))
            }
        }
    }

    private fun goToProfile() {
        startActivityForResult(Intent(requireContext(), DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, DrawerActivity.PROFILE), AppRequestCode.PROFILE_UPDATE)
    }

    private fun openScreen(page: String) {
        startActivity(Intent(requireContext(), DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, page))
    }

    private fun showLogoutDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(
                requireContext(), R.string.sign_out,
                R.string.logout_dialog_message, R.string.yes, R.string.no, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        viewModel.logout()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun bindObservers() {
        viewModel.logout.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    logoutUser(requireActivity(), prefsManager)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            setUserProfile()
        }
    }

}