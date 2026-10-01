package com.cherodevscode.chivo_trabajo.ui.cliente

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
import com.cherodevscode.chivo_trabajo.ui.profesional.GestionarServicioEnCursoActivity
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
                    startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
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

        // 1. Ir a Seguimiento en Vivo al tocar la tarjeta de servicio en curso
        binding.cardServicioEnCurso.setOnClickListener {
            startActivity(Intent(this, SeguimientoActivity::class.java).putExtra("EXTRA_PIN", "3821"))
        }

        // 2. Ir a Gestionar Servicio en Curso al tocar el botón de "Ver mapa"
        binding.btnVerSeguimiento.setOnClickListener {
            startActivity(Intent(this, GestionarServicioEnCursoActivity::class.java))
        }

        // 3. Profesionales cercanos: Ver perfil 1 y 2 -> PerfilProfesionalActivity
        binding.btnVerPerfilPro1.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }
        binding.btnVerPerfilPro2.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // 4. Profesionales cercanos: Solicitar 1 y 2 -> ChatActivity (Abrir chat)
        binding.btnSolicitarPro1.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        binding.btnSolicitarPro2.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

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
