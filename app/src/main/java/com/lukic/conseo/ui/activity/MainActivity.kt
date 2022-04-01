package com.lukic.conseo.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ActivityMainBinding

private const val ERROR_DIALOG_REQUEST = 9001
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = this
        binding.lifecycleOwner = this

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.Activity_Main_FragmentContainerView
        ) as NavHostFragment

        val navController = navHostFragment.navController

        binding.ActivityMainBottomNavigation.setupWithNavController(navController)
    }




    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}