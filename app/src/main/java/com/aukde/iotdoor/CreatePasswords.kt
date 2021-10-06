package com.aukde.iotdoor

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aukde.iotdoor.databinding.ActivityCreatePasswordsBinding
import com.google.firebase.database.*
import java.util.HashMap

class CreatePasswords : BaseActivity() {

    private lateinit var binding : ActivityCreatePasswordsBinding
    var mDatabase : DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
    var idSelected : String = ""
    var nameSelected : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePasswordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getAllUsers()

        binding.btnAsign.setOnClickListener {
            val password = binding.edtClave.text.toString()
            if (idSelected != "" && password.isNotEmpty()){
                showDialog("Creando clave al usuario : $nameSelected")
                val map: MutableMap<String, Any> = HashMap()
                map["password"] = password
                mDatabase.child(idSelected).updateChildren(map).addOnCompleteListener {
                    if (it.isSuccessful){
                        hideDialog()
                        Toast.makeText(this@CreatePasswords,"Clave asignada!",Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else{
                        hideDialog()
                        Toast.makeText(this@CreatePasswords,"Error!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(this@CreatePasswords,"Seleccione usuario ó digite la clave",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun getAllUsers(){
        val listName : MutableList<String> = ArrayList()
        val listID : MutableList<String> = ArrayList()
        mDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val valueID = data.child("id").value.toString()
                        val valueName = data.child("fullname").value.toString()
                        listID.add(valueID)
                        listName.add(valueName)
                    }
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this@CreatePasswords,
                            R.layout.support_simple_spinner_dropdown_item, listName)
                    binding.spUsers.adapter = arrayAdapter
                    binding.spUsers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            idSelected = listID[position]
                            nameSelected = listName[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}