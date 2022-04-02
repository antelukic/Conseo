package com.lukic.conseo.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lukic.conseo.R
import com.lukic.conseo.databinding.FragmentServicesBinding
import com.lukic.conseo.ui.adapters.ServicesAnimationPagerAdapter
import com.lukic.conseo.ui.adapters.ServicesPagerAdapter
import com.lukic.conseo.viewmodel.SingleServiceViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


private const val  TAG = "ServicesFragment"
class ServicesFragment : Fragment() {

    private lateinit var binding: FragmentServicesBinding
    private val viewModel by sharedViewModel<SingleServiceViewModel>()
    val overshootInterpolator by lazy { OvershootInterpolator(2.8f) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_services, container, false)
        binding.model = this
        binding.lifecycleOwner = viewLifecycleOwner

        val tabLayout = binding.FragmentServicesTabLayout
        tabLayout.addOnTabSelectedListener(tabSelectedListener)

        val animations = listOf("coffee.json", "restaurant.json", "party_ball.json")

        binding.FragmentServicesAnimationPager.adapter = ServicesAnimationPagerAdapter(animations)
        binding.FragmentServicesAnimationPager.isUserInputEnabled = false

        val adapter = ServicesPagerAdapter(childFragmentManager, lifecycle)
        val fragmentViewPager = binding.FragmentServicesViewPager
        fragmentViewPager.adapter = adapter

        TabLayoutMediator(tabLayout, fragmentViewPager) { tab, position ->
            val tabNames = listOf("Bars", "Restaurants", "Events")
            tab.text = tabNames[position]
        }.attach()

        return binding.root
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab != null)
                binding.FragmentServicesAnimationPager.setCurrentItem(tab.position, true)
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