package com.aukde.iotdoor

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

class UserProvider {

    var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
        .child("users")

    var mHistory: DatabaseReference = FirebaseDatabase.getInstance().reference
            .child("history")

    fun create(user: User): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["id"] = user.id
        map["fullname"] = user.fullname
        map["email"] = user.email
        map["stateUser"] = "enable"
        return mDatabase.child(user.id).setValue(map)
    }

    fun getHistoryUsers(activity : Record){
        mHistory.get().addOnSuccessListener { snapshot ->
            val historyList : ArrayList<HistoryModel> = ArrayList()
            for (i in snapshot.children){
                val data = i.getValue(HistoryModel::class.java)
                historyList.add(data!!)
            }
            activity.successHistory(historyList)
        }
    }

}