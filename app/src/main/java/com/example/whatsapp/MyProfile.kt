package com.example.whatsapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_my_profile.*

class MyProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile2)
        retrieveUserInfo()
    }


    private fun retrieveUserInfo() {
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {

                    if ((p0.exists()) && p0.hasChild("name") && p0.hasChild("image")){
                        val retrieveUserName = p0.child("name").value.toString()
                        val retrieveUserStatus = p0.child("status").value.toString()
                        val retrieveUserImage = p0.child("image").value.toString()
                        user_name_myProfile.text =retrieveUserName
                        status_myProfile.text = retrieveUserStatus
                        Picasso.get().load(retrieveUserImage).into(profile_image_my_profile);

                    }else if((p0.exists()) && p0.hasChild("name")){
                        val retrieveUserName = p0.child("name").value.toString()
                        val retrieveUserStatus = p0.child("status").value.toString()
                        user_name_myProfile.text =retrieveUserName
                        status_myProfile.text = retrieveUserStatus
                    }else{
                        Toast.makeText(applicationContext, "please  update username or status", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}
