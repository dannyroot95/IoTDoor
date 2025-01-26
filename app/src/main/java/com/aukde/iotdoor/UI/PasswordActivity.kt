package com.aukde.iotdoor.UI

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.aukde.iotdoor.*
import com.aukde.iotdoor.Models.HistoryModel
import com.aukde.iotdoor.Providers.AuthenticationProvider
import com.aukde.iotdoor.Providers.DataDeviceProvider
import com.aukde.iotdoor.R
import com.aukde.iotdoor.databinding.ActivityMainBinding
import com.aukde.iotdoor.databinding.DialogLayoutBinding
import com.aukde.iotdoor.databinding.DialogLightsBinding
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

    private val handler = Handler()
    private val interval: Long = 30 * 1000 // 30 segundos en milisegundos
    private var counter = 0
    private val maxCounter = 5

    private lateinit var dialog: Dialog
    private lateinit var optionsBinding : DialogLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        optionsBinding = DialogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.setContentView(optionsBinding.root)
        dialog.setCancelable(false)
        val window: Window = dialog.window!!
        window.setLayout(950, 1400)

        viewButtons()

        DataDeviceProvider().getData(binding,this)

        preference = getSharedPreferences("cache", MODE_PRIVATE)
        name = preference.getString("key","").toString()

        val type = getSharedPreferences("cache", MODE_PRIVATE).getString("keyTypeUser","").toString()
        if(type == "client"){
            optionsBinding.cvUsers.visibility = View.GONE
            optionsBinding.lnRecords.visibility = View.GONE
        }

        //checkingProximitySensor()
        changeTextEvery30Seconds()

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
                            val time = (binding.time.text.toString()).toLong()
                            val minuteInSeconds = 60
                            val currentTime = System.currentTimeMillis()/1000

                            if (currentTime - time < minuteInSeconds) {

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

                            }else{
                                hideDialog()
                                Toasty.error(this@PasswordActivity, "DISPOSITIVO DESCONECTADO!", Toast.LENGTH_SHORT).show()
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
        binding.btnRecord.setOnClickListener {
            startActivity(Intent(this, Record::class.java))
        }
        binding.btnMenu.setOnClickListener {
            dialog.show()
        }

        binding.btnLights.setOnClickListener {
            val sharedPreferencesDevice = this.getSharedPreferences("cache", Context.MODE_PRIVATE)
            val id = sharedPreferencesDevice.getString("keyDevice", "")!!
            // Usa ViewBinding para inflar el layout del diálogo
            val dialogBinding = DialogLightsBinding.inflate(layoutInflater)

            // Crear el diálogo
            val dialog = android.app.AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .setTitle("Control de Luces")
                .setNegativeButton("Cerrar") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            // Recupera el estado actual de las luces desde Firebase
            mDatabase.child("devices").child(id).get().addOnSuccessListener { dataSnapshot ->
                val light1State = dataSnapshot.child("lights").child("light1").getValue(Int::class.java) ?: 0
                val light2State = dataSnapshot.child("lights").child("light2").getValue(Int::class.java) ?: 0

                // Configura el estado inicial de los switches
                dialogBinding.switchLight1.isChecked = light1State == 1
                dialogBinding.switchLight2.isChecked = light2State == 1
            }

            // Listener para el Switch de Luz 1
            dialogBinding.switchLight1.setOnCheckedChangeListener { _, isChecked ->
                val newState = if (isChecked) 1 else 0
                mDatabase.child("devices").child(id).child("lights").child("light1").setValue(newState)
            }

            // Listener para el Switch de Luz 2
            dialogBinding.switchLight2.setOnCheckedChangeListener { _, isChecked ->
                val newState = if (isChecked) 1 else 0
                mDatabase.child("devices").child(id).child("lights").child("light2").setValue(newState)
            }

            // Mostrar el diálogo
            dialog.show()
        }

        optionsBinding.cvLogout.setOnClickListener {
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

        optionsBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        optionsBinding.cvUsers.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, CreatePasswords::class.java))
        }
        optionsBinding.cvDevices.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, SyncDeviceActivity::class.java))
        }
        optionsBinding.cvRecords.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, Record::class.java))
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

        val sharedPreferences= getSharedPreferences("cache", Context.MODE_PRIVATE)
        val typeUser = sharedPreferences.getString("keyTypeUser","").toString()

        /*
        if(typeUser != "root"){
            binding.type.text = "Offline"
            binding.typeOffline.visibility = View.VISIBLE
            binding.typeOnline.visibility = View.GONE
            binding.edtPassword.visibility = View.GONE
            optionsBinding.cvUsers.visibility = View.GONE
        }else{
            binding.sw.isChecked = true
            binding.type.text = "Online"
        }
         */

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

    override fun onBackPressed() {
    }

    private fun openDoor() {

        val ip = binding.ipDevice.text.toString()

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

    private fun changeTextEvery30Seconds() {
        handler.post(object : Runnable {
            override fun run() {
                // Cambiar el texto del TextView aquí
                DataDeviceProvider().isConnect(binding,this@PasswordActivity)

                counter++

                if (counter < maxCounter) {
                    handler.postDelayed(this, interval)
                }
            }
        })
    }

    override fun onDestroy() {
        // Detén el handler cuando la actividad es destruida para evitar pérdidas de memoria
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

}