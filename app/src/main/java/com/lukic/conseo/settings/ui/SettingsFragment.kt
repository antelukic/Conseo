package com.lukic.conseo.settings.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.conseo.MainActivity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.DialogChangeNameBinding
import com.lukic.conseo.databinding.DialogChooseNotificationsBinding
import com.lukic.conseo.databinding.FragmentSettingsBinding
import com.lukic.conseo.settings.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val settingsViewModel by sharedViewModel<SettingsViewModel>()
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.viewModel = settingsViewModel
        binding.view = this
        binding.lifecycleOwner = viewLifecycleOwner

        settingsViewModel.getCurrentUserData()
        settingsViewModel.getDistanceInKm()

        binding.FragmentSettingsDistanceSlider.addOnChangeListener { _, value, _ ->
            settingsViewModel.updateDistance(value)
        }

        settingsViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null)
                Toast.makeText(
                    requireContext(),
                    "Error occurred with getting your data",
                    Toast.LENGTH_LONG
                ).show()
        }

        return binding.root
    }

    fun showChangeNameDialog() {
        val dialog = Dialog(requireContext()).apply {
            setCancelable(true)
            show()
        }
        val binding = DialogChangeNameBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = settingsViewModel

        binding.DialogChangeNameClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun logoutDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to logout")
            .setPositiveButton("Yes") { _, _ ->
                settingsViewModel.logout()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            .setNegativeButton("No") {_, _ -> }
            .create()
            .show()
    }

    fun showNotificationsDialog() {
        val binding = DialogChooseNotificationsBinding.inflate(LayoutInflater.from(context))
        binding.settingsViewModel = settingsViewModel

        val dialog = Dialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
            show()
        }
        settingsViewModel.getAllSubscribedTopics()

        binding.DialogChooseNotificationsSaveButton.setOnClickListener {
            if(binding.DialogChooseNotificationsEventCheckbox.isChecked)
                FirebaseMessaging.getInstance().subscribeToTopic("event")
            if(binding.DialogChooseNotificationsRestaurantCheckbox.isChecked)
                FirebaseMessaging.getInstance().subscribeToTopic("restaurant")
            if(binding.DialogChooseNotificationsBarCheckbox.isChecked)
                FirebaseMessaging.getInstance().subscribeToTopic("bar")
            settingsViewModel.saveNotificationsChoice()
            dialog.dismiss()
        }

        binding.DialogChooseNotificationsClose.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        settingsViewModel.storeDistance()
    }
}

