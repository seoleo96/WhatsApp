package com.example.whatsapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()

    lateinit var currentUser : Users
    lateinit var uri : Uri




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        recycler_for_messages.adapter = adapter

        getCurrentUserId()

        val username = intent.getParcelableExtra<Users>("usersInfo") ?: return
        supportActionBar?.title = username.name

        FirebaseDatabase.getInstance().getReference("/Users/${username.uid}/userState")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var userState = p0.getValue(ChatFragment.UserState::class.java) ?: return
                    if (userState.state.equals("online")){
                        supportActionBar?.subtitle = "online"
                    }else{
                        supportActionBar?.subtitle = "Last seen ${userState.currentData} ${userState.currentTime}"
                    }

                }
            })

        listenForMesssages()

        send_message_button_fro_chat.setOnClickListener {
            if (newSendMessage.text.isEmpty()){
                Toast.makeText(applicationContext, "write message", Toast.LENGTH_SHORT).show()
            }else{
                performSendMessage()
                recycler_for_messages.smoothScrollToPosition(adapter.itemCount)
                newSendMessage.text.clear()
            }
        }

        data_button.setOnClickListener {
            sendImage()

        }

    }

    private fun sendImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, ""), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data!= null ){
            uri = data.data ?:return
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val uuid = UUID.randomUUID().toString()
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Sending image")
            progressDialog.show()
            val userImageRef = FirebaseStorage.getInstance().getReference("/Images/$uuid")
            userImageRef.putFile(uri)

                .addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "Image successfully downloaded", Toast.LENGTH_SHORT).show()
                        userImageRef.downloadUrl.addOnCompleteListener {
                             val imageUri = it.result.toString()
                             Log.d("ChatActivityImage", it.toString())
                             Log.d("ChatActivityImage", imageUri)

                            val username = intent.getParcelableExtra<Users>("usersInfo") ?: return@addOnCompleteListener
                            val calForData = Calendar.getInstance()
                            val currentDataFormat = SimpleDateFormat("MMM dd, yyyy")


                            val calForTime = Calendar.getInstance()
                            val currentTimeFormat = SimpleDateFormat("hh:mm a")

                            val currentData = currentDataFormat.format(calForData.time)
                            val currentTime = currentTimeFormat.format(calForTime.time)
                            val fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                            val toId = username.uid

                            val text = imageUri
                            val type = "image"
                            val fromReference = FirebaseDatabase.getInstance()
                                     .getReference("/Messages/$fromId/$toId").push()
                            val toReference = FirebaseDatabase.getInstance()
                                     .getReference("/Messages/$toId/$fromId").push()
                            val messages = Messages(
                                     fromReference.key!!,
                                     text,
                                     toId,
                                     fromId,
                                     currentTime,
                                     currentData,
                                     type
                            )

                            fromReference.setValue(messages)
                                .addOnCompleteListener {
                                    progressDialog.dismiss()
                                }
                            toReference.setValue(messages)
                                .addOnCompleteListener {
                                    progressDialog.dismiss()
                                }

                        }
                    }
                }
        }else{
            Toast.makeText(applicationContext, "Error!", Toast.LENGTH_SHORT).show()
        }
    }






    private fun getCurrentUserId(){
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    currentUser = p0.getValue(Users::class.java) ?: return

                }
            })

    }

    private fun listenForMesssages() {

        val username = intent.getParcelableExtra<Users>("usersInfo") ?: return
        val fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val toId = username.uid
        val refMessage = FirebaseDatabase.getInstance().getReference("/Messages/$fromId/$toId")


       refMessage.addChildEventListener(object : ChildEventListener{

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {


            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                val messages = p0.getValue(Messages::class.java) ?: return
                Log.d("ChatActivity", messages.text)


                    if (messages.fromId == FirebaseAuth.getInstance().currentUser?.uid.toString()){
                        adapter.add(ChatFromItem(messages, currentUser))
                        recycler_for_messages.smoothScrollToPosition(adapter.itemCount)
                    }else{
                        val usersInfo = intent.getParcelableExtra<Users>("usersInfo") ?: return
                        adapter.add(ChatToItem(messages, usersInfo))
                        recycler_for_messages.smoothScrollToPosition(adapter.itemCount)
                    }

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })

    }

    class Messages(val id : String, val text : String, val toId : String, val fromId : String, val time : String,
                   val data : String, val type : String){
        constructor() : this("", "", "", "", "", "", "")
    }

    private fun performSendMessage() {

        val username = intent.getParcelableExtra<Users>("usersInfo") ?: return
        val calForData = Calendar.getInstance()
        val currentDataFormat = SimpleDateFormat("MMM dd, yyyy")


        val calForTime = Calendar.getInstance()
        val currentTimeFormat = SimpleDateFormat("hh:mm a")

        val currentData = currentDataFormat.format(calForData.time)
        val currentTime = currentTimeFormat.format(calForTime.time)
        val fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val toId = username.uid

        val text = newSendMessage.text.toString()
        val type = "text"
        val fromReference = FirebaseDatabase.getInstance().getReference("/Messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/Messages/$toId/$fromId").push()
        val messages = Messages(fromReference.key!!, text, toId, fromId, currentTime,currentData, type )

        fromReference.setValue(messages)
        toReference.setValue(messages)
    }


    class ChatFromItem(val messages : Messages, val users: Users) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return  R.layout.chat_from_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            if (messages.type.equals("text")){
                viewHolder.itemView.messages_chat_from.visibility = View.VISIBLE
                val calendar = Calendar.getInstance()
                val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy")
                val currentData = simpleDateFormat.format(calendar.time)
                viewHolder.itemView.messages_chat_from.text = "${messages.text}"
                if (currentData != messages.data){
                    viewHolder.itemView.time_and_data.text =  messages.time + " " + messages.data
                }else{
                    viewHolder.itemView.time_and_data.text =  messages.time
                }
            }else{
                val calendar = Calendar.getInstance()
                val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy")
                val currentData = simpleDateFormat.format(calendar.time)
                if (currentData != messages.data){
                    viewHolder.itemView.time_and_data.text =  messages.time + " " + messages.data
                }else{
                    viewHolder.itemView.time_and_data.text =  messages.time
                }
                Picasso.get().load(messages.text).into(viewHolder.itemView.image_view)
                viewHolder.itemView.image_view.visibility = View.VISIBLE
            }


        }
    }

    class ChatToItem(val messages : Messages, val users: Users) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return  R.layout.chat_to_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            if (messages.type.equals("text")){
                Picasso.get().load(users.image).into(viewHolder.itemView.profile_image_new_message_chatTo)
                viewHolder.itemView.messages_chat_to.visibility = View.VISIBLE
                val calendar = Calendar.getInstance()
                val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy")
                val currentData = simpleDateFormat.format(calendar.time)
                viewHolder.itemView.messages_chat_to.text = "${messages.text}"
                if (currentData != messages.data){
                    viewHolder.itemView.data_and_time_to.text =  messages.time + " " + messages.data
                }else{
                    viewHolder.itemView.data_and_time_to.text =  messages.time
                }
            }else{
                val calendar = Calendar.getInstance()
                val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy")
                val currentData = simpleDateFormat.format(calendar.time)
                if (currentData != messages.data){
                    viewHolder.itemView.data_and_time_to.text =  messages.time + " " + messages.data
                }else{
                    viewHolder.itemView.data_and_time_to.text =  messages.time
                }

               // Picasso.get().load(users.image).into(viewHolder.itemView.profile_image_new_message_chatTo)
                //Picasso.get().load(messages.text).into(viewHolder.itemView.image_view_to)
                viewHolder.itemView.image_view_to.visibility = View.VISIBLE
            }

        }
    }


}


