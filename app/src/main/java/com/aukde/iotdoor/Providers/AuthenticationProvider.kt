package com.aukde.iotdoor.Providers

import android.content.Context
import com.aukde.iotdoor.Models.HistoryModel
import com.aukde.iotdoor.UI.PasswordActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.HashMap

class AuthenticationProvider {
    var mAuth: FirebaseAuth = Firebase.auth
    var mDatabaseReference : DatabaseReference = FirebaseDatabase.getInstance().reference

    fun register(email: String, password: String): Task<AuthResult?> {
        return mAuth.createUserWithEmailAndPassword(email, password)
    }

    fun login(email: String, password: String): Task<AuthResult?> {
        return mAuth.signInWithEmailAndPassword(email, password)
    }

    fun registerHistory(historyModel: HistoryModel, activity: PasswordActivity) : Task<Void>{
        val sharedPreferencesDevice = activity.getSharedPreferences("cache", Context.MODE_PRIVATE)
        val id = sharedPreferencesDevice.getString("keyDevice", "")!!
        val map: MutableMap<String, Any> = HashMap()
        map["fullname"] = historyModel.fullname
        map["date"] = historyModel.date
        return mDatabaseReference.child("devices").child(id).child("history").push().setValue(map)
    }


    fun logout(context: Context) {
        mAuth.signOut()
        val sharedPreferences = context.getSharedPreferences("cache", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun getId(): String {
        return mAuth.currentUser!!.uid
    }

    fun existSession(): Boolean {
        var exist = false
        if (mAuth.currentUser != null) {
            exist = true
        }
        return exist
    }
}