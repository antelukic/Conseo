package com.lukic.conseo.loginregister.ui

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lukic.conseo.MainActivity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentVerifyRegistrationBinding
import com.lukic.conseo.loginregister.viewmodels.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class VerifyRegistrationFragment : Fragment() {

    private val viewModel by sharedViewModel<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentVerifyRegistrationBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_verify_registration, container, false)
        binding.model = this
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.isAccountSaved.observe(viewLifecycleOwner){ isSaved ->
            if(isSaved){
                binding.FragmentVerifyRegistrationAnimation.setAnimation("success.json")
                binding.FragmentVerifyRegistrationText.text = getString(R.string.account_saved)
            } else {
                binding.FragmentVerifyRegistrationAnimation.setAnimation("error.json")
                binding.FragmentVerifyRegistrationText.text = getString(R.string.error_saving_account)
            }
        }

        binding.FragmentVerifyRegistrationAnimation.addAnimatorListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }
        })

        return binding.root
    }

}