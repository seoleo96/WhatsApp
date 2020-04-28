package com.example.whatsapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private val GalleryPick = 1

    lateinit var userImageRef : StorageReference
    lateinit var resultUri : Uri

    lateinit var currentUserId : String
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.title = "Settings"

        userImageRef = FirebaseStorage.getInstance().getReference("/Images/")
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        progressDialog = ProgressDialog(this)

        update_button_settings.setOnClickListener {
            updateSettings()
        }
    }


    private fun updateSettings() {
        val setUsername = username_settings.text.toString()
        val setStatus = status_settings.text.toString()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        if (setStatus.isEmpty() || setUsername.isEmpty()){
            Toast.makeText(applicationContext, "username or status or image incorrect", Toast.LENGTH_SHORT).show()
            return
        }else{

            val profileMap = HashMap<String, String>()
            profileMap.put("uid", currentUserId)
            profileMap.put("name", setUsername)
            profileMap.put("status", setStatus)

            FirebaseDatabase.getInstance().reference
                .child("Users").child(currentUserId).setValue(profileMap)

                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                        Toast.makeText(applicationContext,"Username successfully created", Toast.LENGTH_SHORT).show()
                    }else{
                        val errorString = it.exception.toString()
                        Toast.makeText(applicationContext,errorString, Toast.LENGTH_SHORT).show()
                    }
                }




        }

    }
}
