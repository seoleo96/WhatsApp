package com.example.whatsapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_setting.*
import java.util.*

class UsersInfoActivity : AppCompatActivity() {

    private val GalleryPick = 1
    lateinit var userImageRef : StorageReference
    lateinit var resultUri : Uri
    lateinit var currentUserId : String
    private var result : CropImage.ActivityResult? = null


    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportActionBar?.title = "Settings"

        progressDialog = ProgressDialog(this)
        userImageRef = FirebaseStorage.getInstance().getReference("/Images/")
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        profile_image.setOnClickListener {
            toGalery()
        }


        update_button.setOnClickListener {
            saveUsersInfo()
        }
    }

    private fun saveUsersInfo() {
        progressDialog.setTitle("Set profile image")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        saveImageToFirebaseDatabase()
        saveUsersInfoToFirebaseDatabase()

        progressDialog.dismiss()
    }

    private fun toGalery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1,1)
            .start(this)
        startActivityForResult(intent, GalleryPick)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GalleryPick && resultCode == Activity.RESULT_OK && data != null ){
            val getData = data.data ?:return
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)

        }else{
            Toast.makeText(applicationContext, "Error!", Toast.LENGTH_SHORT).show()
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            result = CropImage.getActivityResult(data)
            resultUri = result!!.uri
            val imageUri = result!!.uri
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            profile_image.setImageBitmap(bitmap)
        }else{
            Toast.makeText(applicationContext, "Error!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveImageToFirebaseDatabase(){

        val uuid = UUID.randomUUID().toString()
        userImageRef = FirebaseStorage.getInstance().getReference("/Images/$uuid")

        userImageRef.putFile(resultUri)
            .addOnCompleteListener{
                if (it.isSuccessful){
                    Toast.makeText(applicationContext, "Image successfully downloaded to firebase", Toast.LENGTH_SHORT).show()
                    userImageRef.downloadUrl.addOnSuccessListener {
                        Log.d("SettingsActivity", it.toString())
                        FirebaseDatabase.getInstance().reference
                            .child("Users")
                            .child(currentUserId)
                            .child("image")
                            .setValue(it.toString())
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    Toast.makeText(applicationContext, "Image successfully downloaded to firebase", Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                                    progressDialog.dismiss()
                                }
                            }
                    }
                }else{
                    progressDialog.dismiss()
                    val message = it.exception.toString()
                    Toast.makeText(applicationContext, "xaay " +  message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUsersInfoToFirebaseDatabase() {
        val setUsername = user_name_edit_text.text.toString()
        val setStatus = status_edit_text.text.toString()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        if (setUsername.isEmpty() || setStatus.isEmpty() || result == null ) {
            Toast.makeText(applicationContext, "write username or status or image ", Toast.LENGTH_SHORT).show()
        } else {

            val profileMap = HashMap<String, String>()
            profileMap.put("uid", currentUserId)
            profileMap.put("name", setUsername)
            profileMap.put("status", setStatus)
            //profileMap.put("image", resultUri.toString())

            FirebaseDatabase.getInstance().reference
                .child("Users")
                .child(currentUserId)
                .setValue(profileMap)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                        Toast.makeText(
                            applicationContext,
                            "Username successfully created",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val errorString = it.exception.toString()
                        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
