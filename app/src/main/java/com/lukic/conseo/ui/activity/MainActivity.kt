package com.lukic.conseo.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ActivityMainBinding

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = this
        binding.lifecycleOwner = this

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.Activity_Main_FragmentContainerView
        ) as NavHostFragment

        val navController = navHostFragment.navController

        val mapFragment = supportFragmentManager.findFragmentById(R.id.Fragment_Maps_Map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.ActivityMainBottomNavigation.setupWithNavController(navController)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
       getCurrentLocation()
    }
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionDenied = when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                true
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                true
            } else -> {
                false
            }
        }
    }

    private fun getCurrentLocation(){
        val task = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }else {
            LocationServices.getFusedLocationProviderClient(this).lastLocation
        }

        task.addOnSuccessListener { location ->
            Log.d(TAG, location.toString())
            if(location != null){
                Log.d(TAG, map.toString())
                if(map != null){
                    val latLng = LatLng(location.latitude, location.longitude)

                    val options = MarkerOptions().position(latLng)
                        .title("I am there")


                    map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
                    map!!.addMarker(options)
                }
            }
        }
        task.addOnCanceledListener {
            Log.d(TAG, "canceled")
        }
        task.addOnFailureListener{
            Log.e(TAG, it.message.toString())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}