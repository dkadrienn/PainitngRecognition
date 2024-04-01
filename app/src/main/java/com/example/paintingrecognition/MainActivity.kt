package com.example.paintingrecognition

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.paintingrecognition.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // needed to have good effect clicking on a menu item
        binding.bottomNavigationView.background = null
        // needed to make unable users to click on the placeholder item
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false

        setUpNavigationListener()

        // set the initial selected item
        binding.bottomNavigationView.selectedItemId = R.id.home

        setContentView(binding.root)
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction();
        transaction.replace(R.id.mainContainter, fragment)
        transaction.commit()
    }

    private fun setUpNavigationListener() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
            }
            // reset floating button color to white
            binding.floatingActionButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            true
        }

        binding.floatingActionButton.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.mainContainter)

            if (fragment is HomeFragment) {
                // set the placeholder to remove selection from other menu items
                binding.bottomNavigationView.selectedItemId = R.id.placeholder
                // set selected color for floating button
                binding.floatingActionButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryAccent))
                replaceFragment(ScanFragment())
            } else if (fragment is ScanFragment) {
                fragment.capturePhoto()
            }

        }
    }
}