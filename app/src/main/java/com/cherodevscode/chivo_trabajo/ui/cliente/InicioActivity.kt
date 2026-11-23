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

import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class InicioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding
    private val firestoreRepository = FirestoreRepository()

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
        lifecycleScope.launch {
            try {
                val profesionales = ProfesionalesInicioRepository().obtener()
                adapter.mostrar(profesionales.map { dato ->
                    val distancia = dato.distanciaKm?.let { " • %.1f km".format(it) }.orEmpty()
                    ContactoChat(
                        dato.usuario.uid, "${dato.usuario.nombre} ${dato.usuario.apellido}".trim(),
                        "${dato.profesional.especialidad} • ★ ${dato.profesional.calificacionPromedio}$distancia • Disponible", dato.usuario.fotoPerfil
                    )
                })
                binding.tvEstadoProfesionales.text = if (profesionales.isEmpty()) "No hay profesionales disponibles" else ""
                binding.tvEstadoProfesionales.visibility = if (profesionales.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                binding.tvEstadoProfesionales.text = "No se pudieron cargar los profesionales. Toca para reintentar."
                binding.tvEstadoProfesionales.setOnClickListener { cargarProfesionales() }
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
