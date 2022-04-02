package com.lukic.conseo.ui.fragment

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentMapsBinding
import java.io.IOException

private const val TAG = "MapsFragment"
class MapsFragment : Fragment() {

    private lateinit var binding: FragmentMapsBinding
    private var _client: FusedLocationProviderClient? = null
    private var _locationPermissionGranted = false
    private var mMap: GoogleMap? = null
    private var searchText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false)
        binding.model = this
        binding.lifecycleOwner = viewLifecycleOwner

        getLocationPermission()

        getDeviceLocation()

        binding.FragmentMapsSearchButton.setOnClickListener {
            if (!binding.FragmentMapsSearch.text.isNullOrEmpty()) {
                searchText = binding.FragmentMapsSearch.text.toString()
                geoLocate()
            }
        }



        return binding.root
    }

    private fun geoLocate(){
        val geocoder: Geocoder = Geocoder(requireContext())
        var list = mutableListOf<Address>()
        try {
            list = geocoder.getFromLocationName(searchText, 1)
            if(list.isNotEmpty()){
                val address = list.first()
                moveCamera(LatLng(address.latitude, address.longitude), address.getAddressLine(0))
            }
        }catch (e: IOException){
            Log.e(TAG, e.message.toString())
        }
    }

    private fun getDeviceLocation(){
        _client = LocationServices.getFusedLocationProviderClient(requireContext())

        try {
            if(_locationPermissionGranted){
                val location = _client?.lastLocation
                location?.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val lat = task.result.latitude
                        val lng = task.result.longitude
                        moveCamera(LatLng(lat, lng),  "")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Unable to get your current location",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }catch (e: SecurityException){
            Log.e(TAG, e.message.toString())
        }
    }

    private fun moveCamera(latLng: LatLng, title: String){

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getLocationPermission()
            return
        }
        hideSoftKeyboard()
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))

        if(title.isNotEmpty()){
            val options: MarkerOptions = MarkerOptions().position(latLng)
                .title(title)
            mMap?.addMarker(options)

        }
        mMap?.isMyLocationEnabled = _locationPermissionGranted
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isCompassEnabled = false
    }

    private fun getLocationPermission(){
        val permissions = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        if(ContextCompat.checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            initMap()
            _locationPermissionGranted = true
        } else{
            ActivityCompat.requestPermissions(requireActivity(), permissions, 123)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 123 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initMap()
                    _locationPermissionGranted = true
                } else
                    Toast.makeText(requireContext(), "Map is required to proceed", Toast.LENGTH_LONG).show()
            }else
                Toast.makeText(requireContext(), "Map is required to proceed", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
        }
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.Fragment_Maps_Map) as SupportMapFragment
        mapFragment.getMapAsync(mapReadyCallback)
    }

    private val mapReadyCallback = OnMapReadyCallback{ googleMap: GoogleMap ->
        Log.d(TAG, "google map" + googleMap.toString())
        mMap = googleMap
    }

    private fun hideSoftKeyboard(){
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

}