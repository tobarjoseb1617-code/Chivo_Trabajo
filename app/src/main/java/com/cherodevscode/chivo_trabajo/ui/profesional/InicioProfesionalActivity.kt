package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioProfesionalBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity

import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InicioProfesionalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioProfesionalBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        cargarDatosProfesional()

        // 1. Servicio en curso ahora mismo (Tarjeta) -> Ir a Gestionar Servicio en Curso
        binding.cardServicioEnCursoPro.setOnClickListener {
            startActivity(Intent(this, GestionarServicioEnCursoActivity::class.java))
        }

        // 2. Ver ruta GPS -> Ir a Detalles de Solicitud de Trabajo Profesional
        binding.btnVerRutaGps.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // 3. Abrir chat -> Ir a ChatActivity
        binding.btnAbrirChatPro.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // 4. Ver detalles de oportunidad 1 -> Ir a Detalles de Solicitud de Trabajo Profesional
        binding.btnDetallesOp1.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // 5. Ver detalles de oportunidad 2 -> Ir a Detalles de Solicitud de Trabajo Profesional
        binding.btnDetallesOp2.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // 6. Menú inferior: Solicitudes -> Ir a Solicitudes Trabajos Cercanos
        binding.navSolicitudesPro.setOnClickListener {
            startActivity(Intent(this, SolicitudesTrabajosCercanosActivity::class.java))
        }

        // 7. Menú inferior: Servicios -> Ir a Historial de Servicios (Paquete profesional)
        binding.navServiciosPro.setOnClickListener {
            startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
        }

        // 8. Menú inferior: Mensajes -> Ir a Historial de Chats
        binding.navMensajesPro.setOnClickListener {
            startActivity(Intent(this, HistorialChatsActivity::class.java))
        }

        // 9. Menú inferior y botón superior: Perfil / Configuración
        binding.navPerfilPro.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }


        // Propuesta rápida 1
        binding.btnPropuestaRapida1.setOnClickListener {
            Toast.makeText(this, "¡Propuesta rápida enviada para Fuga en Cisterna!", Toast.LENGTH_SHORT).show()
        }

        // Propuesta rápida 2
        binding.btnPropuestaRapida2.setOnClickListener {
            Toast.makeText(this, "¡Propuesta rápida enviada para Instalación de Grifería!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosProfesional() {

        val currentUser = authRepository.usuarioActual()

        if (currentUser == null) {
            binding.tvNombreProfesionalInicio.text = "Profesional"
            return
        }

        // Nombre temporal mientras se consultan los datos de Firestore
        binding.tvNombreProfesionalInicio.text =
            currentUser.displayName ?: "Profesional"

        // Foto de Firebase Auth, si existe
        if (currentUser.photoUrl != null) {
            Glide.with(this)
                .load(currentUser.photoUrl)
                .circleCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(binding.ivFotoProfesionalInicio)
        }

        // Obtener los datos reales desde Firestore
        CoroutineScope(Dispatchers.IO).launch {

            val resultado =
                firestoreRepository.obtenerPerfilUsuario(currentUser.uid)

            withContext(Dispatchers.Main) {

                resultado.onSuccess { usuario ->

                    if (usuario != null) {

                        // Nombre y apellido guardados en Firestore
                        val nombreCompleto =
                            "${usuario.nombre} ${usuario.apellido}".trim()

                        if (nombreCompleto.isNotBlank()) {
                            binding.tvNombreProfesionalInicio.text =
                                nombreCompleto
                        }

                        // Foto guardada en Usuario.fotoPerfil
                        if (usuario.fotoPerfil.isNotBlank()) {

                            Glide.with(this@InicioProfesionalActivity)
                                .load(usuario.fotoPerfil)
                                .circleCrop()
                                .placeholder(R.mipmap.ic_launcher)
                                .into(binding.ivFotoProfesionalInicio)
                        }
                    }
                }
            }
        }
    }
}
