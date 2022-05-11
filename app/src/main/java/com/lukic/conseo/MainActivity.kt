package com.lukic.conseo

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.Activity_Main_FragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.ActivityMainBottomNavigation.setupWithNavController(navController)
        binding.ActivityMainBottomNavigation.setOnItemSelectedListener(bottomNavListener)
    }

    private val bottomNavListener = object: NavigationBarView.OnItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when(item.itemId){
                R.id.chats -> {
                    navController.navigate(R.id.chats)
                    return@onNavigationItemSelected true
                }
                R.id.services -> {
                    navController.navigate(R.id.services)
                    return@onNavigationItemSelected true
                }
                R.id.settings ->{
                    Toast.makeText(this@MainActivity, "Not available yet", Toast.LENGTH_LONG).show()
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