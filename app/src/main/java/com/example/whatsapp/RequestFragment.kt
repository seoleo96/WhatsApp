package com.example.whatsapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.contacts_view_row.view.*

/**
 * A simple [Fragment] subclass.
 */
class RequestFragment : Fragment() {
    lateinit var currentUserId : String

    lateinit var receiverUserId : String

    lateinit var chatRequestsRef : DatabaseReference
    lateinit var contactsRef : DatabaseReference

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_request, container, false)

        return view
    }

    override fun onStart() {
        super.onStart()



        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        contactsRef = FirebaseDatabase.getInstance().getReference("Contacts")
        chatRequestsRef = FirebaseDatabase.getInstance().getReference("chat requests")
        Log.d("CurrentUserId", currentUserId)

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycle_requestFr) ?: return




        val ref = FirebaseDatabase.getInstance().getReference("/chat requests/$currentUserId")

           ref.addValueEventListener(object : ValueEventListener{

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {


                    Log.d("RequestFragmentA", p0.toString())
                    Log.d("RequestFragmentA", p0.key.toString())
                    Log.d("RequestFragmentA", p0.value.toString())

                    p0.children.forEach {
                        Log.d("RequestFragment111", it.toString())
                        Log.d("RequestFragmentKey", it.key.toString())
                        Log.d("RequestFragment111", it.value.toString())
                        val requestSentUser = it.key.toString()

                        if (it.exists() && it.hasChild("requestType")){
                            Log.d("RequestFragment11111", it.child("requestType").value.toString())
                            val requestType = it.child("requestType").value.toString()

                            if (requestType.equals("received")){
                                adapter.clear()
                                FirebaseDatabase.getInstance().getReference("/Users/$requestSentUser")

                                    .addListenerForSingleValueEvent(object : ValueEventListener{

                                        override fun onCancelled(p0: DatabaseError) {

                                        }

                                        override fun onDataChange(p0: DataSnapshot) {

                                            Log.d("RequestFragmentReceived", p0.toString())
                                            val users = p0.getValue(Users::class.java) ?:return
                                            adapter.add(RequestItem(users))

                                            adapter.setOnItemClickListener { item, view ->
                                                val usersInfo = p0.getValue(Users::class.java) ?: return@setOnItemClickListener
                                                receiverUserId = usersInfo.uid
                                                val alertDialog = AlertDialog.Builder(view.context)
                                                alertDialog.setTitle("Do you want to communicate with ${usersInfo.name}")

                                                alertDialog.setPositiveButton("Accept", DialogInterface.OnClickListener { dialog, which ->
                                                    contactsRef.child(currentUserId).child(receiverUserId)
                                                        .child("Contacts").setValue("Saved")
                                                        .addOnCompleteListener {
                                                            if (it.isSuccessful){

                                                                contactsRef.child(receiverUserId).child(currentUserId)
                                                                    .child("Contacts").setValue("Saved")
                                                                    .addOnCompleteListener {
                                                                        if (it.isSuccessful){

                                                                            chatRequestsRef.child(currentUserId).child(receiverUserId)
                                                                                .removeValue()
                                                                                .addOnCompleteListener {
                                                                                    if (it.isSuccessful){

                                                                                        chatRequestsRef.child(receiverUserId).child(currentUserId)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener {
                                                                                              Toast.makeText(view.context, "You added ${usersInfo.name}", Toast.LENGTH_SHORT).show()

                                                                                                contactsRef.child(receiverUserId).child(currentUserId)
                                                                                                    .child("Contacts").setValue("Saved")
                                                                                                    .addOnCompleteListener {
                                                                                                        if (it.isSuccessful){

                                                                                                            contactsRef.child(currentUserId).child(receiverUserId)
                                                                                                                .child("Contacts").setValue("Saved")
                                                                                                                .addOnCompleteListener {
                                                                                                                    if (it.isSuccessful){
                                                                                                                        Toast.makeText(view.context, "You are friends with ${usersInfo.name}", Toast.LENGTH_SHORT).show()
                                                                                                                    }
                                                                                                                }
                                                                                                        }
                                                                                                    }
                                                                                            }
                                                                                    }
                                                                                }
                                                                        }
                                                                    }
                                                            }
                                                        }
                                                })

                                                alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                                                    chatRequestsRef.child(currentUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener {
                                                            if (it.isSuccessful){

                                                                chatRequestsRef.child(receiverUserId).child(currentUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener {
                                                                        Toast.makeText(view.context, "You removed ${usersInfo.name}", Toast.LENGTH_SHORT).show()


                                                                    }
                                                            }
                                                        }
                                                })

                                                alertDialog.show()
                                            }
                                        }


                                    })
                            }

                        }else{
                            Log.d("RequestFragment", "NOt EQUALS")
                        }

                    }
                }
            })
        recyclerView.adapter = adapter
    }

    class RequestItem(val users: Users) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.contacts_view_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.user_name_contactFr.text = users.name
            viewHolder.itemView.user_status_contactFr.text = users.status
            Picasso.get().load(users.image).into(viewHolder.itemView.profile_image_contactFr)
        }
    }

}
