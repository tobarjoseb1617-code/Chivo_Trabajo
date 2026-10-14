
package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetallesSolicitudCreadaClienteBinding
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class DetallesSolicitudCreadaClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesSolicitudCreadaClienteBinding

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var idSolicitud = ""
    private var operacionEnCurso = false

    // Actualiza los detalles cuando se guarda una edición.
    private val editarSolicitudLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            cargarDetalleSolicitud()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetallesSolicitudCreadaClienteBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        idSolicitud = intent.getStringExtra("idSolicitud").orEmpty()

        configurarBotones()

        if (idSolicitud.isBlank()) {
            mostrarMensaje("No se recibió el ID de la solicitud")
            finish()
            return
        }

        cargarDetalleSolicitud()
    }

    // Configura la navegación y las acciones de la solicitud.
    private fun configurarBotones() {

        binding.btnRegresarSeguimientoCliente.setOnClickListener {
            finish()
        }

        binding.btnPerfilDetallesTop.setOnClickListener {
            startActivity(
                Intent(this, ConfiguracionPerfilActivity::class.java)
            )
        }

        binding.btnEditarSolicitud.setOnClickListener {

            if (operacionEnCurso) return@setOnClickListener

            if (idSolicitud.isBlank()) {
                mostrarMensaje("No se encontró la solicitud")
                return@setOnClickListener
            }

            val intent = Intent(
                this,
                CrearSolicitudActivity::class.java
            ).apply {
                putExtra("idSolicitud", idSolicitud)
            }

            editarSolicitudLauncher.launch(intent)
        }

        binding.btnCancelarSolicitud.setOnClickListener {
            confirmarCancelacion()
        }
    }

    // Consulta la solicitud y muestra su información actualizada.
    private fun cargarDetalleSolicitud() {

        firestore.collection("Solicitudes")
            .document(idSolicitud)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    mostrarMensaje("No se encontró la solicitud")
                    finish()
                    return@addOnSuccessListener
                }

                val uid = auth.currentUser?.uid
                val clienteId = documento.getString("clientId")

                // Solo el propietario puede consultar esta solicitud.
                if (uid == null || clienteId != uid) {
                    mostrarMensaje("No tienes permiso para ver esta solicitud")
                    finish()
                    return@addOnSuccessListener
                }

                val estado = documento.getString("estado") ?: "PENDIENTE"
                val profesionalId = documento.getString("profesionalId")

                actualizarBotonCancelar(estado, profesionalId)
                actualizarBotonEditar(estado, profesionalId)

                val categoria = documento.getString("categoryId")
                    ?: "Sin categoría"

                val descripcion = documento.getString("descripcion")
                    ?: "Sin descripción"

                val direccion = documento.getString("direccion").orEmpty()
                val ciudad = documento.getString("ciudad").orEmpty()

                val fechaServicio = documento.getString("fechaServicio")
                    ?: "Sin fecha"

                val horaServicio = documento.getString("horaServicio")
                    ?: "Sin hora"

                val presupuesto = documento.getString("presupuesto")
                    ?: "No especificado"

                val fotoProblema = documento.getString("fotoProblema")
                    .orEmpty()

                val fechaCreacion = documento.getTimestamp("fechaCreacion")

                binding.tvIdSolicitudDetalle.text =
                    "#SOL-${idSolicitud.take(8)}"

                binding.tvTituloSolicitudDetalle.text = categoria
                binding.tvCategoriaSolicitudDetalle.text = categoria
                binding.tvDescripcionSolicitudDetalle.text = descripcion

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

                val ubicacion = listOf(direccion, ciudad)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(", ")
                    .ifBlank { "Ubicación no especificada" }

                binding.tvUbicacionSolicitudDetalle.text = ubicacion

                binding.tvFechaServicioDetalle.text =
                    "Fecha: $fechaServicio\nHora: $horaServicio"

                binding.tvPresupuestoSolicitudDetalle.text = presupuesto

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

                binding.tvEstadoPropuestasDetalle.text =
                    "Las propuestas de profesionales aparecerán aquí cuando estén disponibles."
            }
            .addOnFailureListener { error ->
                mostrarMensaje(
                    "Error al cargar la solicitud: ${error.localizedMessage}"
                )
            }
    }

    // Permite cancelar únicamente solicitudes pendientes y sin profesional.
    private fun actualizarBotonCancelar(
        estado: String,
        profesionalId: String?
    ) {

        val puedeCancelar =
            estado.uppercase(Locale.ROOT) == "PENDIENTE" &&
                    profesionalId.isNullOrBlank()

        binding.btnCancelarSolicitud.visibility =
            if (puedeCancelar) View.VISIBLE else View.GONE
    }

    // Permite editar únicamente solicitudes pendientes y sin profesional.
    private fun actualizarBotonEditar(
        estado: String,
        profesionalId: String?
    ) {

        val puedeEditar =
            estado.uppercase(Locale.ROOT) == "PENDIENTE" &&
                    profesionalId.isNullOrBlank()

        binding.btnEditarSolicitud.visibility =
            if (puedeEditar) View.VISIBLE else View.GONE
    }

    // Solicita confirmación antes de cancelar la solicitud.
    private fun confirmarCancelacion() {

        if (operacionEnCurso) return

        MaterialAlertDialogBuilder(this)
            .setTitle("Cancelar solicitud")
            .setMessage(
                "¿Estás seguro de que deseas cancelar esta solicitud? " +
                        "Dejará de estar disponible para los profesionales, " +
                        "pero permanecerá en tu historial."
            )
            .setNegativeButton("Volver", null)
            .setPositiveButton("Sí, cancelar") { _, _ ->
                cancelarSolicitud()
            }
            .show()
    }

    // Utiliza una transacción para evitar cancelar solicitudes ya asignadas.
    private fun cancelarSolicitud() {

        val uid = auth.currentUser?.uid ?: run {
            mostrarMensaje("Debes iniciar sesión")
            return
        }

        if (operacionEnCurso) return

        operacionEnCurso = true

        val referencia = firestore.collection("Solicitudes")
            .document(idSolicitud)

        firestore.runTransaction { transaccion ->

            val documento = transaccion.get(referencia)

            if (!documento.exists() ||
                documento.getString("clientId") != uid
            ) {
                throw IllegalStateException(
                    "No tienes permiso para cancelar esta solicitud"
                )
            }

            val estadoActual = documento.getString("estado")
                ?.uppercase(Locale.ROOT)

            val profesionalId = documento.getString("profesionalId")

            if (estadoActual != "PENDIENTE" ||
                !profesionalId.isNullOrBlank()
            ) {
                throw IllegalStateException(
                    "Esta solicitud ya no se puede cancelar"
                )
            }

            transaccion.update(
                referencia,
                mapOf(
                    "estado" to "CANCELADA",
                    "fechaActualizacion" to FieldValue.serverTimestamp()
                )
            )

            null
        }
            .addOnSuccessListener {

                operacionEnCurso = false

                mostrarMensaje("Solicitud cancelada correctamente")
                cargarDetalleSolicitud()
            }
            .addOnFailureListener { error ->

                operacionEnCurso = false

                mostrarMensaje(
                    error.localizedMessage
                        ?: "No se pudo cancelar la solicitud"
                )

                cargarDetalleSolicitud()
            }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
