package com.aukde.iotdoor.Providers

import android.content.Context
import com.aukde.iotdoor.Models.HistoryModel
import com.aukde.iotdoor.UI.Record
import com.aukde.iotdoor.Models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList

class UserProvider {

    var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    var mHistory: DatabaseReference = FirebaseDatabase.getInstance().reference
    fun create(user: User): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["id"] = user.id
        map["fullname"] = user.fullname
        map["email"] = user.email
        map["device"] = ""
        map["stateUser"] = "enable"
        map["typeUser"] = ""
        return mDatabase.child("users").child(user.id).setValue(map)
    }

    fun updateDevice(device: String,id:String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["hasRoot"] = id
        return mDatabase.child("devices").child(device).updateChildren(map)
    }

    fun updateDeviceAndRoot(device: String,root:String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["device"] = device
        map["password"] = "1010"
        map["typeUser"] = root
        return mDatabase.child("users").child(AuthenticationProvider().getId()).updateChildren(map)
    }

    fun updateOnlyDevice(device: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["device"] = device
        return mDatabase.child("users").child(AuthenticationProvider().getId()).updateChildren(map)
    }

    fun getHistoryUsers(activity : Record, date : String){
        mHistory.child("history").get().addOnSuccessListener { snapshot ->
            val historyList : ArrayList<HistoryModel> = ArrayList()
            for (i in snapshot.children){
                val data = i.getValue(HistoryModel::class.java)
                historyList.add(data!!)
            }
            historyList.reverse()
            activity.successHistory(historyList)
        }
    }

}