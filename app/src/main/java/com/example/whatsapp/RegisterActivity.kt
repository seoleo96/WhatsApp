package com.example.whatsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.title = "RegisterActivity"

        already_have_an_account.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        register_button.setOnClickListener {
            registerWithEmailAndPassword()
        }
    }

    private fun registerWithEmailAndPassword() {
        val email = register_email.text.toString()
        val password = register_password.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(applicationContext, "email or password is empty", Toast.LENGTH_SHORT).show()
        }else{
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        createFileDirectoryUsers()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Log.d(TAG, "email successfully created" + it.result.toString())
                        Toast.makeText(applicationContext, "create user email and password successfully created", Toast.LENGTH_SHORT).show()

                    }else{
                        Log.d(TAG, "email did not create")
                        Toast.makeText(applicationContext, "create user email and password did not create", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun createFileDirectoryUsers() {
        val currentUserName = FirebaseAuth.getInstance().currentUser?.uid.toString()
        FirebaseDatabase.getInstance().reference.child("Users").child(currentUserName).setValue("")
    }
}
