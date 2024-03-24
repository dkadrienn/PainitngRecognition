package com.example.paintingrecognition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paintingrecognition.databinding.ActivityAuthenticationBinding
import com.google.firebase.auth.FirebaseAuth

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    companion object {
        lateinit var auth: FirebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        setContentView(binding.root)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.authentication_main, LoginFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }
}