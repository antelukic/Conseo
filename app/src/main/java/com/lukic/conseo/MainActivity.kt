package com.lukic.conseo

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lukic.conseo.base.BaseActivity
import com.lukic.conseo.databinding.ActivityMainBinding
import com.lukic.conseo.geofencing.GeofencingBroadcastReceiver
import com.lukic.conseo.geofencing.GeofencingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>(){

    private lateinit var geofencingClient: GeofencingClient
    private val geofenceViewModel by viewModel<GeofencingViewModel>()
    private lateinit var navController: NavController
    override fun getLayout(): Int = R.layout.activity_main


    override fun setViews() {
        binding.viewModel = baseViewModel
        binding.lifecycleOwner = this

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.Activity_Main_FragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceViewModel.getAllPlaces()

        baseViewModel.checkUserToken()

        checkIfUserFromMessageNotification()
        checkIfUserFromGeofenceNotification()



        binding.ActivityMainBottomNavigation.setOnItemSelectedListener(bottomNavListener)
        binding.ActivityMainBottomNavigation.setupWithNavController(findNavController(R.id.Activity_Main_FragmentContainerView))

        geofenceViewModel.allPlaces.observe(this){
            if(it != null)
                geofenceViewModel.passPlacesToGeonfencingList()
        }

        GeofencingViewModel.geofenceList.observe(this){
            if(it.isNotEmpty())
                addGeofences()
        }
    }

    private fun checkIfUserFromGeofenceNotification() {
        val serviceID = intent.extras?.getString("placeID")
        val serviceType = intent.extras?.getString("placeType")
        if(!serviceID.isNullOrEmpty() && !serviceType.isNullOrEmpty() && Firebase.auth.currentUser != null) {
            val uri = Uri.parse("conseo://com.lukic.conseo/place-details/$serviceID/$serviceType")
            navController.navigate(uri)
        }
    }

    private fun checkIfUserFromMessageNotification() {
        val receiverID = intent.extras?.getString("receiverID")
        intent.removeExtra("receiverID")
        if(!receiverID.isNullOrEmpty() && Firebase.auth.currentUser != null){
            val uri = Uri.parse("conseo://com.lukic.conseo/messages/$receiverID")
            navController.navigate(uri)
        }
    }


    @SuppressLint("MissingPermission")
    private fun addGeofences(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                geofencingClient.addGeofences(geofenceViewModel.getGeofencingRequest(), geofencePendingIntent)
                    .run {
                        addOnSuccessListener {
                            Log.d("MainActivity", "addGeofences: success")
                        }
                        addOnFailureListener {
                            if ((it.message != null)) {
                                Log.w("BaseActivity", it.message.toString())
                            }
                        }
                    }
            }
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofencingBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val bottomNavListener =
        NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.chats -> {
                    navController.navigate(R.id.chats)
                    true
                }
                R.id.places -> {
                    navController.navigate(R.id.places)
                    true
                }
                R.id.settings -> {
                    navController.navigate(R.id.settings)
                    true
                }
                else -> false
            }
        }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        return navController.navigateUp()
    }
}