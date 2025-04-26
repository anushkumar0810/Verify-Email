package com.anush.verifyemail

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anush.verifyemail.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var verificationId: String
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please wait...")

        // Email login
        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                progressDialog.show()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user?.isEmailVerified == true) {
                                Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show()
                                // Navigate to main screen
                            } else {
                                Toast.makeText(this, "Please verify your email.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        // Resend email verification
        binding.resendBtn.setOnClickListener {
            auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Verification email resent.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navigate to signup
        binding.gotoSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Send OTP
        binding.sendOtpBtn.setOnClickListener {
            val phoneNumber = binding.phoneEt.text.toString().trim()

            if (phoneNumber.isNotEmpty()) {
                progressDialog.show()
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            progressDialog.dismiss()
                            signInWithPhoneAuthCredential(credential)
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            progressDialog.dismiss()
                            Toast.makeText(this@LoginActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                            progressDialog.dismiss()
                            this@LoginActivity.verificationId = verificationId
                            Toast.makeText(this@LoginActivity, "OTP sent!", Toast.LENGTH_SHORT).show()
                            disableSendOtpButtonFor60Seconds()
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } else {
                Toast.makeText(this, "Enter valid phone number.", Toast.LENGTH_SHORT).show()
            }
        }

        // When user presses done in OTP field (keyboard done button)
        binding.otpEt.setOnEditorActionListener { _, _, _ ->
            checkOtp()
            false
        }

        // Check OTP manually
        binding.checkOtpBtn.setOnClickListener {
            checkOtp()
        }
    }

    private fun checkOtp() {
        val code = binding.otpEt.text.toString().trim()
        if (::verificationId.isInitialized && code.isNotEmpty()) {
            progressDialog.show()
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Please enter the OTP.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(this, "Phone login successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to main screen
                } else {
                    Toast.makeText(this, "Phone login failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun disableSendOtpButtonFor60Seconds() {
        binding.sendOtpBtn.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            binding.sendOtpBtn.isEnabled = true
        }, 60000)
    }
}
