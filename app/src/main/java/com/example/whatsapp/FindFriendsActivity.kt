package com.example.whatsapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_find_friends.*
import kotlinx.android.synthetic.main.users_layout_row.view.*

class FindFriendsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        supportActionBar?.title = "Friends"
    }

    override fun onStart() {
        super.onStart()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        val adapter = GroupAdapter<GroupieViewHolder>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                Log.d("FindFriendActivity", p0.value.toString())
                p0.children.forEach {

                    Log.d("FindFriendActivity", it.value.toString())
                    val users = it.getValue(Users::class.java) ?:return
                    adapter.add(UsersInfoItem(users, this@FindFriendsActivity))
                }

                adapter.setOnItemClickListener { item, view ->
                    val visitUserId = item as UsersInfoItem
                    val intent = Intent(view.context, UsersInfo::class.java)
                    intent.putExtra("visitUserId", visitUserId.usersInfo)
                    startActivity(intent)
                }
            }
        })
        find_friends_recycler_view.adapter = adapter
        find_friends_recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    class UsersInfoItem(val usersInfo: Users, val context:Context): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.users_layout_row
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (FirebaseAuth.getInstance().currentUser?.uid.toString() == usersInfo.uid ){
                 viewHolder.itemView.user_you.visibility = View.VISIBLE
             }
            if (usersInfo.name.isEmpty()){
                viewHolder.itemView.user_name_find_friends.text = "null"
            }else{
                viewHolder.itemView.user_name_find_friends.text = usersInfo.name
            }
            if (usersInfo.status.isEmpty()){
                viewHolder.itemView.user_status.text = "null"
            }else{
                viewHolder.itemView.user_status.text = usersInfo.status
            }

            if (usersInfo.image.isEmpty()){
                viewHolder.itemView.users_profile_image.setImageResource(R.drawable.profil)
            }else{
                Picasso.get().load(usersInfo.image).into(viewHolder.itemView.users_profile_image)
            }
        }
    }
}




