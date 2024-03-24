package com.example.paintingrecognition

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.paintingrecognition.databinding.FragmentLoginBinding
import com.google.android.material.textfield.TextInputLayout


class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding

    // views
    private lateinit var emailTextField: TextInputLayout
    private lateinit var passwordTextField: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var notRegistedTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        emailTextField = binding.loginEmailEditText
        passwordTextField = binding.loginPasswordEditText
        loginButton = binding.loginSubmitButton
        notRegistedTextView = binding.notRegisterdText

        notRegistedTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        registerListeners()

        return binding.root
    }

    fun registerListeners() {
        loginButton.setOnClickListener{ view ->
            val email = emailTextField.editText?.text.toString()
            val password = passwordTextField.editText?.text.toString()
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

        notRegistedTextView.setOnClickListener { view ->
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.run {
                replace(R.id.authentication_main, RegistrationFragment())
                addToBackStack(null)
                commit()
            }
        }
    }
}