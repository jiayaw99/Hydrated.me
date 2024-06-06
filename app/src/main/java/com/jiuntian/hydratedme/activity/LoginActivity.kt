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

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val emailTextInputLayout: TextInputLayout = findViewById(R.id.login_email_input_layout)
        val passwordTextInputLayout: TextInputLayout = findViewById(R.id.login_password_input_layout)


        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener { _ ->
            val email = findViewById<EditText>(R.id.loginEditTextEmailAddress).text.toString()
            val password = findViewById<EditText>(R.id.loginEditTextPassword).text.toString()

            if (email == "") {
                emailTextInputLayout.error = getString(R.string.email_required)
            } else {
                emailTextInputLayout.error = ""
            }
            if (password == "") {
                passwordTextInputLayout.error = getString(R.string.password_required)
            } else {
                passwordTextInputLayout.error = ""
            }

            if(email != "" && password != "") {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.addOnFailureListener{ exception ->
                    Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
                    Log.e("login:loginAccount", exception.localizedMessage!!)
                }
            }
        }

        val linkToRegister: Button = findViewById(R.id.login_to_register)
        linkToRegister.setOnClickListener { _ ->
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        val resetPasswordButton: Button = findViewById(R.id.login_reset_password_button)
        resetPasswordButton.setOnClickListener { _ ->
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}