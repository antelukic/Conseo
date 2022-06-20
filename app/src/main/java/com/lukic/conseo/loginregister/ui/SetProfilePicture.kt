package com.lukic.conseo.loginregister.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.conseo.R
import com.lukic.conseo.databinding.DialogChooseNotificationsBinding
import com.lukic.conseo.databinding.FragmentSetProfilePictureBinding
import com.lukic.conseo.loginregister.viewmodels.RegisterViewModel
import com.lukic.conseo.settings.viewmodels.SettingsViewModel
import org.koin.androidx.navigation.koinNavGraphViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val TAG = "SetProfilePicture"

class SetProfilePicture : Fragment() {

    private lateinit var binding: FragmentSetProfilePictureBinding
    private val viewModel: RegisterViewModel by  sharedViewModel()
    private val settingsViewModel by koinNavGraphViewModel<SettingsViewModel>(R.id.settings)
    private var imageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetProfilePictureBinding.inflate(
            inflater,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.FragmentSetProfilePictureCamera.setOnClickListener {
            if(checkPermissions(Manifest.permission.CAMERA)) {
                takePicture()
            }
            else
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.FragmentSetProfilePictureGallery.setOnClickListener {
            if(checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE))
                chooseImage()
            else
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        binding.FragmentSetProfilePictureProceed.setOnClickListener {
            showNotificationsDialog()
        }

        binding.FragmentSetProfilePictureSkip.setOnClickListener {
            showNotificationsDialog()
        }

        viewModel.isAccountSaved.observe(viewLifecycleOwner){
            if(it)
                findNavController().navigate(SetProfilePictureDirections.actionSetProfilePictureToLoginFragment())
            else
                Toast.makeText(requireContext(), "An error occurred with registration. Please try again!", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }


    private fun checkPermissions(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            resultCameraLauncher.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "takePicture: ERROR ${e.message}")
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        Log.d(TAG, "chooseImage: chooseImage")
        resultGalleryLauncher.launch(intent)
    }

    private val resultCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                imageBitmap = data?.extras?.get("data") as Bitmap
                if (imageBitmap != null)
                    Glide.with(requireContext()).load(imageBitmap)
                        .into(binding.FragmentSetProfilePicturePicture)
            } else {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
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
                Log.e(TAG, "resultGalleryLauncher: $result")
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                return@registerForActivityResult
            }
        }

    private fun showNotificationsDialog() {
        val dialog = Dialog(requireContext())
        val binding = DialogChooseNotificationsBinding.inflate(LayoutInflater.from(context))
        binding.settingsViewModel = settingsViewModel
        dialog.setContentView(binding.root)

        binding.DialogChooseNotificationsSaveButton.setOnClickListener {
            if(binding.DialogChooseNotificationsEventCheckbox.isChecked)
                FirebaseMessaging.getInstance().subscribeToTopic("events")
            if(binding.DialogChooseNotificationsRestaurantCheckbox.isChecked)
                FirebaseMessaging.getInstance().subscribeToTopic("restaurants")
            if(binding.DialogChooseNotificationsBarCheckbox.isChecked)
                FirebaseMessaging.getInstance().subscribeToTopic("bars")
            settingsViewModel.saveNotificationsChoice()
            viewModel.registerAccount(imageBitmap)
            dialog.dismiss()
        }

        binding.DialogChooseNotificationsClose.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }
}