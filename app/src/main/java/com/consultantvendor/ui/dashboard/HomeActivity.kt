package com.consultantvendor.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.SaveAddress
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityHomeBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.google.android.gms.location.*
import dagger.android.support.DaggerAppCompatActivity
import java.util.*
import javax.inject.Inject


class HomeActivity : DaggerAppCompatActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appSocket: AppSocket

    lateinit var binding: ActivityHomeBinding

    private var currentNavController: LiveData<NavController>? = null

    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var geoCoder: Geocoder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        initialise()

        setNavigation()
        listeners()
    }

    private fun setNavigation() {
        val navGraphIds = listOf(
                R.navigation.navigation_home,
                R.navigation.navigation_wallet,
                R.navigation.navigation_revenue,
                R.navigation.navigation_profile)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = binding.bottomNav.setupWithNavController(
                navGraphIds = navGraphIds,
                fragmentManager = supportFragmentManager,
                containerId = R.id.nav_host_fragment,
                intent = intent
        )

        currentNavController = controller

        if (intent.hasExtra(EXTRA_TAB)) {
            if (intent.getStringExtra(EXTRA_TAB) == "1") {
                binding.bottomNav.selectedItemId = R.id.navigation_wallet
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    private fun initialise() {
        LocaleHelper.setLocale(this, userRepository.getUserLanguage(), prefsManager)
        appSocket.init()

        Log.d("AccessToken", userRepository.getUser()?.token ?: "")

        /* Fetch Notification Token */
        userRepository.pushTokenUpdate()

        /*Ask for location*/
        geoCoder = Geocoder(this, Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }


    private fun listeners() {
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                requestNewLocationData()

            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        runOnUiThread {
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 0
            mLocationRequest.fastestInterval = 0
            mLocationRequest.numUpdates = 1

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper())
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            getLocationName(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        startActivityForResult(Intent(this, DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, DrawerActivity.LOCATION), AppRequestCode.ASK_FOR_LOCATION)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == AppRequestCode.LOCATION_PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun getLocationName(lat: Double, lng: Double) {
        runOnUiThread {
            try {
                var locationName = ""

                val addresses = geoCoder.getFromLocation(lat, lng, 1
                ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                if (addresses.isNotEmpty()) {
                    locationName = when {
                        addresses[0].getAddressLine(1) != null -> addresses[0].getAddressLine(
                                1)
                        addresses[0].featureName == null -> addresses[0].adminArea
                        else -> String.format("%s, %s", addresses[0].featureName,
                                addresses[0].locality)
                    }
                }

                /*Save Address*/
                val address = SaveAddress()
                address.locationName = locationName
                address.location = ArrayList()
                address.location?.add(lng)
                address.location?.add(lat)

                prefsManager.save(USER_ADDRESS, address)
            } catch (e: Exception) {
            }
        }
    }

}
