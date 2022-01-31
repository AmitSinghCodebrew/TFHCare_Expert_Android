package com.consultantvendor.ui.loginSignUp.welcome

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.network.ApiKeys
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.ProviderType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentWelcomeBinding
import com.consultantvendor.ui.adapter.CommonFragmentPagerAdapter
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.ui.loginSignUp.signup.SignUpFragment
import com.consultantvendor.ui.walkthrough.WalkThroughFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class WelcomeFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentWelcomeBinding

    private var rootView: View? = null

    private var callbackManager: CallbackManager? = CallbackManager.Factory.create()

    lateinit var mGoogleSignInClient: GoogleSignInClient

    lateinit var gso: GoogleSignInOptions

    private val RC_SIGN_IN = 111

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            setBanners()
        }
        return rootView
    }

    private fun initialise() {
        binding.tvTitle.text = formatString()

        //initial google sign in
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())


        binding.tvTerms.hideKeyboard()
        binding.tvTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.tvTerms.setText(setAcceptTerms(requireActivity()), TextView.BufferType.SPANNABLE)

        if(!prefsManager.getBoolean(WalkThroughFragment.WALK_THROUGH_SCREEN,false)){
            replaceFragment(requireActivity().supportFragmentManager,
                    WalkThroughFragment(), R.id.container)
        }
    }

    private fun formatString(): SpannableString {
        val createAccount = getString(R.string.create_a_account, getString(R.string.app_name))
        val appName = getString(R.string.app_name)
        val stringFinal = SpannableString.valueOf(createAccount)
        stringFinal.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimary)),
                createAccount.indexOf(appName),
                (createAccount.indexOf(appName) + appName.length), 0)

        return stringFinal
    }

    private fun setBanners() {
        val adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
        adapter.addTab("", BannerFragment())
        binding.viewPager.adapter = adapter

        binding.pageIndicatorView.setViewPager(binding.viewPager)
        binding.pageIndicatorView.hideShowView(adapter.fragments.count() > 1)
    }

    private fun listeners() {
        binding.tvMobile.setOnClickListener {
            replaceFragment(
                    requireActivity().supportFragmentManager,
                    LoginFragment(), R.id.container
            )
        }

        binding.tvLogin.setOnClickListener {
            replaceFragment(
                    requireActivity().supportFragmentManager,
                    LoginFragment(), R.id.container
            )
        }

        binding.tvEmail.setOnClickListener {
            replaceFragment(
                    requireActivity().supportFragmentManager,
                    SignUpFragment(), R.id.container
            )
        }

        binding.tvFacebook.setOnClickListener {
            loginFacebook()
        }

        binding.tvGoogle.setOnClickListener {
            loginGoogle()
        }
    }

    private fun loginGoogle() {
        mGoogleSignInClient.signOut()
        mGoogleSignInClient.revokeAccess()
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    /*Login with Facebook*/
    private fun loginFacebook() {
        LoginManager.getInstance().logOut()
        LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("public_profile", "email"))

        LoginManager.getInstance().registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {

            override fun onSuccess(loginResult: LoginResult) {
                if (isConnectedToInternet(requireActivity(), true)) {

                    val hashMap = HashMap<String, Any>()
                    hashMap[ApiKeys.PROVIDER_TYPE] = ProviderType.facebook
                    hashMap[ApiKeys.PROVIDER_VERIFICATION] = loginResult.accessToken.token
                    hashMap[ApiKeys.USER_TYPE] = APP_TYPE

                    viewModel.login(hashMap)
                }
            }

            override fun onCancel() {
                Log.e("FBLOGIN_FAILD", "Cancel")
            }

            override fun onError(error: FacebookException) {
                Log.e("FBLOGIN_FAILD", "ERROR", error)
            }
        })
    }


    private fun bindObservers() {
        viewModel.login.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    if (userRepository.isUserLoggedIn()) {
                        startActivity(Intent(requireActivity(), HomeActivity::class.java))
                        requireActivity().finish()
                    } else {
                        val fragment = LoginFragment()
                        val bundle = Bundle()
                        bundle.putBoolean(UPDATE_NUMBER, true)
                        fragment.arguments = bundle

                        replaceFragment(
                                requireActivity().supportFragmentManager,
                                fragment, R.id.container
                        )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                if (isConnectedToInternet(requireActivity(), true)) {

                    val hashMap = HashMap<String, Any>()
                    hashMap[ApiKeys.PROVIDER_TYPE] = ProviderType.google
                    hashMap[ApiKeys.PROVIDER_VERIFICATION] = account?.idToken ?: ""
                    hashMap[ApiKeys.USER_TYPE] = APP_TYPE

                    viewModel.login(hashMap)
                }

            } catch (e: ApiException) {
                Log.e("Google_FAILD", "ERROR", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvEmail.post {
            binding.tvEmail.requestFocus()
            binding.tvEmail.hideKeyboard()
        }
    }
}
