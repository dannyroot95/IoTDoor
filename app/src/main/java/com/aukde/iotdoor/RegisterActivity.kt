package com.aukde.iotdoor

import android.os.Bundle
import android.widget.Toast
import com.aukde.iotdoor.databinding.ActivityRegisterBinding

class RegisterActivity : BaseActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var mAuth : AuthenticationProvider
    private lateinit var mUser : UserProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = AuthenticationProvider()
        mUser = UserProvider()

        binding.btnRegister.setOnClickListener {
            register()
        }

    }

    private fun register() {
        val email : String = binding.edtEmail.text.toString()
        val password : String =  binding.edtPassword.text.toString()
        val fullname : String = binding.edtFullname.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && fullname.isNotEmpty()){
            if (password.length >= 6){
                showDialog("Registrando Usuario...")
                mAuth.register(email,password).addOnCompleteListener {  task ->
                    if (task.isSuccessful){
                        val id = task.result!!.user!!.uid
                        val mUserRegister = User(id,fullname,email)
                        mUser.create(mUserRegister).addOnCompleteListener { response ->
                            if (response.isSuccessful) {
                                Toast.makeText(this,"Usuario creado!", Toast.LENGTH_SHORT).show()
                                hideDialog()
                                mAuth.logout()
                                finish()
                            }else{
                                hideDialog()
                                Toast.makeText(this,"Error al registrar datos!", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            hideDialog()
                            Toast.makeText(this,"Error al registrar datos!", Toast.LENGTH_SHORT).show()
                        }.addOnCanceledListener {
                            hideDialog()
                            Toast.makeText(this,"Error al registrar datos!", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        hideDialog()
                        Toast.makeText(this,"Error al registrar usuario!", Toast.LENGTH_SHORT).show()
                    }
                }.addOnCanceledListener {
                    hideDialog()
                    Toast.makeText(this,"Error al registrar usuario!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    hideDialog()
                    Toast.makeText(this,"Error al registrar usuario!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"La constrase??a debe tener al menos 6 caracteres!", Toast.LENGTH_LONG).show()
            }

        }
        else{
            Toast.makeText(this,"Complete los campos!", Toast.LENGTH_SHORT).show()
          }
        }
    }