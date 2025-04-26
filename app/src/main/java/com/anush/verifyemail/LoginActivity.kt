package com.anush.verifyemail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anush.verifyemail.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user?.isEmailVerified == true) {
                                Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show()
                                // go to main screen
                            } else {
                                Toast.makeText(this, "Please verify your email.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        binding.resendBtn.setOnClickListener {
            auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Verification email resent.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.gotoSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        auth.currentUser?.reload()
    }
}
