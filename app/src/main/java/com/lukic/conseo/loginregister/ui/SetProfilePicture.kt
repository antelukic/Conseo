package com.lukic.conseo.loginregister.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentSetProfilePictureBinding
import com.lukic.conseo.loginregister.viewmodels.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val TAG = "SetProfilePicture"

class SetProfilePicture : Fragment() {

    private lateinit var binding: FragmentSetProfilePictureBinding
    private val viewModel: RegisterViewModel by sharedViewModel()
    private var imageBitmap: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_set_profile_picture,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        checkPermissions()

        binding.FragmentSetProfilePictureCamera.setOnClickListener {
            takePicture()
        }

        binding.FragmentSetProfilePictureGallery.setOnClickListener {
            chooseImage()
        }

        binding.FragmentSetProfilePictureProceed.setOnClickListener {
            viewModel.registerAccount(imageBitmap)
            findNavController().navigate(SetProfilePictureDirections.actionSetProfilePictureToVerifyRegistrationFragment())
        }

        binding.FragmentSetProfilePictureSkip.setOnClickListener {
            viewModel.registerAccount(imageBitmap)
            findNavController().navigate(SetProfilePictureDirections.actionSetProfilePictureToVerifyRegistrationFragment())
        }

        return binding.root
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        )
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

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        resultGalleryLauncher.launch(intent)
    }

    private val resultCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "nes: $result")
            if (result.resultCode == Activity.RESULT_OK) {
                imageBitmap = result.data?.extras?.get("data") as Bitmap
                if (imageBitmap != null)
                    Glide.with(requireContext()).load(imageBitmap)
                        .into(binding.FragmentSetProfilePicturePicture)
            } else {
                Log.d(TAG, result.toString())
            }
        }

    private val resultGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data?.data != null) {
                    val pickedImage: Uri = result.data?.data!!
                    imageBitmap = viewModel.getBitmap(pickedImage, requireContext().contentResolver)
                    if (imageBitmap != null)
                        Glide.with(requireContext()).load(imageBitmap)
                            .into(binding.FragmentSetProfilePicturePicture)
                }
            } else {
                Log.e(TAG, "resultGalleryLauncher: $result", )
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
            setPositiveButton("Yes") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            setNegativeButton("No") { _, _ ->
                binding.FragmentSetProfilePictureCamera.isEnabled = false
                binding.FragmentSetProfilePictureGallery.isEnabled = false
                binding.FragmentSetProfilePictureProceed.isEnabled = false
            }
        }.show()
    }

}