
package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.model.Profesional
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioProfesionalBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import kotlinx.coroutines.launch

class InicioProfesionalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInicioProfesionalBinding
    private lateinit var authRepository: AuthRepository
    private val firestoreRepository = FirestoreRepository()

    private var profesionalActual: Profesional? = null
    private var uidProfesional: String? = null
    private var actualizandoDisponibilidad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)

        binding.bottomNavigation.selectedItemId = R.id.navInicioPro

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navSolicitudesPro -> {
                    startActivity(
                        Intent(this, SolicitudesTrabajosCercanosActivity::class.java)
                    )
                }

                R.id.navServiciosPro -> {
                    startActivity(
                        Intent(this, HistorialDeServiciosActivity::class.java)
                    )
                }

                R.id.navMensajesPro -> {
                    startActivity(
                        Intent(this, HistorialChatsActivity::class.java)
                            .putExtra("EXTRA_IS_PRO", true)
                    )
                }

                R.id.navPerfilPro -> {
                    startActivity(
                        Intent(this, ConfiguracionPerfilActivity::class.java)
                    )
                }
            }

            item.itemId == R.id.navInicioPro
        }

        cargarDatosProfesional()

        binding.switchDisponibilidadPro.setOnCheckedChangeListener { _, disponible ->
            if (!actualizandoDisponibilidad) {
                actualizarDisponibilidad(disponible)
            }
        }

        binding.cardServicioEnCursoPro.setOnClickListener {
            startActivity(
                Intent(this, GestionarServicioEnCursoActivity::class.java)
            )
        }

        binding.btnVerRutaGps.setOnClickListener {
            startActivity(
                Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java)
                    .putExtra("idSolicitud", "SRV-3821")
            )
        }

        binding.btnAbrirChatPro.setOnClickListener {
            startActivity(
                Intent(this, ChatActivity::class.java)
                    .putExtra("EXTRA_IS_PRO", true)
            )
        }

        binding.btnDetallesOp1.setOnClickListener {
            startActivity(
                Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java)
                    .putExtra("idSolicitud", "OP-001")
            )
        }

        binding.btnDetallesOp2.setOnClickListener {
            startActivity(
                Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java)
                    .putExtra("idSolicitud", "OP-002")
            )
        }

        binding.btnPerfilProTop.setOnClickListener {
            startActivity(
                Intent(this, ConfiguracionPerfilActivity::class.java)
            )
        }

        binding.btnPropuestaRapida1.setOnClickListener {
            Toast.makeText(
                this,
                "¡Propuesta rápida enviada para Fuga en Cisterna!",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnPropuestaRapida2.setOnClickListener {
            Toast.makeText(
                this,
                "¡Propuesta rápida enviada para Instalación de Grifería!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cargarDatosProfesional() {

        val usuarioActual = authRepository.usuarioActual()

        if (usuarioActual == null) {
            Toast.makeText(
                this,
                "No se encontró una sesión activa",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        uidProfesional = usuarioActual.uid

        lifecycleScope.launch {

            val resultadoUsuario =
                firestoreRepository.obtenerPerfilUsuario(usuarioActual.uid)

            resultadoUsuario.onSuccess { usuario ->

                if (usuario != null) {

                    val nombreCompleto =
                        "${usuario.nombre} ${usuario.apellido}".trim()

                    binding.tvNombreProfesionalInicio.text =
                        nombreCompleto.ifEmpty { "Profesional" }

                    if (usuario.fotoPerfil.isNotBlank()) {

                        Glide.with(this@InicioProfesionalActivity)
                            .load(usuario.fotoPerfil)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .circleCrop()
                            .into(binding.ivFotoProfesionalInicio)

                        Glide.with(this@InicioProfesionalActivity)
                            .load(usuario.fotoPerfil)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .circleCrop()
                            .into(binding.btnPerfilProTop)

                    } else {

                        binding.ivFotoProfesionalInicio.setImageResource(
                            R.mipmap.ic_launcher
                        )

                        binding.btnPerfilProTop.setImageResource(
                            R.mipmap.ic_launcher
                        )
                    }
                }

            }.onFailure { error ->

                Toast.makeText(
                    this@InicioProfesionalActivity,
                    "Error al cargar perfil: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val resultadoProfesional =
                firestoreRepository.obtenerProfesional(usuarioActual.uid)

            resultadoProfesional.onSuccess { profesional ->

                if (profesional != null) {

                    profesionalActual = profesional

                    val categoria = profesional.especialidades
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifEmpty {
                            profesional.especialidad.ifBlank {
                                "Sin categoría"
                            }
                        }

                    binding.tvCategoriaProfesionalInicio.text = categoria

                    mostrarEstadoDisponibilidad(profesional.disponible)

                } else {

                    Toast.makeText(
                        this@InicioProfesionalActivity,
                        "No se encontró el perfil profesional",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }.onFailure { error ->

                Toast.makeText(
                    this@InicioProfesionalActivity,
                    "Error al cargar datos profesionales: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun actualizarDisponibilidad(disponible: Boolean) {

        val profesional = profesionalActual
        val uid = uidProfesional

        if (profesional == null || uid == null) {

            Toast.makeText(
                this,
                "Espera a que cargue tu perfil profesional",
                Toast.LENGTH_SHORT
            ).show()

            actualizandoDisponibilidad = true
            binding.switchDisponibilidadPro.isChecked =
                profesional?.disponible ?: false
            actualizandoDisponibilidad = false

            return
        }

        lifecycleScope.launch {

            binding.switchDisponibilidadPro.isEnabled = false

            val profesionalActualizado = profesional.copy(
                disponible = disponible
            )

            val resultado =
                firestoreRepository.guardarProfesional(profesionalActualizado)

            resultado.onSuccess {

                profesionalActual = profesionalActualizado

                mostrarEstadoDisponibilidad(disponible)

                Toast.makeText(
                    this@InicioProfesionalActivity,
                    if (disponible) {
                        "Ahora estás disponible para recibir solicitudes"
                    } else {
                        "Ahora estás fuera de servicio"
                    },
                    Toast.LENGTH_SHORT
                ).show()

            }.onFailure { error ->

                actualizandoDisponibilidad = true
                binding.switchDisponibilidadPro.isChecked =
                    profesional.disponible
                actualizandoDisponibilidad = false

                mostrarEstadoDisponibilidad(profesional.disponible)

                Toast.makeText(
                    this@InicioProfesionalActivity,
                    "No se pudo actualizar la disponibilidad: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            binding.switchDisponibilidadPro.isEnabled = true
        }
    }

    private fun mostrarEstadoDisponibilidad(disponible: Boolean) {

        actualizandoDisponibilidad = true
        binding.switchDisponibilidadPro.isChecked = disponible
        actualizandoDisponibilidad = false

        if (disponible) {

            binding.tvEstadoDisponibilidadPro.text = "Estás disponible"

            binding.tvDescripcionDisponibilidadPro.text =
                "Puedes recibir nuevas solicitudes de trabajo."

            binding.viewIndicadorDisponibilidadPro.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#22C55E")
                )

        } else {

            binding.tvEstadoDisponibilidadPro.text = "No estás disponible"

            binding.tvDescripcionDisponibilidadPro.text =
                "Activa tu disponibilidad para recibir solicitudes."

            binding.viewIndicadorDisponibilidadPro.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#9CA3AF")
                )
        }
    }
}
