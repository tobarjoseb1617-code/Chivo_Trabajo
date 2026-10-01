package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityRegistroBinding
import android.text.InputType
import com.cherodevscode.chivo_trabajo.R
// Apariencia: los colores creados desde Kotlin respetan el modo elegido.
class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private lateinit var autenticacionViewModel: AutenticacionViewModel
    private var rolSeleccionado = "CLIENTE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autenticacionViewModel = AutenticacionViewModel(this)

        var contrasenaVisible = false

        binding.btnMostrarContrasena.setOnClickListener {

            contrasenaVisible = !contrasenaVisible

            if (contrasenaVisible) {

                binding.etRegContrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.btnMostrarContrasena.setImageResource(
                    R.drawable.ic_visibility
                )

                binding.btnMostrarContrasena.contentDescription =
                    "Ocultar contraseña"

            } else {

                binding.etRegContrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                binding.btnMostrarContrasena.setImageResource(
                    R.drawable.ic_visibility_off
                )

                binding.btnMostrarContrasena.contentDescription =
                    "Mostrar contraseña"
            }

            binding.etRegContrasena.setSelection(
                binding.etRegContrasena.text.length
            )
        }

        binding.cardCliente.setOnClickListener {
            rolSeleccionado = "CLIENTE"

            binding.cbCliente.isChecked = true
            binding.cbProfesional.isChecked = false

            binding.cardCliente.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(this@RegistroActivity, com.cherodevscode.chivo_trabajo.R.color.ui_info_background)
            )

            binding.cardProfesional.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(this@RegistroActivity, com.cherodevscode.chivo_trabajo.R.color.ui_surface)
            )
        }

        binding.cardProfesional.setOnClickListener {
            rolSeleccionado = "PROFESIONAL"

            binding.cbCliente.isChecked = false
            binding.cbProfesional.isChecked = true

            binding.cardProfesional.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(this@RegistroActivity, com.cherodevscode.chivo_trabajo.R.color.ui_info_background)
            )

            binding.cardCliente.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(this@RegistroActivity, com.cherodevscode.chivo_trabajo.R.color.ui_surface)
            )
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
