package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityRegistroBinding

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private lateinit var autenticacionViewModel: AutenticacionViewModel
    private var rolSeleccionado = "CLIENTE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autenticacionViewModel = AutenticacionViewModel(this)

        binding.cardCliente.setOnClickListener {
            rolSeleccionado = "CLIENTE"
            binding.cardCliente.setCardBackgroundColor(Color.parseColor("#F0F9FF"))
            binding.cardProfesional.setCardBackgroundColor(Color.parseColor("#F8FAFC"))
        }

        binding.cardProfesional.setOnClickListener {
            rolSeleccionado = "PROFESIONAL"
            binding.cardProfesional.setCardBackgroundColor(Color.parseColor("#F0F9FF"))
            binding.cardCliente.setCardBackgroundColor(Color.parseColor("#F8FAFC"))
        }

        binding.btnRegresarRegistro.setOnClickListener {
            finish()
        }

        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etRegNombre.text.toString().trim()
            val apellido = binding.etRegApellido.text.toString().trim()
            val correo = binding.etRegCorreo.text.toString().trim()
            val contrasena = binding.etRegContrasena.text.toString().trim()
            val telefono = binding.etRegTelefono.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            autenticacionViewModel.registrarse(nombre, apellido, correo, contrasena, telefono, rolSeleccionado)
        }

        binding.btnVolverLogin.setOnClickListener {
            finish()
        }

        autenticacionViewModel.mensaje.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }

        autenticacionViewModel.tipoUsuarioDestino.observe(this) { tipo ->
            if (tipo != null) {
                val intent = if (tipo == "PROFESIONAL") {
                    Intent(this, RegistroProfesionalPaso2Activity::class.java)
                } else {
                    Intent(this, RegistroClientePaso2Activity::class.java)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
