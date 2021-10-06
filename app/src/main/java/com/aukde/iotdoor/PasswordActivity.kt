package com.aukde.iotdoor

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.aukde.iotdoor.databinding.ActivityMainBinding
import com.google.firebase.database.*

class PasswordActivity : BaseActivity() {

    private lateinit var  binding : ActivityMainBinding
    var mDatabase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth : AuthenticationProvider = AuthenticationProvider()
    private lateinit var preference : SharedPreferences
    var name : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewButtons()

        preference = getSharedPreferences("fullname", MODE_PRIVATE)
        name = preference.getString("key","").toString()

        binding.btnOk.setOnClickListener {
            if (binding.edtPassword.text.isNotEmpty()){
                showDialog("Abriendo puerta...")
                val password = binding.edtPassword.text.toString()
                mDatabase.child("users").child(mAuth.getId()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChild("password")) {
                            val datapassword = snapshot.child("password").value.toString()
                            val state = snapshot.child("stateUser").value.toString()
                            if (state != "blocked"){
                                if (datapassword == password){
                                    val history =  HistoryModel(name,System.currentTimeMillis())
                                    mDatabase.child("status").setValue("open")
                                    mAuth.registerHistory(history)
                                    binding.edtPassword.setText("")
                                    hideDialog()
                                    Toast.makeText(this@PasswordActivity, "ABIERTO!", Toast.LENGTH_LONG).show()
                                }
                                else{
                                    hideDialog()
                                    Toast.makeText(this@PasswordActivity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                hideDialog()
                                Toast.makeText(this@PasswordActivity, "USUARIO BLOQUEADO!", Toast.LENGTH_SHORT).show()
                            }

                        } else {
                            hideDialog()
                            Toast.makeText(this@PasswordActivity, "No tiene clave asignada", Toast.LENGTH_SHORT).show()
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        hideDialog()
                        Toast.makeText(this@PasswordActivity,"error",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else{
                Toast.makeText(this@PasswordActivity,"digite la clave!",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCreateClave.setOnClickListener {
            startActivity(Intent(this,CreatePasswords::class.java))
        }
        binding.btnRecord.setOnClickListener {
            startActivity(Intent(this,Record::class.java))
        }
        binding.btnLogout.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Hey!")
            builder.setIcon(R.drawable.ic_logout)
            builder.setMessage("Estas seguro de cerrar sesión?")

            builder.setPositiveButton("SI") { dialog, which ->
                mAuth.logout()
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }

            builder.setNegativeButton("NO") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
        }
    }



    @SuppressLint("SetTextI18n")
    private fun viewButtons(){
        binding.btn0.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"0")
        }
        binding.btn1.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"1")
        }
        binding.btn2.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"2")
        }
        binding.btn3.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"3")
        }
        binding.btn4.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"4")
        }
        binding.btn5.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"5")
        }
        binding.btn6.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"6")
        }
        binding.btn7.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"7")
        }
        binding.btn8.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"8")
        }
        binding.btn9.setOnClickListener {
            val temp = binding.edtPassword.text.toString()
            binding.edtPassword.setText(temp+"9")
        }
        binding.btnDelete.setOnClickListener {
            binding.edtPassword.setText("")
        }
    }


    override fun onBackPressed() {
    }

}