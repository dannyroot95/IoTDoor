package com.aukde.iotdoor.UI

import android.os.Bundle
import android.widget.Toast
import com.aukde.iotdoor.BaseActivity
import com.aukde.iotdoor.databinding.ActivityRecoveryPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty


class RecoveryPasswordActivity : BaseActivity() {

    private lateinit var binding : ActivityRecoveryPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoveryPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRecovery.setOnClickListener {
            recoveryPassword()
        }


    }

    private fun recoveryPassword() {
        val email = binding.edtRecoveryEmail.text.toString()

        if (email.isNotEmpty()){
            showDialog("Enviando enlace")
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toasty.success(this,"Enlace enviado a su CORREO!",Toast.LENGTH_LONG).show()
                        hideDialog()
                        finish()
                    }
                    else{
                        hideDialog()
                        Toasty.error(this,"No se pudo enviar el enlace",Toast.LENGTH_SHORT).show()
                    }
                }.addOnCanceledListener {
                    hideDialog()
                    Toasty.error(this,"No se pudo enviar el enlace",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    hideDialog()
                    Toasty.error(this,"No se pudo enviar el enlace",Toast.LENGTH_SHORT).show()
                }
        }
        else{
            Toasty.warning(this,"Ingrese su Correo!",Toast.LENGTH_SHORT).show()
        }

    }
}