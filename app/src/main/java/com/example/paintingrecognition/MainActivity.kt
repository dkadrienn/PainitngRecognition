package com.example.paintingrecognition

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
            true
        }

        binding.floatingActionButton.setOnClickListener {
            // set the placeholder to remove selection from other menu items
            binding.bottomNavigationView.selectedItemId = R.id.placeholder
            replaceFragment(ScanFragment())
        }
    }
}