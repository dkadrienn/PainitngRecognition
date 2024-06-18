package com.example.paintingrecognition.views.auth.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.paintingrecognition.R
import com.example.paintingrecognition.databinding.FragmentLoginBinding
import com.example.paintingrecognition.views.auth.AuthenticationActivity
import com.example.paintingrecognition.views.main.MainActivity


class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding

    // views
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var notRegisterTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        emailEditText = binding.loginEmailEditText
        passwordEditText = binding.loginPasswordEditText
        loginButton = binding.loginSubmitButton
        notRegisterTextView = binding.loginSigninText

        registerListeners()

        return binding.root
    }

    private fun registerListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthenticationActivity.auth.signInWithEmailAndPassword(email, password)
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

        notRegisterTextView.setOnClickListener {
            notRegisterTextView.setTypeface(null, Typeface.BOLD_ITALIC)
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.run {
                replace(R.id.authentication_main, RegistrationFragment())
                addToBackStack(null)
                commit()
            }
        }
    }
}