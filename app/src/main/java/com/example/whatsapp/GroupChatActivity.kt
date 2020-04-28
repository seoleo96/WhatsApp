package com.example.whatsapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_group_chat_two.*
import kotlinx.android.synthetic.main.new_message_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class GroupChatActivity : AppCompatActivity() {
    private var currentUserName : String? = null
    private var currentGroupNameRef : DatabaseReference? = null
    private var groupMessageKeyRef : DatabaseReference? = null
    val adapter = GroupAdapter<GroupieViewHolder>()
    private var currentGroupName :String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat_two)



         currentGroupName = intent.getStringExtra("groupName")

        supportActionBar?.title = currentGroupName.toString()

        currentGroupNameRef = FirebaseDatabase.getInstance().reference.child("Groups").child(currentGroupName.toString())



        getUserInfo()
        send_message_id.setOnClickListener {
            sendMessageInfoToDataBase()
            messages_for_group.text.clear()
            recaycle_for_group_chat.scrollToPosition(adapter.itemCount -1)
        }

    }

    override fun onStart() {
        super.onStart()


        val groupName = currentGroupName.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/Groups/$groupName")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d("GroupChatActivityChild", p0.value.toString())
                Log.d("GroupChatActivityChild", p0.key.toString())
                val messagesInfo = p0.getValue(MessagesInfo::class.java) ?:return
                adapter.add(DisplayMessagesItem(messagesInfo))

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                Log.d("GroupChatActivityAdd", p0.value.toString())
                Log.d("GroupChatActivityAdd", p0.key.toString())

                val messagesInfo = p0.getValue(MessagesInfo::class.java) ?:return
                adapter.add(DisplayMessagesItem(messagesInfo))

            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })

            recaycle_for_group_chat.adapter = adapter



    }




    class DisplayMessagesItem( val messagesInfo: MessagesInfo) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.new_message_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.messages_of_group.text = messagesInfo.message
        }
    }

    private fun sendMessageInfoToDataBase() {

        val message = messages_for_group.text.toString()
        val messagekey = currentGroupNameRef?.push()?.key
        if (message.isEmpty()){
            Toast.makeText(applicationContext, "please write message...", Toast.LENGTH_SHORT).show()
        }else{
            val calForData = Calendar.getInstance()
            val currentDataFormat = SimpleDateFormat("MMM dd, yyyy")
            val currentData = currentDataFormat.format(calForData.time)

            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm a")
            val currentTime = currentTimeFormat.format(calForTime.time)

            val hashMap = HashMap<String, Any>()
            currentGroupNameRef?.updateChildren(hashMap)

            groupMessageKeyRef = currentGroupNameRef?.child(messagekey!!)

            val messageInfo = HashMap<String, Any>()
            messageInfo.put("name", currentUserName!!)
            messageInfo.put("message", message)
            messageInfo.put("data", currentData)
            messageInfo.put("time", currentTime)
            groupMessageKeyRef?.updateChildren(messageInfo)

            recaycle_for_group_chat.scrollToPosition(adapter.itemCount -1)


        }
    }

    private fun getUserInfo() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(currentUserId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        currentUserName = p0.child("name").value.toString()
                    }

                }
                override fun onCancelled(p0: DatabaseError) {

                }

            })
    }
}
