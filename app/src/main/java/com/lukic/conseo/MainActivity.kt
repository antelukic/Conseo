package com.lukic.conseo

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.conseo.base.BaseActivity
import com.lukic.conseo.base.BaseViewModel
import com.lukic.conseo.databinding.ActivityMainBinding
import com.lukic.conseo.geofencing.GeofencingBroadcastReceiver
import com.lukic.conseo.geofencing.GeofencingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "MainActivity"
class MainActivity : BaseActivity<ActivityMainBinding>(){

    private lateinit var geofencingClient: GeofencingClient
    private val geofenceViewModel by viewModel<GeofencingViewModel>()
    private val baseViewModel by viewModel<BaseViewModel>()

    override fun getLayout(): Int = R.layout.activity_main


    override fun setViews() {
        binding.model = this
        binding.lifecycleOwner = this

        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceViewModel.getAllPlaces()

        baseViewModel.checkUserToken()

        val receiverID = intent.extras?.getString("receiverID")
        if(!receiverID.isNullOrEmpty() && Firebase.auth.currentUser != null){
            val uri = Uri.parse("conseo://com.lukic.conseo/messages/$receiverID")
            findNavController(R.id.Activity_Main_FragmentContainerView).navigate(uri)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("bars")
        geofenceViewModel.allPlaces.observe(this){
            if(it != null)
                geofenceViewModel.passPlacesToGeonfencingList()
        }

        GeofencingViewModel.geofenceList.observe(this){
            if(it.isNotEmpty())
                addGeofences()
        }
    }


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
                            // Geofences added.
                            Log.d("BaseActivity", "addGeofences: success")
                        }
                        addOnFailureListener {
                            // Failed to add geofences.
                            Toast.makeText(this@MainActivity, R.string.geofences_not_added,
                                Toast.LENGTH_SHORT).show()
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }
}