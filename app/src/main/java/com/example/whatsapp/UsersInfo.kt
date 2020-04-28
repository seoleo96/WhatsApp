package com.example.whatsapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_my_profile.*

class UsersInfo : AppCompatActivity() {

    lateinit var usersId : String
    lateinit var receiverUserId : String
    lateinit var senderUserId : String
    lateinit var currentState : String
    lateinit var chatRequestsRef : DatabaseReference
    lateinit var contactsRef : DatabaseReference

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

       // val visitUserId = intent.getStringExtra("visitUserId") ?:""
        val usersInfo = intent.getParcelableExtra<Users>("visitUserId")
        Toast.makeText(applicationContext, usersInfo.name, Toast.LENGTH_SHORT).show()
        supportActionBar?.title = usersInfo.name

        usersId = usersInfo.uid
        receiverUserId = usersInfo.uid
        senderUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        currentState = "new"
        chatRequestsRef = FirebaseDatabase.getInstance().getReference("chat requests")
        contactsRef = FirebaseDatabase.getInstance().getReference("Contacts")


        retrieveUserInfo()
    }


    private fun retrieveUserInfo() {
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(usersId)
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

                        manageChatRequests()

                    }else if((p0.exists()) && p0.hasChild("name")){
                        val retrieveUserName = p0.child("name").value.toString()
                        val retrieveUserStatus = p0.child("status").value.toString()
                        user_name_myProfile.text =retrieveUserName
                        status_myProfile.text = retrieveUserStatus
                        manageChatRequests()
                    }else{
                        Toast.makeText(applicationContext, "please  update username or status", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun manageChatRequests() {
        chatRequestsRef.child(senderUserId)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild(receiverUserId)){

                        val requestType = p0.child(receiverUserId).child("requestType").value.toString()

                        if (p0.equals("sent")){
                            currentState = "request_sent"
                            send_message_id_button.text = "Cancel chat request"
                        }else if (requestType.equals("received")){
                            currentState = "request_received"
                            send_message_id_button.text = "Accept chat request"
                            cancel_chat_request.visibility = View.VISIBLE
                            cancel_chat_request.isEnabled = true

                            cancel_chat_request.setOnClickListener {
                                cancelChatRequest()
                            }
                        }
                    }else{
                        contactsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.hasChild(receiverUserId)){
                                        currentState = "friends"
                                        send_message_id_button.text = "Remove this contact"
                                    }
                                }
                            })
                    }
                }
            })


        if(!senderUserId.equals(receiverUserId)){
            send_message_id_button.setOnClickListener {
                send_message_id_button.isEnabled = false
                if (currentState.equals("new")){
                    sendChatRequests()
                }
                if (currentState.equals("request_sent")){
                    cancelChatRequest()
                }
                if (currentState.equals("request_received")){
                    acceptChatRequest()
                }
                if (currentState.equals("friends")){
                    removeSpecificContact()
                }
            }
        }else{
            send_message_id_button.visibility = View.INVISIBLE
        }
    }

    private fun removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    contactsRef.child(receiverUserId).child(senderUserId)
                        .removeValue()
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                send_message_id_button.isEnabled = true
                                currentState = "new"
                                send_message_id_button.text = "Send message"

                                cancel_chat_request.visibility = View.INVISIBLE
                                cancel_chat_request.isEnabled = false
                            }
                        }
                }
            }
    }

    private fun acceptChatRequest(){

        contactsRef.child(senderUserId).child(receiverUserId)
            .child("Contacts").setValue("Saved")
            .addOnCompleteListener {
                if (it.isSuccessful){

                    contactsRef.child(receiverUserId).child(senderUserId)
                        .child("Contacts").setValue("Saved")
                        .addOnCompleteListener {
                            if (it.isSuccessful){

                                chatRequestsRef.child(senderUserId).child(receiverUserId)
                                    .removeValue()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful){

                                            chatRequestsRef.child(receiverUserId).child(senderUserId)
                                                .removeValue()
                                                .addOnCompleteListener {
                                                    send_message_id_button.isEnabled = true
                                                    currentState = "friends"
                                                    send_message_id_button.text = "Remove this contact"

                                                    cancel_chat_request.visibility = View.INVISIBLE
                                                    cancel_chat_request.isEnabled = false
                                                }
                                        }
                                    }
                            }
                        }
                }
            }
    }

    private fun cancelChatRequest() {
        chatRequestsRef.child(senderUserId).child(receiverUserId)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    chatRequestsRef.child(receiverUserId).child(senderUserId)
                        .removeValue()
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                send_message_id_button.isEnabled = true
                                currentState = "new"
                                send_message_id_button.text = "Send message"

                                cancel_chat_request.visibility = View.INVISIBLE
                                cancel_chat_request.isEnabled = false
                            }
                        }
                }
            }
    }

    private fun sendChatRequests(){
        chatRequestsRef.child(senderUserId).child(receiverUserId)
            .child("requestType").setValue("sent")
            .addOnCompleteListener {
                if (it.isSuccessful){
                    chatRequestsRef.child(receiverUserId).child(senderUserId)
                        .child("requestType").setValue("received")
                        .addOnCompleteListener {
                            send_message_id_button.isEnabled = true
                            currentState = "request_sent"
                            send_message_id_button.text = "Cancel chat request"
                        }
                }
            }
    }
}


