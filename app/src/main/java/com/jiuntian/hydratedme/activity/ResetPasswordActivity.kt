package com.jiuntian.hydratedme.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.jiuntian.hydratedme.R

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val backButton = findViewById<Button>(R.id.back_button)
        val resetButton = findViewById<Button>(R.id.reset_password_button)

        val resetPasswordEmailInputLayout: TextInputLayout = findViewById(R.id.reset_email_input_layout)

        auth = FirebaseAuth.getInstance()

        backButton.setOnClickListener { _ ->
            finish()
        }

        resetButton.setOnClickListener { _ ->
            val email = findViewById<EditText>(R.id.resetEditTextEmailAddress).text.toString()
            if (email == "") {
                resetPasswordEmailInputLayout.error = getString(R.string.email_required)
            } else {
                resetPasswordEmailInputLayout.error = ""
            }
            if (email != "") {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Password reset email has been sent.",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("resetPassword:sendEmail", "successful send reset password email")
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        applicationContext,
                        exception.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("resetPassword:sendEmail", exception.localizedMessage!!)
                }
            }
        }

    }
}