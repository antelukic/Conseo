package com.lukic.conseo.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentAddServiceBinding
import com.lukic.conseo.viewmodel.AddServiceViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.log

private const val TAG = "AddServiceFragment"
class AddServiceFragment : Fragment() {

    private lateinit var binding: FragmentAddServiceBinding
    private val viewModel by sharedViewModel<AddServiceViewModel>()
    private var imageBitmap: Bitmap? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_service, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //This work when user returns from map fragment
        val args by navArgs<AddServiceFragmentArgs>()
        if(args.location.isNotEmpty())
            viewModel.location.value = args.location
        else viewModel.location.value = ""
        if(viewModel.imageBitmap != null)
            Glide.with(requireContext()).load(viewModel.imageBitmap).into(binding.FragmentAddServiceImage)

        checkPermissions()

        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.spinner_dropdown,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.FragmentAddServiceSpinner.adapter = spinnerAdapter

        binding.FragmentAddServiceLookOnMap.setOnClickListener{
            if(!viewModel.location.value.isNullOrEmpty())
                findNavController().navigate(AddServiceFragmentDirections.actionAddServiceFragmentToMapsFragment(viewModel.location.value!!))
            else
                Toast.makeText(requireContext(), "Write the location first!", Toast.LENGTH_LONG).show()
        }

        binding.FragmentAddServiceAddButton.setOnClickListener {
            if(args.location.isNotEmpty()) {
                viewModel.addService(location = args.location)
            } else{
                Toast.makeText(requireContext(), "Add the location first", Toast.LENGTH_LONG).show()
            }
        }

        binding.FragmentAddServiceCameraButton.setOnClickListener {
            takePicture()
        }

        binding.FragmentAddServiceGalleryButton.setOnClickListener{
            chooseImage()
        }

        viewModel.proceed.observe(viewLifecycleOwner){ proceed ->
            if(proceed)
                findNavController().navigate(AddServiceFragmentDirections.actionAddServiceFragmentToServicesFragment())
            else
                Toast.makeText(requireContext(), "Something went wrong, please try again later!", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    private fun checkPermissions() {
        if(ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PERMISSION_DENIED)
            showPermissionDialog()

        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_DENIED)
            showPermissionDialog()

    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            resultCameraLauncher.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_LONG).show()
        }
    }

    private fun chooseImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        resultGalleryLauncher.launch(intent)
    }

    private val resultCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.imageBitmap = result.data?.extras?.get("data") as Bitmap
            Log.d(TAG, viewModel.imageBitmap.toString())
            if(viewModel.imageBitmap != null)
                Glide.with(requireContext()).load(viewModel.imageBitmap).into(binding.FragmentAddServiceImage)
        } else {
            Log.d(TAG, result.toString())
        }
    }

    private val resultGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            if(result.data?.data != null) {
                val pickedImage: Uri = result.data?.data!!
                imageBitmap = viewModel.getBitmap(pickedImage, requireContext().contentResolver)
                if(imageBitmap != null)
                    Glide.with(requireContext()).load(imageBitmap).into(binding.FragmentAddServiceImage)
            } else {
                Log.e(TAG, "ERROR: ${result.toString()}")
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                return@registerForActivityResult
            } else {
                showPermissionDialog()
            }
        }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setTitle("Permission required")
            setMessage("If you press no you can only skip this part")
            setPositiveButton("Yes"){_, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            setNegativeButton("No"){_, _ ->
                binding.FragmentAddServiceCameraButton.isEnabled = false
                binding.FragmentAddServiceGalleryButton.isEnabled = false
            }
        }.show()
    }

}