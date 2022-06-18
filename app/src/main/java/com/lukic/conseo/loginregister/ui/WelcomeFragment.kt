package com.lukic.conseo.loginregister.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lukic.conseo.R
import com.lukic.conseo.base.BaseViewModel
import com.lukic.conseo.databinding.FragmentWelcomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    private val baseViewModel by sharedViewModel<BaseViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        binding.model = this
        binding.lifecycleOwner = viewLifecycleOwner

        baseViewModel.bottomNavVisibility.postValue(false)

        setTextAnimation()


        binding.FragmentWelcomeLogin.setOnClickListener{
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToLoginFragment())
        }

        binding.FragmentWelcomeRegister.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToRegisterFragment())
        }

        return binding.root
    }

    private fun setTextAnimation() {
        lifecycleScope.launch {
            delay(100)
            var text = ""
            for (letter in getString(R.string.welcome_to_conseo)) {
                text += letter
                binding.FragmentWelcomeTitle.text = text
                delay(20)
            }

            if(isUserLoggedIn()) {
                delay(1000)
                findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToMainNavGraph())
            }

            positionWelcomeText()
            text = ""
            for (letter in getString(R.string.login_or_register)) {
                text += letter
                binding.FragmentWelcomeSubtitle.text = text
                delay(20)
            }
            showButtons()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return Firebase.auth.currentUser != null
    }

    private fun showButtons() {
        binding.FragmentWelcomeRegister.visibility = View.VISIBLE
        binding.FragmentWelcomeLogin.visibility = View.VISIBLE
    }

    private fun positionWelcomeText() {
        ObjectAnimator.ofFloat(
            binding.FragmentWelcomeTitle,
            "translationY", -250f
        ).apply {
            duration = 500
            start()
        }
    }

}