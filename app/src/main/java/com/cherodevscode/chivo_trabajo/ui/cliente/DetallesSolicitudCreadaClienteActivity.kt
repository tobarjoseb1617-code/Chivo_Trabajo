
package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetallesSolicitudCreadaClienteBinding
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class DetallesSolicitudCreadaClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesSolicitudCreadaClienteBinding

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var idSolicitud: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetallesSolicitudCreadaClienteBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        // Recibir el ID de la solicitud seleccionada
        idSolicitud = intent.getStringExtra("idSolicitud").orEmpty()

        configurarBotones()

        if (idSolicitud.isBlank()) {
            Toast.makeText(
                this,
                "No se recibió el ID de la solicitud",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        cargarDetalleSolicitud()
    }

    private fun configurarBotones() {

        // Regresar al historial
        binding.btnRegresarSeguimientoCliente.setOnClickListener {
            finish()
        }

        // Abrir configuración del perfil
        binding.btnPerfilDetallesTop.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ConfiguracionPerfilActivity::class.java
                )
            )
        }

        // Editar solicitud (pendiente de implementar)
        binding.btnEditarSolicitud.setOnClickListener {
            Toast.makeText(
                this,
                "Próximamente podrás editar tu solicitud",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Cancelar solicitud (pendiente de implementar)
        binding.btnCancelarSolicitud.setOnClickListener {
            Toast.makeText(
                this,
                "La cancelación estará disponible próximamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cargarDetalleSolicitud() {

        firestore.collection("Solicitudes")
            .document(idSolicitud)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {

                    Toast.makeText(
                        this,
                        "No se encontró la solicitud",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                    return@addOnSuccessListener
                }

                // Comprobar que la solicitud pertenece al cliente
                val uid = auth.currentUser?.uid
                val clienteId = documento.getString("clientId")

                if (uid == null || clienteId != uid) {

                    Toast.makeText(
                        this,
                        "No tienes permiso para ver esta solicitud",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                    return@addOnSuccessListener
                }

                // Obtener información de Firestore

                val categoria = documento.getString("categoryId")
                    ?: "Sin categoría"

                val descripcion = documento.getString("descripcion")
                    ?: "Sin descripción"

                val estado = documento.getString("estado")
                    ?: "PENDIENTE"

                val direccion = documento.getString("direccion")
                    .orEmpty()

                val ciudad = documento.getString("ciudad")
                    .orEmpty()

                val fechaServicio = documento.getString("fechaServicio")
                    ?: "Sin fecha"

                val horaServicio = documento.getString("horaServicio")
                    ?: "Sin hora"

                val presupuesto = documento.getString("presupuesto")
                    ?: "No especificado"

                val fotoProblema = documento.getString("fotoProblema")
                    .orEmpty()

                val fechaCreacion = documento.getTimestamp("fechaCreacion")

                // ID de la solicitud
                binding.tvIdSolicitudDetalle.text =
                    "#SOL-${idSolicitud.take(8)}"

                // Título y categoría
                binding.tvTituloSolicitudDetalle.text = categoria

                binding.tvCategoriaSolicitudDetalle.text = categoria

                // Descripción
                binding.tvDescripcionSolicitudDetalle.text = descripcion

                // Estado
                val estadoFormateado = when (
                    estado.uppercase(Locale.ROOT)
                ) {
                    "PENDIENTE" -> "Pendiente"
                    "ABIERTA", "PUBLICADA" -> "Abierta"
                    "EN_PROCESO", "EN PROCESO" -> "En proceso"
                    "ACEPTADA", "ASIGNADA" -> "Asignada"
                    "FINALIZADA", "FINALIZADO", "COMPLETADA" -> "Finalizada"
                    "CANCELADA" -> "Cancelada"
                    else -> estado.replace("_", " ")
                }

                binding.tvEstadoSolicitudDetalle.text = estadoFormateado

                // Ubicación
                val ubicacion = listOf(direccion, ciudad)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(", ")
                    .ifBlank { "Ubicación no especificada" }

                binding.tvUbicacionSolicitudDetalle.text = ubicacion

                // Fecha y hora del servicio
                binding.tvFechaServicioDetalle.text =
                    "Fecha: $fechaServicio\nHora: $horaServicio"

                // Presupuesto
                binding.tvPresupuestoSolicitudDetalle.text = presupuesto

                // Fecha de creación
                if (fechaCreacion != null) {

                    val formato = SimpleDateFormat(
                        "dd/MM/yyyy · hh:mm a",
                        Locale("es", "SV")
                    )

                    binding.tvFechaCreacionDetalle.text =
                        "Publicada: ${formato.format(fechaCreacion.toDate())}"

                } else {

                    binding.tvFechaCreacionDetalle.text =
                        "Fecha de publicación no disponible"
                }

                // Mostrar fotografía del problema
                if (fotoProblema.isNotBlank()) {

                    binding.ivFotoProblemaDetalle.visibility = View.VISIBLE
                    binding.tvSinFotoDetalle.visibility = View.GONE

                    Glide.with(this)
                        .load(fotoProblema)
                        .centerCrop()
                        .into(binding.ivFotoProblemaDetalle)

                } else {

                    binding.ivFotoProblemaDetalle.visibility = View.GONE
                    binding.tvSinFotoDetalle.visibility = View.VISIBLE

                    binding.tvSinFotoDetalle.text =
                        "No se agregó ninguna fotografía"
                }

                // Mensaje de propuestas
                binding.tvEstadoPropuestasDetalle.text =
                    "Las propuestas de profesionales aparecerán aquí cuando estén disponibles."
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Error al cargar la solicitud: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
