package com.aukde.iotdoor.Providers

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.aukde.iotdoor.UI.PasswordActivity
import com.aukde.iotdoor.databinding.ActivityMainBinding
import com.google.firebase.database.*

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
                    val time = snapshot.child("time").value.toString()
                    val minuteInSeconds = 60
                    val currentTime = System.currentTimeMillis()/1000

                    binding.txtGetData.visibility = View.GONE

                    if (currentTime - time.toLong() < minuteInSeconds) {
                        binding.ivConnect.visibility = View.VISIBLE
                        binding.txtConnect.visibility = View.VISIBLE
                        binding.ivDisconnect.visibility = View.GONE
                        binding.txtDisconnect.visibility = View.GONE
                    }else{
                        binding.ivConnect.visibility = View.GONE
                        binding.txtConnect.visibility = View.GONE
                        binding.ivDisconnect.visibility = View.VISIBLE
                        binding.txtDisconnect.visibility = View.VISIBLE
                    }

                   // val sharedPreferencesWeb = activity.getSharedPreferences("cache", Context.MODE_PRIVATE)
                    val editorWeb = sharedPreferencesDevice.edit()

                    editorWeb.putString("keyWebServer",server)
                    editorWeb.apply()

                    binding.ipDevice.text = server
                    binding.time.text = time

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

    fun isConnect(binding: ActivityMainBinding,activity : PasswordActivity){
        val sharedPreferencesDevice = activity.getSharedPreferences("cache", Context.MODE_PRIVATE)
        val id = sharedPreferencesDevice.getString("keyDevice", "")!!

        mDatabase.child("devices").child(id).addValueEventListener(object : ValueEventListener{
            @SuppressLint("CommitPrefEdits")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){

                    val time = snapshot.child("time").value.toString()
                    val minuteInSeconds = 60
                    val currentTime = System.currentTimeMillis()/1000

                    binding.txtGetData.visibility = View.GONE

                    if (currentTime - time.toLong() < minuteInSeconds) {
                        binding.ivConnect.visibility = View.VISIBLE
                        binding.txtConnect.visibility = View.VISIBLE
                        binding.ivDisconnect.visibility = View.GONE
                        binding.txtDisconnect.visibility = View.GONE
                    }else{
                        binding.ivConnect.visibility = View.GONE
                        binding.txtConnect.visibility = View.GONE
                        binding.ivDisconnect.visibility = View.VISIBLE
                        binding.txtDisconnect.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    }
