package com.lukic.conseo.places.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentPlacesBinding
import com.lukic.conseo.places.ui.adapters.PlaceAnimationPagerAdapter
import com.lukic.conseo.places.ui.adapters.PlacePagerAdapter
import com.lukic.conseo.places.viewmodels.PlaceViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


private const val  TAG = "ServicesFragment"
class PlacesFragment : Fragment() {

    private lateinit var binding: FragmentPlacesBinding
    private val viewModel by sharedViewModel<PlaceViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_places, container, false)
        binding.model = this
        binding.lifecycleOwner = viewLifecycleOwner

        val tabLayout = binding.FragmentPlaceTabLayout
        tabLayout.addOnTabSelectedListener(tabSelectedListener)

        val animations = listOf("coffee.json", "restaurant.json", "party_ball.json")
        viewModel.getUserLocation()

        binding.FragmentPlaceAnimationPager.apply {
            adapter = PlaceAnimationPagerAdapter(animations)
            isUserInputEnabled = false
        }

        val adapter = PlacePagerAdapter(childFragmentManager, lifecycle)
        val fragmentViewPager = binding.FragmentPlaceViewPager
        fragmentViewPager.adapter = adapter

        TabLayoutMediator(tabLayout, fragmentViewPager) { tab, position ->
            val tabNames = listOf("Bars", "Restaurants", "Events")
            tab.text = tabNames[position]
        }.attach()

        binding.FragmentPlaceAddService.setOnClickListener{
            findNavController().navigate(R.id.action_servicesFragment_to_addServiceFragment)
        }


        return binding.root
    }


    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab != null)
                binding.FragmentPlaceAnimationPager.setCurrentItem(tab.position, true)
            when (tab?.position) {
                0 -> {
                    viewModel.getAllItemsByService("bars")
                }
                1 -> {
                    viewModel.getAllItemsByService("restaurants")
                }
                2 -> {
                    viewModel.getAllItemsByService("events")
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }

}