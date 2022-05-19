package com.lukic.conseo

import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
import com.lukic.conseo.databinding.ActivityMainBinding

private const val ERROR_DIALOG_REQUEST = 9001
private const val TAG = "MainActivity"
class MainActivity : BaseActivity<ActivityMainBinding>(){


    override fun getLayout(): Int = R.layout.activity_main

    override fun setViews() {
        binding.model = this
        binding.lifecycleOwner = this

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.Activity_Main_FragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.ActivityMainBottomNavigation.setupWithNavController(navController)
        binding.ActivityMainBottomNavigation.setOnItemSelectedListener(bottomNavListener)
    }

    private lateinit var navController: NavController
    

    private val bottomNavListener = object: NavigationBarView.OnItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            Log.d(TAG, "onNavigationItemSelected: ${item.itemId}")
            when(item.itemId){
                R.id.chats -> {
                    navController.navigate(R.id.chats)
                    return@onNavigationItemSelected true
                }
                R.id.places -> {
                    navController.navigate(R.id.places)
                    return@onNavigationItemSelected true
                }
                R.id.settings ->{
                    navController.navigate(R.id.settings)
                    return@onNavigationItemSelected true
                }
                else -> return@onNavigationItemSelected false
            }
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}