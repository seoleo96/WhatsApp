package com.example.whatsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title = "LoginActivity"

        need_new_account.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        login_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val email = login_email.text.toString()
        val password = login_password.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(applicationContext, "email or password is empty", Toast.LENGTH_SHORT).show()
            return
        }else{
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "email successfully created" + it.result.toString())
                        Toast.makeText(applicationContext, "sign in email and password ", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }else{
                        Log.d(TAG, "email did not create")

                        Toast.makeText(applicationContext, "email or password incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
