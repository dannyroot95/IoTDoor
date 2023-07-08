package com.aukde.iotdoor

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.aukde.iotdoor.databinding.ActivityLoginBinding
import com.aukde.iotdoor.databinding.ActivityMainBinding
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty

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
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){

                                val name = snapshot.child("fullname").value.toString()
                                val sharedPreferencesFullname = getSharedPreferences("fullname", Context.MODE_PRIVATE)
                                val editorFullname = sharedPreferencesFullname.edit()

                                editorFullname.putString("key",name)
                                editorFullname.apply()
                                hideDialog()
                                val intent = Intent(this@LoginActivity, PasswordActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }
                            else{
                                mAuth.logout()
                                hideDialog()
                                Toasty.error(this@LoginActivity, "Error!", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            mAuth.logout()
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
            startActivity(Intent(this,PasswordActivity::class.java))
        }
    }

    override fun onBackPressed() {
    }

}