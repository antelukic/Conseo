package com.lukic.conseo.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.lukic.conseo.MyApplication
import com.lukic.conseo.utils.CheckNetworkConnection
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {

    lateinit var binding: VB
    private var networkAlertDialog: AlertDialog? = null
    private var locationAlertDialog: AlertDialog? = null
    private var locManager: LocationManager? = null
    val baseViewModel by viewModel<BaseViewModel>()


    abstract fun getLayout(): Int

    abstract fun setViews()

    override fun onResume() {
        super.onResume()
        if (locManager != null)
            startRequestingLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayout())

        setViews()
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) && checkPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        )
            locManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager



        val checkNetworkConnection = CheckNetworkConnection(MyApplication.getInstance())
        checkNetworkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                showInternetDialog()
            } else {
                if (networkAlertDialog?.isShowing == true)
                    networkAlertDialog?.dismiss()
            }
        }


    }



    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                return@registerForActivityResult
            } else
                showLocationDialog()
        }

    private fun checkPermission(permission: String): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(permission)
            false
        } else
            true
    }

    private fun showInternetDialog() {
        networkAlertDialog = this.let {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("Network connection error")
                setMessage("Please turn on your internet data")
            }
            builder.show()
        }
        networkAlertDialog?.show()
    }

    private fun showLocationDialog() {
        locationAlertDialog = this.let {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("Location connection error")
                setMessage("Please accept Location permissions and turn on your location to proceed")
            }
            builder.show()
        }
        networkAlertDialog?.show()
    }





    private val locListener: LocationListener by lazy {
        object : LocationListener {
            override fun onLocationChanged(loc: Location) {
            }

            override fun onProviderEnabled(provider: String) {
                if (locationAlertDialog?.isShowing == true)
                    locationAlertDialog?.dismiss()
            }

            override fun onProviderDisabled(provider: String) {
                showLocationDialog()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (locManager != null)
                locManager!!.removeUpdates(locListener)
        } catch (e: SecurityException) {
            Log.e("BaseActivity", "onStop: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRequestingLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locListener)
        } else {
            showLocationDialog()
        }
    }


}