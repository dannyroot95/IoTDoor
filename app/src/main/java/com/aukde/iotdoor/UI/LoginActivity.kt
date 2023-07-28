package com.aukde.iotdoor.UI

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.aukde.iotdoor.BaseActivity
import com.aukde.iotdoor.Providers.AuthenticationProvider
import com.aukde.iotdoor.databinding.ActivityLoginBinding
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty
import java.util.HashMap

class LoginActivity : BaseActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var mAuth : AuthenticationProvider
    private var mDatabase : DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = AuthenticationProvider()
        binding.tvRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnLogin.setOnClickListener {
            login()
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecoveryPasswordActivity::class.java))
        }
    }

    private fun login() {
        val email : String = binding.edtEmail.text.toString()
        val password : String =  binding.edtPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()){
            showDialog("Iniciando Sesión...")
            mAuth.login(email,password).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val id = task.result!!.user!!.uid
                    mDatabase.child("users").child(id).addListenerForSingleValueEvent(object : ValueEventListener{
                        @SuppressLint("CommitPrefEdits")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){

                                val name = snapshot.child("fullname").value.toString()
                                val device = snapshot.child("device").value.toString()
                                val type = snapshot.child("typeUser").value.toString()

                                if(type == ""){
                                    val map: MutableMap<String, Any> = HashMap()
                                    map["typeUser"] = "client"
                                    mDatabase.child("users").child(AuthenticationProvider().getId()).updateChildren(map)

                                    if(device != ""){
                                        val sharedPreferences= getSharedPreferences("cache", Context.MODE_PRIVATE)
                                        val editorFullname = sharedPreferences.edit()
                                        val editorDevice = sharedPreferences.edit()
                                        val editorTypeUser = sharedPreferences.edit()

                                        editorFullname.putString("key",name)
                                        editorFullname.apply()

                                        editorDevice.putString("keyDevice",device)
                                        editorDevice.apply()

                                        editorTypeUser.putString("keyTypeUser",type)
                                        editorTypeUser.apply()

                                        hideDialog()
                                        val intent = Intent(this@LoginActivity, PasswordActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                    }else{
                                        val intent = Intent(this@LoginActivity, SyncDeviceActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                    }

                                }else{
                                    if(device != ""){
                                        val sharedPreferences= getSharedPreferences("cache", Context.MODE_PRIVATE)
                                        val editorFullname = sharedPreferences.edit()
                                        val editorDevice = sharedPreferences.edit()
                                        val editorTypeUser = sharedPreferences.edit()

                                        editorFullname.putString("key",name)
                                        editorFullname.apply()

                                        editorDevice.putString("keyDevice",device)
                                        editorDevice.apply()

                                        editorTypeUser.putString("keyTypeUser",type)
                                        editorTypeUser.apply()

                                        hideDialog()
                                        val intent = Intent(this@LoginActivity, PasswordActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                    }else{
                                        val intent = Intent(this@LoginActivity, SyncDeviceActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                    }
                                }



                            }
                            else{
                                mAuth.logout(this@LoginActivity)
                                hideDialog()
                                Toasty.error(this@LoginActivity, "Error!", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            mAuth.logout(this@LoginActivity)
                            hideDialog()
                            Toasty.error(this@LoginActivity, "Error!", Toast.LENGTH_LONG).show()
                        }
                    })}
                else{
                    hideDialog()
                    Toasty.error(this, "Error!, revise sus credenciales", Toast.LENGTH_LONG).show()
                }
            }
        }else{
            Toasty.warning(this, "Complete los campos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.existSession()){
            val sharedPreferencesDevice = this.getSharedPreferences("cache", Context.MODE_PRIVATE)
            val id = sharedPreferencesDevice.getString("keyDevice", "")!!
            if(id != ""){
                startActivity(Intent(this, PasswordActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this,SyncDeviceActivity::class.java))
                finish()
            }
        }
    }

    override fun onBackPressed() {
    }

}