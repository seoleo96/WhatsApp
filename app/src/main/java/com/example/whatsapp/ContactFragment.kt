package com.example.whatsapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.users_layout_row.view.*


class ContactFragment : Fragment() {

    lateinit var contactRef: DatabaseReference
    lateinit var currentUserId: String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
         val view =  inflater.inflate(R.layout.fragment_contact, container, false)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        Log.d("ContactFragment", currentUserId)
        contactRef = FirebaseDatabase.getInstance().getReference("/Contacts/$currentUserId")


        return view
    }

    override fun onStart() {
        super.onStart()
        contactRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                p0.children.forEach {
                    Log.d("ContactFragment", it.toString())
                    Log.d("ContactFragment", it.key.toString())
                    Log.d("ContactFragment", it.value.toString())

                    //to get Username, image, status

                    adapter.add(ContactsItem(it.key.toString()))

                }
                recycler_contact_fragmen.adapter = adapter
                recycler_contact_fragmen.addItemDecoration(DividerItemDecoration(view?.context, DividerItemDecoration.VERTICAL))
            }

        })
    }

    class ContactsItem(val usersId : String) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.users_layout_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            FirebaseDatabase.getInstance().getReference("Users")
                .child(usersId)
                .addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val usersState = p0.child("userState").getValue(ChatFragment.UserState::class.java) ?: return
                            val userName = p0.child("name").value.toString()
                            val userStatus = p0.child("status").value.toString()
                            val userImage = p0.child("image").value.toString()
                            viewHolder.itemView.user_name_find_friends.text = userName
                            viewHolder.itemView.user_status.text = userStatus
                            if (usersState.state.equals("online")){
                                viewHolder.itemView.user_online_status_image.visibility = View.VISIBLE
                            }
                            Picasso.get().load(userImage).into(viewHolder.itemView.users_profile_image)
                        }

                    }
                })
        }
    }

}
