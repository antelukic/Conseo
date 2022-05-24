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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.lukic.conseo.R
import com.lukic.conseo.loginregister.ui.LoginRegisterActivity
import com.lukic.conseo.settings.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SettingsFragment : Fragment() {

    private val settingsViewModel by sharedViewModel<SettingsViewModel>()
    private lateinit var binding: com.lukic.conseo.databinding.FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        binding.viewModel = settingsViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        settingsViewModel.getCurrentUserData()
        settingsViewModel.getDistance()

        binding.FragmentSettingsChangeName.setOnClickListener {
            showChangeNameDialog()
        }

        binding.FragmentSettingsSaveDistanceButton.setOnClickListener {
            settingsViewModel.updateDistance(binding.FragmentSettingsCurrentMaxDistance.text.toString().toInt())
        }

        settingsViewModel.distance.observe(viewLifecycleOwner){ distance ->
            binding.FragmentSettingsCurrentMaxDistance.setText(distance.toString())
        }

        binding.FragmentSettingsSaveDistanceButton.setOnClickListener(setNewDistanceClickListener)

        binding.FragmentSettingsLogout.setOnClickListener(logoutClickListener)

        settingsViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null)
                Toast.makeText(
                    requireContext(),
                    "Error occured with getting your data",
                    Toast.LENGTH_LONG
                ).show()
            else
                Glide
                    .with(requireContext())
                    .load(user.image)
                    .error(R.mipmap.ic_launcher)
                    .into(binding.FragmentSettingsUserImage)

        }

        return binding.root
    }

    private fun showChangeNameDialog() {
        val dialog = Dialog(requireContext()).apply {
            setCancelable(false)
            setContentView(R.layout.dialog_change_name)
        }

        val nameEditText = dialog.findViewById<EditText>(R.id.Dialog_ChangeName_NewName)
        val button = dialog.findViewById<Button>(R.id.Dialog_ChangeName_Button)

        button.setOnClickListener {
            settingsViewModel.changeName(nameEditText.text.toString())
            dialog.dismiss()
        }
        dialog.show()
    }

    private val logoutClickListener = View.OnClickListener {
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to logout")
            .setPositiveButton("Yes") { _, _ ->
                settingsViewModel.logout()
                startActivity(Intent(requireContext(), LoginRegisterActivity::class.java))
            }
            .setNegativeButton("No") {_, _ -> }
            .create()
            .show()
    }

    private val setNewDistanceClickListener = View.OnClickListener {
        settingsViewModel.updateDistance(binding.FragmentSettingsCurrentMaxDistance.text.toString().take(5).toInt())
    }
}

