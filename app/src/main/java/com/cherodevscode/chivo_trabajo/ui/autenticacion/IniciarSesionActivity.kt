
package com.cherodevscode.chivo_trabajo.ui.autenticacion

import com.cherodevscode.chivo_trabajo.ui.admin.PanelAdminActivity
import android.content.Intent
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

        window.statusBarColor = androidx.core.content.ContextCompat.getColor(
            this,
            R.color.ui_background
        )

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars =
            resources.getBoolean(R.bool.light_system_bars)

        autenticacionViewModel = AutenticacionViewModel(this)

        Glide.with(this)
            .load("https://res.cloudinary.com/djwvfjt7k/image/upload/v1790744838/o8ipleapa27fyd72nsll.png")
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.ivLogoLogin!!)

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

            if (!msg.contains("éxito", ignoreCase = true) &&
                !msg.contains("exitoso", ignoreCase = true)
            ) {
                setLoading(false)
            }
        }

        autenticacionViewModel.tipoUsuarioDestino.observe(this) { tipo ->

            if (tipo != null) {

                val destino = when (tipo.uppercase()) {
                    "ADMIN" -> PanelAdminActivity::class.java
                    "PROFESIONAL" -> InicioProfesionalActivity::class.java
                    "CLIENTE" -> InicioActivity::class.java
                    else -> {
                        setLoading(false)
                        Toast.makeText(
                            this,
                            "Tipo de usuario no reconocido",
                            Toast.LENGTH_LONG
                        ).show()
                        return@observe
                    }
                }

                startActivity(Intent(this, destino))
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

        binding.btnLoginCorreo.text =
            if (loading) "Iniciando sesión..." else "Iniciar Sesión ➔"
    }
}
