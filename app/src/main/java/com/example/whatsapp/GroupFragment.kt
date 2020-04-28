package com.example.whatsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.groups_names.view.*

/**
 * A simple [Fragment] subclass.
 */
class GroupFragment : Fragment() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        val viewFragmentGroup : View =  inflater.inflate(R.layout.fragment_group, container, false)
        val recyclerView : RecyclerView = viewFragmentGroup.findViewById(R.id.group_recycler_view)

        readGroupNameFromFireBaseDatabase(recyclerView)


        return viewFragmentGroup
    }




    private fun readGroupNameFromFireBaseDatabase(recyclerView : RecyclerView) {

        FirebaseDatabase.getInstance().reference
            .child("Groups")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    adapter.clear()

                    Log.d("GroupFragment", p0.value.toString())
                    p0.children.forEach {
                        Log.d("GroupFragment", it.toString())
                        val keys = it.key.toString()
                        val values = it.value.toString()

                        Log.d("GroupFragment", keys)
                        Log.d("GroupFragment", "values= $values")

                        adapter.add(GroupNamesItem(keys))
                    }


                    adapter.setOnItemClickListener { item, view ->

                        val groupName = item as GroupNamesItem
                        val intent = Intent(view.context, GroupChatActivity::class.java)
                        intent.putExtra("groupName", groupName.string)
                        startActivity(intent)
                    }

                }
            })

        recyclerView.adapter = adapter
    }


    class GroupNamesItem(val string: String): Item<GroupieViewHolder>(){

        override fun getLayout(): Int {
            return R.layout.groups_names
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.groups_name_id.text = string
        }
    }





}

