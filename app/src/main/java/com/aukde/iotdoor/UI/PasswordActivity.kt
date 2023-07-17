package com.aukde.iotdoor.UI

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.aukde.iotdoor.*
import com.aukde.iotdoor.Models.HistoryModel
import com.aukde.iotdoor.Providers.AuthenticationProvider
import com.aukde.iotdoor.Providers.DataDeviceProvider
import com.aukde.iotdoor.R
import com.aukde.iotdoor.databinding.ActivityMainBinding
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PasswordActivity : BaseActivity() {

    private lateinit var  binding : ActivityMainBinding
    var mDatabase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth : AuthenticationProvider = AuthenticationProvider()
    private lateinit var preference : SharedPreferences
    var name : String = ""
    val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewButtons()

        DataDeviceProvider().getData(binding,this)

        preference = getSharedPreferences("fullname", MODE_PRIVATE)
        name = preference.getString("key","").toString()
        //checkingProximitySensor()

        binding.btnOk.setOnClickListener {
            if (binding.edtPassword.text.isNotEmpty()){
                showDialog("Abriendo puerta...")
                val password = binding.edtPassword.text.toString()
                val sharedPreferencesDevice = this.getSharedPreferences("cache", Context.MODE_PRIVATE)
                val id = sharedPreferencesDevice.getString("keyDevice", "")!!
                mDatabase.child("users").child(mAuth.getId()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChild("password")) {
                            val datapassword = snapshot.child("password").value.toString()
                            val state = snapshot.child("stateUser").value.toString()
                            if (state != "blocked"){
                                if (datapassword == password){
                                    val history =  HistoryModel(name,System.currentTimeMillis())
                                    mDatabase.child("devices").child(id).child("status").setValue("open")
                                    mAuth.registerHistory(history,this@PasswordActivity)
                                    binding.edtPassword.setText("")
                                    hideDialog()
                                    Toasty.success(this@PasswordActivity, "ABIERTO!", Toast.LENGTH_LONG).show()
                                }
                                else{
                                    hideDialog()
                                    Toasty.error(this@PasswordActivity, "Contraseña incorrecta!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                hideDialog()
                                Toasty.error(this@PasswordActivity, "USUARIO BLOQUEADO!", Toast.LENGTH_SHORT).show()
                            }

                        } else {
                            hideDialog()
                            Toasty.info(this@PasswordActivity, "No tiene clave asignada..", Toast.LENGTH_SHORT).show()
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        hideDialog()
                        Toasty.error(this@PasswordActivity,"ERROR",Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else{
                Toasty.info(this@PasswordActivity,"Digite la clave!",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCreateClave.setOnClickListener {
            startActivity(Intent(this, CreatePasswords::class.java))
        }
        binding.btnRecord.setOnClickListener {
            startActivity(Intent(this, Record::class.java))
        }
        binding.btnLogout.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Hey!")
            builder.setIcon(R.drawable.ic_logout)
            builder.setMessage("Estas seguro de cerrar sesión?")

            builder.setPositiveButton("SI") { dialog, which ->
                mAuth.logout(this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            builder.setNegativeButton("NO") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
        }

        binding.btnOpenLocal.setOnClickListener {
            showDialog("Abriendo puerta...")
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    openDoor()
                } catch (e: IOException) {
                    // Manejar el error de la solicitud HTTP
                    runOnUiThread {
                        hideDialog()
                        Toast.makeText(this@PasswordActivity,"Error",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.sw.isChecked = true
        binding.type.text = "Online"
        binding.sw.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.type.text = "Online"
                binding.typeOnline.visibility = View.VISIBLE
                binding.edtPassword.visibility = View.VISIBLE
                binding.typeOffline.visibility = View.GONE
            } else {
                binding.type.text = "Offline"
                binding.typeOffline.visibility = View.VISIBLE
                binding.typeOnline.visibility = View.GONE
                binding.edtPassword.visibility = View.GONE
            }
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


    private fun checkingProximitySensor(){
        mDatabase.child("distance").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val value = snapshot.value
                    val valueInt = Integer.parseInt(value.toString())
                    if(valueInt in 50..120){
                        Toast.makeText(this@PasswordActivity,"Estas Cerca",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@PasswordActivity,"OFF",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onBackPressed() {
    }

    private fun openDoor() {

        val sharedPreferences= this.getSharedPreferences("webServer", Context.MODE_PRIVATE)
        val ip = sharedPreferences.getString("keyWebServer", "")!!

        if(ip != "") {
            val url = "http://$ip/toggle" // Reemplaza con la IP del ESP32
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody()) // Cambia a .get() si estás usando una solicitud GET
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    runOnUiThread {
                        hideDialog()
                        Toasty.success(this@PasswordActivity, "Error!", Toast.LENGTH_LONG).show()
                    }
                }else{
                    runOnUiThread {
                        hideDialog()
                        Toasty.success(this@PasswordActivity, "ABIERTO!", Toast.LENGTH_LONG).show()
                    }
                }
                // Aquí puedes realizar cualquier procesamiento adicional con la respuesta del ESP32
            }
        }else{
            Toasty.error(this@PasswordActivity, "No existe IP local!", Toast.LENGTH_LONG).show()
        }
    }

}