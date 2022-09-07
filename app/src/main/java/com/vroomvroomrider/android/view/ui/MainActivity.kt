package com.vroomvroomrider.android.view.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.databinding.ActivityMainBinding
import com.vroomvroomrider.android.view.viewmodel.AuthViewModel
import com.vroomvroomrider.android.view.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    private var isHomeSelected = true

    override fun onStart() {
        super.onStart()
        authViewModel.saveIdToken()
        authViewModel.getLocalIdToken()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.itemIconTintList = null
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navController = navHostFragment.findNavController()
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnItemReselectedListener {
            if (it.itemId == R.id.homeFragment) {
                if (isHomeSelected && mainViewModel.isHomeScrolled.value == true) {
                    mainViewModel.setShouldBackToTop(true)
                }
            }
        }

        destinationListener()
        observeIsHomeScrolled()

    }

    private fun destinationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) {
                isHomeSelected = true
            }
            when (destination.id) {
                R.id.homeFragment /** R.id.browseFragment, R.id.ordersFragment, R.id.accountFragment **/ -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    isHomeSelected = false
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }
    }

    private fun observeIsHomeScrolled() {
        mainViewModel.isHomeScrolled.observe(this) {
            if (!isHomeSelected) {
                return@observe
            }

            val homeItem = bottomNavigationView.menu.getItem(0)
            homeItem.icon =
                ContextCompat.getDrawable(
                    this,
                    if (it) R.drawable.home_scrolled_selector else R.drawable.home_selector
                )
        }
    }
}