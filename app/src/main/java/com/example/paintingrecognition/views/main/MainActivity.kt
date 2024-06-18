package com.example.paintingrecognition.views.main

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.paintingrecognition.R
import com.example.paintingrecognition.databaseUtils.CapturedImageDatabase
import com.example.paintingrecognition.databinding.ActivityMainBinding
import com.example.paintingrecognition.utils.IOnBackPressed
import com.example.paintingrecognition.utils.Navigator
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel
import com.example.paintingrecognition.views.main.fragments.ScanFragment

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var navigator: Navigator

    }

    private lateinit var binding: ActivityMainBinding
    lateinit var db: CapturedImageDatabase

    private val capturedImageViewModel by viewModels<CapturedImageViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CapturedImageViewModel(db.capturedImageDao) as T
                }
            }
        }
    )

    private val scanViewModel by viewModels<ScanViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScanViewModel(db.scanResultDao) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        db = Room.databaseBuilder(applicationContext, CapturedImageDatabase::class.java, "captured_image.db")
            .fallbackToDestructiveMigration()
            .build()

        navigator = Navigator(supportFragmentManager, capturedImageViewModel, scanViewModel)

        // needed to have good effect clicking on a menu item
        binding.bottomNavigationView.background = null
        // needed to make unable users to click on the placeholder item
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false

        setUpNavigationListener()

        // set the initial selected item
        binding.bottomNavigationView.selectedItemId = R.id.home

        setContentView(binding.root)
    }


    private fun setUpNavigationListener() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> navigator.openHomeFragment()
                R.id.history -> navigator.openHistoryFragment()
            }
            // reset floating button color to white
            binding.floatingActionButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(
                R.color.white
            ))
            true
        }

        binding.floatingActionButton.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.main_container)

            if (fragment is ScanFragment) {
                fragment.capturePhoto()
            } else {
                // set the placeholder to remove selection from other menu items
                binding.bottomNavigationView.selectedItemId = R.id.placeholder
                // set selected color for floating button
                binding.floatingActionButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(
                    R.color.customColor
                ))
                navigator.openScanFragment()
            }

        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.main_container)
        if (fragment is IOnBackPressed) {
            fragment.onBackPressed()
        }
        super.onBackPressed()
    }
}