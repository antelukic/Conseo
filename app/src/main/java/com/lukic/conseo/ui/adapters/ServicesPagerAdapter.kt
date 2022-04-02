package com.lukic.conseo.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lukic.conseo.ui.fragment.SingleServiceFragment

private const val NUM_TABS = 3
class ServicesPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        return SingleServiceFragment()
    }
}