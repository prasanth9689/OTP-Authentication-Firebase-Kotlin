package com.skyblue.skybluea

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.skyblue.skybluea.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var context: Context = this@MainActivity
    private val TAG = "otp_"
    private var mPhoneCode: String? = null
    private var mCountryName: String? = null
    private var mobileFullNo: String? = null
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        auth = Firebase.auth

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                val code: String? = credential.smsCode
                Log.e(TAG, "sms code :" + code.toString())

                if (code != null) {
                    verifyCode(code)
                }
           //     signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number
                binding.status.text = "Code sent success"
                binding.status.setTextColor(
                    ContextCompat.getColor(applicationContext,
                    R.color.green));
                binding.phoneLayout.setVisibility(View.GONE)
                binding.otpVerifyLayout.setVisibility(View.VISIBLE)

                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        onClick()
    }

    private fun onClick() {
      binding.getOtp.setOnClickListener(){
          val mMobile = binding.mobile.text.toString().trim()

          if ("" == mMobile || mMobile.isEmpty()) {
              binding.mobile.error = "Enter valid mobile no"
              binding.mobile.requestFocus()
              return@setOnClickListener
          }

          mPhoneCode = binding.ccp.selectedCountryCodeWithPlus
          mCountryName = binding.ccp.selectedCountryName
          mobileFullNo = mPhoneCode + mMobile

          startPhoneNumberVerification(mobileFullNo.toString())
          binding.status.text = "Send otp please wait"
          binding.status.setTextColor(
              ContextCompat.getColor(applicationContext,
                  R.color.blue))
      }

        binding.verifyOtp.setOnClickListener(){
            val mOtp = binding.otp.text.toString().trim()
            verifyCode(mOtp)
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    binding.status.text = "OTP verification success"
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
    // [END sign_in_with_phone]

    private fun updateUI(user: FirebaseUser? = auth.currentUser) {
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
    }
}