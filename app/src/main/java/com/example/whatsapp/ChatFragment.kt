package com.example.whatsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_layout_row.view.*

/**
 * A simple [Fragment] subclass.
 */
class ChatFragment : Fragment() {
    lateinit var contactRef: DatabaseReference
    lateinit var currentUserId: String
    lateinit var state: String
    lateinit var status: String
    lateinit var userState: UserState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        contactRef = FirebaseDatabase.getInstance().getReference("/Contacts/$currentUserId")
        val view =  inflater.inflate(R.layout.fragment_chat, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()
        val recyclerview = view?.findViewById<RecyclerView>(R.id.recycler_chat_fragment) ?: return
        val adapter = GroupAdapter<GroupieViewHolder>()


        contactRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                adapter.clear()
                p0.children.forEach {
                    Log.d("ChatFragment", it.toString())
                    Log.d("ChatFragment", it.key.toString())
                    Log.d("ChatFragment", it.value.toString())

                    FirebaseDatabase.getInstance().getReference("/Users/${it.key.toString()}")
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {

                                if (p0.exists()){

                                    state = p0.child("userState").child("state").value.toString()
                                    val userState = p0.child("userState").getValue(UserState::class.java) ?:return
                                    adapter.notifyDataSetChanged()
                                    val users = p0.getValue(Users::class.java) ?:return

                                    //val adapterItem = adapter.groupCount

                                    adapter.add(ChatFragmentItem(users))
                                    adapter.notifyDataSetChanged()

                                }
                            }
                        })
                }

                adapter.setOnItemClickListener { item, view ->
                    val users = item as ChatFragmentItem
                    val intent = Intent(view.context, ChatActivity::class.java)
                    intent.putExtra("usersInfo", users.users)
                    startActivity(intent)
                }
                recyclerview.adapter = adapter
                recyclerview.addItemDecoration(DividerItemDecoration(view!!.context, DividerItemDecoration.VERTICAL))
            }
        })
    }

    class UserState(val currentData : String, val currentTime : String,val state : String){
        constructor() : this("", "", "")
    }



    class ChatFragmentItem(val users: Users) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.chat_layout_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            FirebaseDatabase.getInstance().getReference("/Users/${users.uid}/userState")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var userState = p0.getValue(UserState::class.java) ?: return
                        if (userState.state.equals("online")){
                            //viewHolder.itemView.onlineStatus.visibility = View.VISIBLE
                            viewHolder.itemView.user_status_for_chat.text = "online"
                        }else{
                            viewHolder.itemView.user_status_for_chat.text = "Last seen ${userState.currentData} ${userState.currentTime}"
                        }

                    }
                })
                viewHolder.itemView.user_name_for_chat.text = users.name

                Picasso.get().load(users.image).into(viewHolder.itemView.users_profile_image_for_chat)

        }
    }

}
