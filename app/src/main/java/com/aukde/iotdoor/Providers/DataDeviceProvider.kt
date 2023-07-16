package com.aukde.iotdoor.Providers

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Toast
import com.aukde.iotdoor.PasswordActivity
import com.aukde.iotdoor.databinding.ActivityMainBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class DataDeviceProvider {

    var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getData(binding: ActivityMainBinding,activity : PasswordActivity){
        val sharedPreferencesDevice = activity.getSharedPreferences("cache", Context.MODE_PRIVATE)
        val id = sharedPreferencesDevice.getString("keyDevice", "")!!

        mDatabase.child("devices").child(id).addValueEventListener(object : ValueEventListener{
            @SuppressLint("CommitPrefEdits")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){

                    val door = snapshot.child("door").value.toString()
                    val server = snapshot.child("web_server").value.toString()

                   // val sharedPreferencesWeb = activity.getSharedPreferences("cache", Context.MODE_PRIVATE)
                    val editorWeb = sharedPreferencesDevice.edit()

                    editorWeb.putString("keyWebServer",server)
                    editorWeb.apply()

                    binding.ipDevice.text = server

                    if (door == "0"){
                        binding.isOpenDoor.text = "Cerrado"
                        binding.ivOpenDoor.visibility = View.VISIBLE
                        binding.ivCloseDoor.visibility = View.GONE
                    }else {
                        binding.isOpenDoor.text = "Abierto"
                        binding.ivCloseDoor.visibility = View.VISIBLE
                        binding.ivOpenDoor.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        }
    }
