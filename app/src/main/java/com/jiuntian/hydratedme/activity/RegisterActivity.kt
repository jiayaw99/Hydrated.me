package com.jiuntian.hydratedme.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.PetDataController
import com.jiuntian.hydratedme.model.PetType

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailInputLayout: TextInputLayout = findViewById(R.id.register_email_input_layout)
        val passwordInputLayout: TextInputLayout = findViewById(R.id.register_password_input_layout)

        val registerButton: Button = findViewById(R.id.register_button)
        registerButton.setOnClickListener { _ ->
            val email = findViewById<EditText>(R.id.registerEditTextEmailAddress).text.toString()
            val password = findViewById<EditText>(R.id.registerEditTextPassword).text.toString()

            if (email == "") {
                emailInputLayout.error = getString(R.string.email_required)
            } else {
                emailInputLayout.error = ""
            }
            if (password == "") {
                passwordInputLayout.error = getString(R.string.password_required)
            } else {
                passwordInputLayout.error = ""
            }

            if(email != "" && password != "") {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        PetDataController.createPetData(100, 0, PetType.CAT)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        applicationContext,
                        exception.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("register:createAccount", exception.localizedMessage!!)
                }
            }
        }

        val linkToLoginButton: Button = findViewById(R.id.register_sign_in)
        linkToLoginButton.setOnClickListener { _ ->
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val resetPasswordButton: Button = findViewById(R.id.register_reset_password_button)
        resetPasswordButton.setOnClickListener { _ ->
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

    }
}
