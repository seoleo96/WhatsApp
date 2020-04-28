package com.example.whatsapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
        lateinit var currentUserId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        viewPagerFragments()

    }

    private fun viewPagerFragments(){
        val fragmentPagerAdapter = TabAccessorAdapter(supportFragmentManager)
        viewpager.adapter = fragmentPagerAdapter
        tabs.setupWithViewPager(viewpager)
    }

    override fun onStart() {
        super.onStart()
        if (currentUserId == null){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }else{
            updateUserStatus("online")
            verifyUserExistence()
        }
    }

    override fun onStop() {
        super.onStop()
        if (currentUserId != null){
            updateUserStatus("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentUserId != null){
            updateUserStatus("offline")
        }
    }



    private fun verifyUserExistence() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(currentUserId)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child("name").exists()){
                        Toast.makeText(applicationContext, "Welcome", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(applicationContext, "name or image or status in correct", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, UsersInfoActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }

            })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean{
        menuInflater.inflate(R.menu.menu_m_a, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val mauth = FirebaseAuth.getInstance()
        // Handle presses on the action bar menu items
        when (item.itemId) {

            R.id.log_out_id -> {
                mauth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }

            R.id.find_friends_id -> {
                val intent = Intent(this, FindFriendsActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.create_group_id -> {
                requestNewGroup()
                return true
            }

            R.id.example -> {
                val intent = Intent(this, ExampleActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.profile_image_menu -> {
                val intent = Intent(this, UsersInfoActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.my_profile_activity -> {
                val intent = Intent(this, MyProfile::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestNewGroup() {
        val alertDialog = AlertDialog.Builder(this,R.style.AlertDialog_AppCompat_Light_)
        alertDialog.setTitle("Enter group name ")
        val editText = EditText(this)
        editText.hint = "Group name"
        alertDialog.setView(editText)

        alertDialog.setPositiveButton("Create", DialogInterface.OnClickListener { dialog, which ->
            val groupName = editText.text.toString()

            if (groupName.isEmpty()){
                Toast.makeText(applicationContext, "Please write group name", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }else{
                createNewGroup(groupName)
            }
        })

        alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        alertDialog.show()
    }

    private fun createNewGroup(groupName : String) {
        FirebaseDatabase.getInstance().reference
            .child("Groups")
            .child(groupName)
            .setValue("")
            .addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(applicationContext, "$groupName group is created successfully", Toast.LENGTH_SHORT).show()

                }else{

                    Toast.makeText(applicationContext, "$groupName group is not created", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun updateUserStatus(state : String){
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy")
        val currentData = simpleDateFormat.format(calendar.time)
        val simpleTimeFormat = SimpleDateFormat("hh:mm a")
        val currentTime = simpleTimeFormat.format(calendar.time)

        val usersStatusMap = HashMap<String, Any>()
        usersStatusMap.put("currentTime", currentTime)
        usersStatusMap.put("currentData", currentData)
        usersStatusMap.put("state", state)

        FirebaseDatabase.getInstance().getReference("/Users/$currentUserId/userState")
            .setValue(usersStatusMap)


    }

}

