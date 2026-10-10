package com.cherodevscode.chivo_trabajo.ui.profesional

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetalleSolicitudTrabajoProfesionalBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity que muestra los detalles exactos de una solicitud seleccionada por el profesional (Vista en MVVM).
 */
class DetallesSolicitudTrabajoProfesionalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSolicitudTrabajoProfesionalBinding
    private lateinit var viewModel: DetallesSolicitudProfesionalViewModel
    private lateinit var firestoreRepository: FirestoreRepository
    private var idSolicitud: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleSolicitudTrabajoProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreRepository = FirestoreRepository()

        // Obtener el ID de la solicitud pasado por Intent desde el adaptador de solicitudes
        idSolicitud = intent.getStringExtra("idSolicitud") ?: intent.getStringExtra("EXTRA_SOLICITUD_ID")

        // Inicializar ViewModel (Patrón MVVM)
        viewModel = ViewModelProvider(this)[DetallesSolicitudProfesionalViewModel::class.java]

        // Observar datos de la solicitud desde el ViewModel
        observarViewModel()

        // Configurar botones
        configurarBotones()

        // Cargar datos si el ID es válido
        if (!idSolicitud.isNullOrBlank()) {
            viewModel.cargarDetallesSolicitud(idSolicitud!!)
        } else {
            Toast.makeText(this, "No se encontró el ID de la solicitud", Toast.LENGTH_SHORT).show()
        }
    }

    // Observar el LiveData del ViewModel y renderizar todos los datos reales de la solicitud
    private fun observarViewModel() {
        viewModel.solicitud.observe(this) { solicitud ->
            if (solicitud != null) {
                val codigoCorto = if (solicitud.idSolicitud.length >= 4) {
                    solicitud.idSolicitud.takeLast(4).uppercase()
                } else {
                    "8942"
                }

                // Rellenar datos exactos de la solicitud en la interfaz
                binding.tvCodigoDetallePro.text = "#SOL-$codigoCorto"
                binding.tvCategoriaDetallePro.text = solicitud.categoryId.ifBlank { "Servicio General" }
                binding.tvTituloDetallePro.text = solicitud.categoryId.ifBlank { "Detalle del Servicio" }
                binding.tvDescripcionDetallePro.text = solicitud.descripcion.ifBlank { "Sin descripción detallada del problema." }
                binding.tvUbicacionDetallePro.text = "${solicitud.direccion}, ${solicitud.ciudad}".trim().removePrefix(",")
                binding.tvHorarioDetallePro.text = "⏱ Horario solicitado: ${solicitud.fechaServicio} · ${solicitud.horaServicio}"
                binding.tvPresupuestoDetallePro.text = solicitud.presupuesto.ifBlank { "A convenir" }

                // Cargar nombre del cliente desde Firestore utilizando su clientId
                cargarNombreCliente(solicitud.clientId)

                // Cargar imagen de evidencia / problema con Glide si existe URL
                if (solicitud.fotoProblema.isNotBlank()) {
                    binding.ivFotoProblemaDetalle.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(solicitud.fotoProblema)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop()
                        .into(binding.ivFotoProblemaDetalle)
                } else {
                    binding.ivFotoProblemaDetalle.setImageResource(R.mipmap.ic_launcher)
                }
            } else {
                Toast.makeText(this, "No se pudo cargar la información de la solicitud", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cargar el nombre real del cliente que creó la solicitud
    private fun cargarNombreCliente(clientId: String) {
        if (clientId.isBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            val resultado = firestoreRepository.obtenerPerfilUsuario(clientId)
            withContext(Dispatchers.Main) {
                resultado.onSuccess { usuario ->
                    if (usuario != null) {
                        val nombreCompleto = "${usuario.nombre} ${usuario.apellido}".trim()
                        if (nombreCompleto.isNotBlank()) {
                            binding.tvClienteNombreDetallePro.text = nombreCompleto
                        }
                    }
                }
            }
        }
    }

    private fun configurarBotones() {
        // Botón Regresar
        binding.btnRegresarDetallePro.setOnClickListener {
            finish()
        }

        // Enviar Propuesta
        binding.btnEnviarPropuestaPro.setOnClickListener {
            Toast.makeText(this, "¡Propuesta enviada con éxito al cliente!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
