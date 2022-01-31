package com.consultantvendor.ui.drawermenu

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.consultantvendor.R
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityContainerBinding
import com.consultantvendor.ui.chat.ChatFragment
import com.consultantvendor.ui.dashboard.feeds.AddFeedFragment
import com.consultantvendor.ui.dashboard.feeds.FeedDetailsFragment
import com.consultantvendor.ui.dashboard.feeds.FeedsFragment
import com.consultantvendor.ui.dashboard.home.AppointmentFragment
import com.consultantvendor.ui.dashboard.home.detail.AppointmentDetailFragment
import com.consultantvendor.ui.dashboard.home.prescription.digital.DigitalPrescriptionFragment
import com.consultantvendor.ui.dashboard.home.prescription.manual.ManualPrescriptionFragment
import com.consultantvendor.ui.dashboard.location.LocationFragment
import com.consultantvendor.ui.dashboard.wallet.PayoutFragment
import com.consultantvendor.ui.dashboard.wallet.addmoney.AddCardFragment
import com.consultantvendor.ui.drawermenu.classes.ClassesFragment
import com.consultantvendor.ui.drawermenu.notification.NotificationFragment
import com.consultantvendor.ui.drawermenu.profile.ProfileFragment
import com.consultantvendor.ui.loginSignUp.changepassword.ChangePasswordFragment
import com.consultantvendor.utils.*
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class DrawerActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    lateinit var binding: ActivityContainerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialise()
    }

    private fun initialise() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_container)

        LocaleHelper.setLocale(this, userRepository.getUserLanguage(), prefsManager)

        when (intent.getStringExtra(PAGE_TO_OPEN)) {
            PROFILE ->
                addFragment(supportFragmentManager,
                        ProfileFragment(), R.id.container)
            NOTIFICATION ->
                addFragment(supportFragmentManager,
                        NotificationFragment(), R.id.container)
            CHANGE_PASSWORD ->
                addFragment(supportFragmentManager,
                        ChangePasswordFragment(), R.id.container)
            USER_CHAT ->
                addFragment(supportFragmentManager,
                        ChatFragment(), R.id.container)
            APPOINTMENT, SECOND_OPINION ->
                addFragment(supportFragmentManager,
                        AppointmentFragment(), R.id.container)
            APPOINTMENT_DETAIL ->
                addFragment(supportFragmentManager,
                        AppointmentDetailFragment(), R.id.container)
            PAYOUT ->
                addFragment(supportFragmentManager,
                        PayoutFragment(), R.id.container)
            CLASSES ->
                addFragment(supportFragmentManager,
                        ClassesFragment(), R.id.container)
            ADD_CARD ->
                addFragment(supportFragmentManager,
                        AddCardFragment(), R.id.container)

            BLOGS, ARTICLE ->
                addFragment(supportFragmentManager,
                        FeedsFragment(), R.id.container)
            ADD_ARTICLE, ADD_BLOG ->
                addFragment(supportFragmentManager,
                        AddFeedFragment(), R.id.container)
            BLOGS_DETAILS -> {
                val fragment = FeedDetailsFragment()
                val bundle = Bundle()
                bundle.putSerializable(EXTRA_REQUEST_ID, intent.getSerializableExtra(EXTRA_REQUEST_ID))
                fragment.arguments = bundle
                addFragment(supportFragmentManager, fragment, R.id.container)
            }
            PrescriptionType.MANUAL ->
                addFragment(supportFragmentManager,
                        ManualPrescriptionFragment(), R.id.container)
            PrescriptionType.DIGITAL ->
                addFragment(supportFragmentManager,
                        DigitalPrescriptionFragment(), R.id.container)
            LOCATION ->
                addFragment(supportFragmentManager,
                        LocationFragment(), R.id.container)
        }
    }

    companion object {
        const val HISTORY = "HISTORY"
        const val PROFILE = "PROFILE"
        const val CHANGE_PASSWORD = "CHANGE_PASSWORD"
        const val NOTIFICATION = "NOTIFICATION"
        const val USER_CHAT = "USER_CHAT"
        const val PAYOUT = "PAYOUT"
        const val CLASSES = "CLASSES"
        const val APPOINTMENT = "APPOINTMENT"
        const val APPOINTMENT_DETAIL = "APPOINTMENT_DETAIL"
        const val SECOND_OPINION = "SECOND_OPINION"
        const val ADD_CARD = "ADD_CARD"
        const val BLOGS = "BLOGS"
        const val ARTICLE = "ARTICLE"
        const val ADD_ARTICLE = "ADD_ARTICLE"
        const val ADD_BLOG = "ADD_BLOG"
        const val BLOGS_DETAILS = "BLOGS_DETAILS"
        const val LOCATION = "LOCATION"
    }

}
