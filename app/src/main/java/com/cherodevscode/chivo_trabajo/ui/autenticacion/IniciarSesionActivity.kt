package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivityIniciarSesionBinding
import com.cherodevscode.chivo_trabajo.ui.cliente.InicioActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity

class IniciarSesionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIniciarSesionBinding
    private lateinit var autenticacionViewModel: AutenticacionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Zonas seguras (lateral, horizontal, vertical) y Status Bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityIniciarSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            view.setPadding(
                maxOf(systemBars.left, cutout.left),
                maxOf(systemBars.top, cutout.top),
                maxOf(systemBars.right, cutout.right),
                maxOf(systemBars.bottom, cutout.bottom)
            )
            insets
        }

        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        autenticacionViewModel = AutenticacionViewModel(this)

        // Cargar logo desde Cloudinary
        val logoView = binding.ivLogoLogin
        if (logoView != null) {
            Glide.with(this)
                .load("https://res.cloudinary.com/djwvfjt7k/image/upload/v1790744838/o8ipleapa27fyd72nsll.png")
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(logoView)
        }

        // Mostrar u ocultar contraseña
        var contrasenaVisible = false

        binding.btnMostrarLoginContrasena.setOnClickListener {
            contrasenaVisible = !contrasenaVisible

            if (contrasenaVisible) {
                binding.etLoginContrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.btnMostrarLoginContrasena.setImageResource(
                    R.drawable.ic_visibility
                )

                binding.btnMostrarLoginContrasena.contentDescription =
                    "Ocultar contraseña"

            } else {
                binding.etLoginContrasena.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                binding.btnMostrarLoginContrasena.setImageResource(
                    R.drawable.ic_visibility_off
                )

                binding.btnMostrarLoginContrasena.contentDescription =
                    "Mostrar contraseña"
            }

            // Mantener el cursor al final del texto
            binding.etLoginContrasena.setSelection(
                binding.etLoginContrasena.text.length
            )
        }

        binding.btnLoginCorreo.setOnClickListener {
            val correo = binding.etLoginCorreo.text.toString().trim()
            val contrasena = binding.etLoginContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(
                    this,
                    "Ingrese correo y contraseña",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Estado de carga (Cargando)
            setLoading(true)
            autenticacionViewModel.iniciarSesion(correo, contrasena)
        }

        binding.tvOlvideContrasena.setOnClickListener {
            val correo = binding.etLoginCorreo.text.toString().trim()

            if (correo.isEmpty()) {
                Toast.makeText(
                    this,
                    "Ingrese su correo electrónico",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            autenticacionViewModel.recuperarPassword(correo)
        }

        binding.btnGoogle.setOnClickListener {
            setLoading(true)
            autenticacionViewModel.iniciarSesionGoogle(this)
        }

        binding.btnIrARegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        autenticacionViewModel.mensaje.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            if (!msg.contains("éxito", ignoreCase = true) && !msg.contains("exitoso", ignoreCase = true)) {
                setLoading(false)
            }
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

    private fun setLoading(loading: Boolean) {
        binding.etLoginCorreo.isEnabled = !loading
        binding.etLoginContrasena.isEnabled = !loading
        binding.btnLoginCorreo.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
        binding.btnIrARegistro.isEnabled = !loading
        if (loading) {
            binding.btnLoginCorreo.text = "Iniciando sesión..."
        } else {
            binding.btnLoginCorreo.text = "Iniciar Sesión ➔"
        }
    }
}
