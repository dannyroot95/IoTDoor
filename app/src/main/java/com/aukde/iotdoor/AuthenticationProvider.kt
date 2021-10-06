package com.aukde.iotdoor

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

class AuthenticationProvider {
    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var mDatabaseReference : DatabaseReference = FirebaseDatabase.getInstance().reference

    fun register(email: String, password: String): Task<AuthResult?> {
        return mAuth.createUserWithEmailAndPassword(email, password)
    }

    fun login(email: String, password: String): Task<AuthResult?> {
        return mAuth.signInWithEmailAndPassword(email, password)
    }

    fun registerHistory(historyModel: HistoryModel) : Task<Void>{
        val map: MutableMap<String, Any> = HashMap()
        map["fullname"] = historyModel.fullname
        map["date"] = historyModel.date
        return mDatabaseReference.child("history").push().setValue(map)
    }


    fun logout() {
        mAuth.signOut()
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