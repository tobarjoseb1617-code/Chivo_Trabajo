package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioProfesionalBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class InicioProfesionalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInicioProfesionalBinding

    private val auth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar nombre y foto del profesional
        cargarDatosProfesional()

        // 1. Servicio en curso ahora mismo
        binding.cardServicioEnCursoPro.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    GestionarServicioEnCursoActivity::class.java
                )
            )
        }

        // 2. Ver ruta GPS
        binding.btnVerRutaGps.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DetallesSolicitudTrabajoProfesionalActivity::class.java
                )
            )
        }

        // 3. Abrir chat
        binding.btnAbrirChatPro.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ChatActivity::class.java
                )
            )
        }

        // 4. Ver detalles de oportunidad 1
        binding.btnDetallesOp1.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DetallesSolicitudTrabajoProfesionalActivity::class.java
                )
            )
        }

        // 5. Ver detalles de oportunidad 2
        binding.btnDetallesOp2.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DetallesSolicitudTrabajoProfesionalActivity::class.java
                )
            )
        }

        // 6. Menú inferior: Solicitudes
        binding.navSolicitudesPro.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SolicitudesTrabajosCercanosActivity::class.java
                )
            )
        }

        // 7. Menú inferior: Servicios
        binding.navServiciosPro.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    HistorialDeServiciosActivity::class.java
                )
            )
        }

        // 8. Menú inferior: Mensajes
        binding.navMensajesPro.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    HistorialChatsActivity::class.java
                ).putExtra("EXTRA_IS_PRO", true)
            )
        }

        // 9. Perfil
        binding.navPerfilPro.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ConfiguracionPerfilActivity::class.java
                )
            )
        }

        binding.btnPerfilProTop.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ConfiguracionPerfilActivity::class.java
                )
            )
        }

        // Propuesta rápida 1
        binding.btnPropuestaRapida1.setOnClickListener {
            Toast.makeText(
                this,
                "¡Propuesta rápida enviada para Fuga en Cisterna!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Propuesta rápida 2
        binding.btnPropuestaRapida2.setOnClickListener {
            Toast.makeText(
                this,
                "¡Propuesta rápida enviada para Instalación de Grifería!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Obtiene los datos del profesional que inició sesión
     * y muestra su nombre y foto.
     */
    private fun cargarDatosProfesional() {

        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            Toast.makeText(
                this,
                "No se encontró el usuario autenticado",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {

            val resultado =
                firestoreRepository.obtenerPerfilUsuario(usuarioActual.uid)

            resultado.onSuccess { usuario ->

                if (usuario != null) {

                    // Mostrar nombre y apellido
                    val nombreCompleto =
                        "${usuario.nombre} ${usuario.apellido}".trim()

                    binding.tvNombreProfesionalInicio.text =
                        if (nombreCompleto.isNotEmpty()) {
                            nombreCompleto
                        } else {
                            "Profesional"
                        }

                    // Mostrar foto de perfil
                    if (usuario.fotoPerfil.isNotEmpty()) {

                        Glide.with(this@InicioProfesionalActivity)
                            .load(usuario.fotoPerfil)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .centerCrop()
                            .into(binding.ivFotoProfesionalInicio)

                    } else {

                        binding.ivFotoProfesionalInicio.setImageResource(
                            R.mipmap.ic_launcher
                        )
                    }
                }
            }

            resultado.onFailure { error ->

                Toast.makeText(
                    this@InicioProfesionalActivity,
                    "Error al cargar perfil: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}