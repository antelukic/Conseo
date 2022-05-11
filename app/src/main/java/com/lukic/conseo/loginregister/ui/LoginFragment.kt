package com.lukic.conseo.loginregister.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.lukic.conseo.MainActivity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentLoginBinding
import com.lukic.conseo.loginregister.viewmodels.LoginError
import com.lukic.conseo.loginregister.viewmodels.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.Executor

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by sharedViewModel<LoginViewModel>()
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.proceed.observe(viewLifecycleOwner){
            var showingDialog = false
            if(viewModel.biometricsEnabled.value == false){
                showBiometricsDialog()
                showingDialog = true
            }
            if(it && showingDialog == false)
                startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        viewModel.error.observe(viewLifecycleOwner){ error ->
            if(error == LoginError.SomethingWentWrong)
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            if(error == LoginError.EmailOrPasswordNotValid){
                binding.FragmentLoginEmailLayout.isErrorEnabled = true
                binding.FragmentLoginEmailLayout.error = error.message
                binding.FragmentLoginPasswordLayout.error = error.message
            }
            if(error == null)
                binding.FragmentLoginEmailLayout.isErrorEnabled = false
        }


        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.loginUsingBiometrics()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .build()



        binding.FragmentLoginBiometrics.setOnClickListener{
            biometricPrompt.authenticate(promptInfo)
        }

        return binding.root
    }

    private fun showBiometricsDialog() {
        AlertDialog.Builder(requireContext()).also {
            it.setTitle(getString(R.string.biometric_authentication_question))
            it.setMessage(getString(R.string.biometric_authentication_message))
            it.setPositiveButton(getString(R.string.allow)){_,_->
                viewModel.allowBiometricAuthentication()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            it.setNegativeButton(getString(R.string.dont_allow)){_, _ ->
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
        }.show()
    }
}