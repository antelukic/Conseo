package com.lukic.conseo

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
import com.lukic.conseo.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycleScope?.launchWhenCreated {
            binding.FragmentMainBottomNavigation.setupWithNavController(requireActivity().findNavController(R.id.Fragment_Main_FragmentContainerView))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.FragmentMainBottomNavigation.setOnItemSelectedListener(bottomNavListener)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }


    private val bottomNavListener =
        NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.chats -> {
                    findNavController().navigate(R.id.chats)
                    true
                }
                R.id.places -> {
                    findNavController().navigate(R.id.places)
                    true
                }
                R.id.settings -> {
                    findNavController().navigate(R.id.settings)
                    true
                }
                else -> false
            }
        }
}