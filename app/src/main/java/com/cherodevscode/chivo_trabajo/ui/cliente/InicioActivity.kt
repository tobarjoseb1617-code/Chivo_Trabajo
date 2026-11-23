package com.cherodevscode.chivo_trabajo.ui.cliente

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.data.repository.ProfesionalesInicioRepository
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ContactoChat
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ContactosChatAdapter
import kotlinx.coroutines.CancellationException

import com.cherodevscode.chivo_trabajo.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import com.google.firebase.auth.FirebaseAuth
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class InicioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding
    private val firestoreRepository = FirestoreRepository()
    private var cargaProfesionales: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Material 3: los IDs del menú conservan los destinos de los botones anteriores.
        binding.bottomNavigation.selectedItemId = R.id.navInicio
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navSolicitudes -> {
                    startActivity(Intent(this, HistorialSolicitudesClienteActivity::class.java))
                }
                R.id.navServicios -> {
                    startActivity(Intent(this, HistorialServiciosClienteActivity::class.java))
                }
                R.id.navMensajes -> {
                    startActivity(Intent(this, HistorialChatsActivity::class.java))
                }
                R.id.navPerfil -> {
                    startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
                }
            }
            // Cada destino abre su Activity; el indicador identifica esta pantalla al regresar.
            item.itemId == R.id.navInicio
        }

        cargarNombreUsuario()

        // 1. Ir a Seguimiento en Vivo del Cliente al tocar la tarjeta de servicio en curso
        binding.cardServicioEnCurso.setOnClickListener {
            startActivity(Intent(this, SeguimientoClienteActivity::class.java).putExtra("EXTRA_PIN", "3821"))
        }

        // 2. Ir a Seguimiento en Vivo del Cliente al tocar el botón de "Ver mapa"
        binding.btnVerSeguimiento.setOnClickListener {
            startActivity(Intent(this, SeguimientoClienteActivity::class.java).putExtra("EXTRA_PIN", "3821"))
        }

        cargarProfesionales()

        // 5. Categorías populares (Fontanería, Electricidad, Limpieza, Pintura) -> RadarProfesionalesActivity
        binding.cardCatFontaneria.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.cardCatElectricidad.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.cardCatLimpieza.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.cardCatPintura.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.btnVerTodasCategorias.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }

        binding.btnPerfilTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }
    }

    private fun cargarProfesionales() {
        val adapter = ContactosChatAdapter { contacto ->
            startActivity(Intent(this, ChatActivity::class.java).putExtra("EXTRA_CONTACTO_UID", contacto.id))
        }
        binding.listaProfesionalesInicio.layoutManager = LinearLayoutManager(this)
        binding.listaProfesionalesInicio.adapter = adapter
        cargaProfesionales?.cancel()
        cargaProfesionales = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.tvEstadoProfesionales.text = "Cargando profesionales…"
                binding.tvEstadoProfesionales.visibility = View.VISIBLE
                binding.tvEstadoProfesionales.setOnClickListener(null)
                try {
                    ProfesionalesInicioRepository().observar().collect { profesionales ->
                        adapter.mostrar(profesionales.map { dato ->
                            val pro = dato.profesional
                            val especialidad = pro.especialidades.filter { it.isNotBlank() }.joinToString(", ")
                                .ifBlank { pro.especialidad }.ifBlank { pro.descripcion }
                            val calificacion = if (pro.cantidadCalificaciones > 0) {
                                "★ %.1f (%d reseñas)".format(pro.calificacionPromedio, pro.cantidadCalificaciones)
                            } else "Sin reseñas"
                            val disponibilidad = if (pro.disponible) "Disponible" else "No disponible ahora"
                            ContactoChat(
                                dato.usuario.uid,
                                "${dato.usuario.nombre} ${dato.usuario.apellido}".trim().ifBlank { "Profesional sin nombre registrado" },
                                listOfNotNull(especialidad.takeIf { it.isNotBlank() }, calificacion, disponibilidad).joinToString(" • "),
                                dato.usuario.fotoPerfil
                            )
                        })
                        binding.tvEstadoProfesionales.text = if (profesionales.isEmpty()) "No hay profesionales registrados para mostrar" else ""
                        binding.tvEstadoProfesionales.visibility = if (profesionales.isEmpty()) View.VISIBLE else View.GONE
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    adapter.mostrar(emptyList())
                    binding.tvEstadoProfesionales.visibility = View.VISIBLE
                    android.util.Log.e("ProfesionalesInicio", "Error al consultar la colección profesionales", e)
                    binding.tvEstadoProfesionales.text = when ((e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code) {
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                            "Firebase no permite leer la colección profesionales. Revisa sus reglas de acceso. Toca para reintentar."
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE ->
                            "No se pudo conectar con Firebase. Revisa tu conexión y toca para reintentar."
                        else -> "No se pudieron cargar los profesionales. Toca para reintentar."
                    }
                    binding.tvEstadoProfesionales.setOnClickListener { cargarProfesionales() }
                }
            }
        }
    }

    private fun cargarNombreUsuario() {
        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (usuarioActual == null) {
            return
        }

        lifecycleScope.launch {
            val resultado =
                firestoreRepository.obtenerPerfilUsuario(usuarioActual.uid)

            resultado.onSuccess { usuario ->
                if (usuario != null) {
                    binding.tvSaludoUsuario.text =
                        "¡Hola, ${usuario.nombre}! "
                }
            }.onFailure {
                Toast.makeText(
                    this@InicioActivity,
                    "No se pudo cargar el nombre del usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
