package com.lukic.conseo.loginregister.ui

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.conseo.databinding.DialogChooseNotificationsBinding
import com.lukic.conseo.databinding.FragmentSetProfilePictureBinding
import com.lukic.conseo.loginregister.viewmodels.RegisterViewModel
import com.lukic.conseo.settings.viewmodels.SettingsViewModel
import lv.chi.photopicker.PhotoPickerFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SetProfilePicture : Fragment(), PhotoPickerFragment.Callback {

    private lateinit var binding: FragmentSetProfilePictureBinding
    private val viewModel: RegisterViewModel by  sharedViewModel()
    private val settingsViewModel by sharedViewModel<SettingsViewModel>()
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
        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner

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

    override fun onImagesPicked(photos: ArrayList<Uri>) {
        val pickedImage: Uri = photos.first()
        imageBitmap = viewModel.getBitmap(pickedImage, requireContext().contentResolver)
        if (imageBitmap != null)
            Glide.with(requireContext()).load(imageBitmap)
                .into(binding.FragmentSetProfilePicturePicture)
    }

    fun openPicker() {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = true,
            maxSelection = 1,
            theme = lv.chi.photopicker.R.style.ChiliPhotoPicker_Light
        ).show(childFragmentManager, "picker")
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