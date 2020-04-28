package com.example.whatsapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_phone_login.*
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    lateinit var callbacks :PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var resendToken :PhoneAuthProvider.ForceResendingToken
    private val TAG : String = "PhoneLoginActivity"
    lateinit var storedVerificationId : String
    //lateinit var phoneNumber : String
    lateinit var auth : FirebaseAuth

    lateinit var progressBar : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        auth = FirebaseAuth.getInstance()

        progressBar = ProgressDialog(this)

        send_phone_number_button.setOnClickListener {


            val phoneNumber = phone_number_edit_text.text.toString()
            //mCallbacksVerify()

            if (phoneNumber.isEmpty() ){
                Toast.makeText(applicationContext, "phone Number is empty", Toast.LENGTH_SHORT).show()


            }else{

                mCallbacksVerify()
                progressBar.setTitle("Phone verification")
                progressBar.setMessage("please wait")
                progressBar.setCanceledOnTouchOutside(false)
                progressBar.show()


                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber, // Phone number to verify
                    60, // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    this, // Activity (for callback binding)
                    callbacks) // OnVerificationStateChangedCallbacks
            }


        }

        verify_button.setOnClickListener {


            send_phone_number_button.visibility = View.INVISIBLE
            phone_number_edit_text.visibility = View.INVISIBLE

            val verificationCode = phone_number_verify_code_edit.text.toString()
            if (verificationCode.isEmpty()){
                Toast.makeText(applicationContext, "verification code is empty", Toast.LENGTH_SHORT).show()
            }else{
                progressBar!!.setTitle("Code verification")
                progressBar!!.setMessage("please wait")
                progressBar!!.setCanceledOnTouchOutside(false)
                progressBar!!.show()

                val phoneNumber = phone_number_edit_text.text.toString()

                val phoneAuthProvider = PhoneAuthProvider.getInstance()
                phoneAuthProvider.verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    this, /* activity */
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            signInWithPhoneAuthCredential(credential)
                        }

                        override fun onVerificationFailed(p0: FirebaseException) {

                        }

                        // ...
                    })

            }

        }



    }




    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    progressBar.dismiss()

                    Toast.makeText(applicationContext, "verification successfully created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    val message = task.exception.toString()


                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                    }
                }
            }
    }

    private fun mCallbacksVerify(){

        callbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
/*
                progressBar!!.dismiss()
                Toast.makeText(applicationContext, "incorrect phone number or code", Toast.LENGTH_SHORT).show()
                send_phone_number_button.visibility = View.VISIBLE
                phone_number_edit_text.visibility = View.VISIBLE
                verify_button.visibility = View.INVISIBLE
                phone_number_verify_code_edit.visibility = View.INVISIBLE

 */
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                progressBar.dismiss()
                Log.d(TAG, "onCodeSent:$verificationId")

                storedVerificationId = verificationId
                resendToken = token

                send_phone_number_button.visibility = View.INVISIBLE
                phone_number_edit_text.visibility = View.INVISIBLE
                verify_button.visibility = View.VISIBLE
                phone_number_verify_code_edit.visibility = View.VISIBLE

            }
        }
    }

}
