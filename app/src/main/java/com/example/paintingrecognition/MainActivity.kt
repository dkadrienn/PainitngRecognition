package com.example.paintingrecognition

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.paintingrecognition.databases.CapturedImageDatabase
import com.example.paintingrecognition.databinding.ActivityMainBinding
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var navigation: Navigation

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

        db = Room.databaseBuilder(applicationContext, CapturedImageDatabase::class.java, "captured_image.db").build()
        navigation = Navigation(supportFragmentManager, capturedImageViewModel, scanViewModel)

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
                R.id.home -> navigation.openHomeFragment()
                R.id.history -> navigation.openHistoryFragment()
            }
            // reset floating button color to white
            binding.floatingActionButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            true
        }

        binding.floatingActionButton.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.mainContainter)

            if (fragment is ScanFragment) {
                fragment.capturePhoto()
            } else {
                // set the placeholder to remove selection from other menu items
                binding.bottomNavigationView.selectedItemId = R.id.placeholder
                // set selected color for floating button
                binding.floatingActionButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryAccent))
                navigation.openScanFragment()
            }

        }
    }
}