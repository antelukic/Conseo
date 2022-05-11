package com.lukic.conseo.loginregister.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentRegisterBinding
import com.lukic.conseo.loginregister.viewmodels.RegisterError
import com.lukic.conseo.loginregister.viewmodels.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class RegisterFragment : Fragment() {

    private val viewModel by sharedViewModel<RegisterViewModel>()
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.error.observe(viewLifecycleOwner){ error ->
            if(error != null)
                setErrorUI(error)
        }

        viewModel.proceed.observe(viewLifecycleOwner){
            findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToSetProfilePicture())
        }

        binding.FragmentRegisterFemale.setOnClickListener {
            viewModel.gender.postValue("Female")
        }

        binding.FragmentRegisterMale.setOnClickListener {
            viewModel.gender.postValue("Male")
        }
        return binding.root
    }

    private fun setErrorUI(error: RegisterError) {
        if(error == RegisterError.InvalidEmail)
            binding.FragmentRegisterEmail.error = error.message
        if(error == RegisterError.PasswordsDontMatch) {
            binding.FragmentRegisterPassword.error = error.message
            binding.FragmentRegisterRepeatPassword.error = error.message
        }
        if(error == RegisterError.EmptyInput){
            binding.FragmentRegisterName.error = error.message
            binding.FragmentRegisterEmail.error = error.message
            binding.FragmentRegisterPassword.error = error.message
            binding.FragmentRegisterRepeatPassword.error = error.message
            binding.FragmentRegisterMale.error = error.message
            binding.FragmentRegisterFemale.error = error.message
        }
    }

}