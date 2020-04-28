package com.example.whatsapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_example.*
import kotlinx.android.synthetic.main.example_row.view.*

class ExampleActivity : AppCompatActivity() {

    private var messageKey : String? = null

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        messageKey = FirebaseDatabase.getInstance().reference.child("Example").push().key!!


        readFromDataBAse()

        button1.setOnClickListener {
            saveToFirebase()
            edit1.setText("")
        }
    }
    private fun readFromDataBAse() {
        FirebaseDatabase.getInstance().reference
            .child("Example")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    adapter.clear()
                    p0.children.forEach {

                        Log.d("ExampleActivity", it.toString())
                        Log.d("ExampleActivity1", it.key.toString())

                        val keys = it.key.toString()
                        adapter.add(ForExampleItem(keys))
                    }
                }
            })


        recaycle_example.adapter = adapter
    }

     class ForExampleItem(val keys : String): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.example_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            viewHolder.itemView.textview_example_for_recycle.text = keys

        }
    }

    private fun saveToFirebase(){

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val str = edit1.text.toString()



        if (str.isEmpty()){
            return
        }else {

            val hashmap = HashMap<String, String>()
            hashmap.put(currentUserId,str)

            FirebaseDatabase.getInstance().reference
                .child("Example")
                .child(str)
                .child(messageKey!!)
                .setValue(hashmap)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "Created", Toast.LENGTH_SHORT)
                            .show()
                    }else{
                        Toast.makeText(applicationContext, "NotCreated", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

        }
    }

}
