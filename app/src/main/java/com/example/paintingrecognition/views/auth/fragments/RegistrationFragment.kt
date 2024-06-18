package com.example.paintingrecognition.views.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.paintingrecognition.databinding.FragmentRegistrationBinding
import com.example.paintingrecognition.views.auth.AuthenticationActivity
import com.example.paintingrecognition.views.main.MainActivity

class RegistrationFragment : Fragment() {

    lateinit var binding: FragmentRegistrationBinding

    // vies
    private lateinit var emailTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var registerButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        emailTextField = binding.registerEmailEditText
        passwordTextField = binding.registerPasswordEditText
        registerButton = binding.registerSubmitButton
        registerListeners()

        return binding.root
    }

    fun registerListeners() {
        registerButton.setOnClickListener {
            val email = emailTextField.text.toString()
            val password = passwordTextField.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthenticationActivity.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            startActivity(Intent(context, MainActivity::class.java))
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

}