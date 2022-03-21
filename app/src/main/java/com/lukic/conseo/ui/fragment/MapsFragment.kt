package com.lukic.conseo.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentMapsBinding
import com.lukic.conseo.viewmodel.MapsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import javax.security.auth.callback.Callback

private const val TAG = "MapsFragment"
class MapsFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback{

    private val viewModel by sharedViewModel<MapsViewModel>()
    private lateinit var binding: FragmentMapsBinding
    private var map: GoogleMap? = null
    private var permissionDenied = false
    private lateinit var client: FusedLocationProviderClient
    private var mapFragment: SupportMapFragment? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        enableMyLocation()



        client = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    private fun requestLocation() {
        val locationManager: LocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var locationEnabled = false
        var networkEnabled = false

        try {
            locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception){
            Log.e(TAG, e.message.toString())
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception){
            Log.e(TAG, e.message.toString())
        }

        if(!locationEnabled && !networkEnabled)
            showEnableDialog()
    }

    private fun showEnableDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Please enable your network connection and location ")
            .setPositiveButton("Open Location settings", object: DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    getCurrentLocation()
                }
            })
            .setNegativeButton("Cancel"){_, _ -> }
            .show()
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(requireContext(), "Current location:\n$p0", Toast.LENGTH_LONG).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        if(permissionDenied) {
            getCurrentLocation()
        } else {
            requestLocation()
        }


    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map?.isMyLocationEnabled = true
            permissionDenied = true
        } else {
            permissionDenied = false
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION
                ,Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }


    override fun onResume() {
        super.onResume()
        if (permissionDenied) {
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
   /* private fun showMissingPermissionError() {
        newInstance(true).show(requireActivity().supportFragmentManager, "dialog")
    }*/

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionDenied = when {
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
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }else {
            client.lastLocation
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


}