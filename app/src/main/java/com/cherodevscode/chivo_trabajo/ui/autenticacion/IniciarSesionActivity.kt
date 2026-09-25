package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityIniciarSesionBinding
import com.cherodevscode.chivo_trabajo.ui.cliente.InicioActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity

class IniciarSesionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIniciarSesionBinding
    private lateinit var autenticacionViewModel: AutenticacionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIniciarSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autenticacionViewModel = AutenticacionViewModel(this)

        binding.btnLoginCorreo.setOnClickListener {
            val correo = binding.etLoginCorreo.text.toString().trim()
            val contrasena = binding.etLoginContrasena.text.toString().trim()
            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            autenticacionViewModel.iniciarSesion(correo, contrasena)
        }

        binding.tvOlvideContrasena.setOnClickListener {
            val correo = binding.etLoginCorreo.text.toString().trim()
            if (correo.isEmpty()) {
                Toast.makeText(this, "Ingrese su correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            autenticacionViewModel.recuperarPassword(correo)
        }

        binding.btnGoogle.setOnClickListener {
            autenticacionViewModel.iniciarSesionGoogle(this)
        }

        binding.btnIrARegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        autenticacionViewModel.mensaje.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }

        autenticacionViewModel.tipoUsuarioDestino.observe(this) { tipo ->
            if (tipo != null) {
                val intent = if (tipo == "PROFESIONAL") {
                    Intent(this, InicioProfesionalActivity::class.java)
                } else {
                    Intent(this, InicioActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
