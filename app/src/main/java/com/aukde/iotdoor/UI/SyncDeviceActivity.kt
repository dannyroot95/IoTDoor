package com.aukde.iotdoor.UI

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.aukde.iotdoor.Providers.AuthenticationProvider
import com.aukde.iotdoor.Providers.UserProvider
import com.aukde.iotdoor.databinding.ActivitySyncDeviceBinding
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator

class SyncDeviceActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySyncDeviceBinding
    var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnQr.setOnClickListener {
            initScanner()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                mDatabase.child("devices").child(result.contents.toString()).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val root = snapshot.child("hasRoot").value.toString()
                            if(root == "null"){
                                UserProvider().updateDeviceAndRoot(result.contents,"root")
                                UserProvider().updateDevice(result.contents,AuthenticationProvider().getId())
                                val sharedPreferences= getSharedPreferences("cache", Context.MODE_PRIVATE)
                                val editorDevice = sharedPreferences.edit()
                                editorDevice.putString("keyDevice",result.contents.toString())
                                editorDevice.apply()
                                val intent = Intent(this@SyncDeviceActivity, PasswordActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }else{
                                UserProvider().updateOnlyDevice(result.contents)
                                val sharedPreferences= getSharedPreferences("cache", Context.MODE_PRIVATE)
                                val editorDevice = sharedPreferences.edit()
                                editorDevice.putString("keyDevice",result.contents.toString())
                                editorDevice.apply()
                                val intent = Intent(this@SyncDeviceActivity, PasswordActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }
                        }else{
                            Toast.makeText(this@SyncDeviceActivity, "No existe dispositivo!", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Atros v1.0")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onBackPressed() {
    }

}