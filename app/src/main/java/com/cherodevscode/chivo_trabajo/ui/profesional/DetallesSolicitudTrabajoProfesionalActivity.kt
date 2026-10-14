package com.cherodevscode.chivo_trabajo.ui.profesional

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.model.Propuesta
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetalleSolicitudTrabajoProfesionalBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Muestra los detalles de una solicitud y permite
 * que el profesional envíe una propuesta al cliente.
 */
class DetallesSolicitudTrabajoProfesionalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSolicitudTrabajoProfesionalBinding
    private lateinit var viewModel: DetallesSolicitudProfesionalViewModel
    private lateinit var firestoreRepository: FirestoreRepository

    private var idSolicitud: String? = null
    private var solicitudActual: Solicitud? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleSolicitudTrabajoProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreRepository = FirestoreRepository()

        idSolicitud = intent.getStringExtra("idSolicitud")
            ?: intent.getStringExtra("EXTRA_SOLICITUD_ID")

        viewModel = ViewModelProvider(this)[
            DetallesSolicitudProfesionalViewModel::class.java
        ]

        configurarBotones()
        configurarFormularioPropuesta()
        observarViewModel()

        if (!idSolicitud.isNullOrBlank()) {
            viewModel.cargarDetallesSolicitud(idSolicitud!!)
        } else {
            Toast.makeText(
                this,
                "No se encontró el ID de la solicitud",
                Toast.LENGTH_SHORT
            ).show()
            binding.btnEnviarPropuestaPro.isEnabled = false
        }
    }

    // Observa los datos de la solicitud y los resultados de Firebase.
    private fun observarViewModel() {

        viewModel.solicitud.observe(this) { solicitud ->

            if (solicitud != null) {
                solicitudActual = solicitud

                val codigoCorto = if (solicitud.idSolicitud.length >= 4) {
                    solicitud.idSolicitud.takeLast(4).uppercase()
                } else {
                    solicitud.idSolicitud.uppercase()
                }

                binding.tvCodigoDetallePro.text = "#SOL-$codigoCorto"

                binding.tvCategoriaDetallePro.text =
                    solicitud.categoryId.ifBlank { "Servicio General" }

                binding.tvTituloDetallePro.text =
                    solicitud.categoryId.ifBlank { "Detalle del Servicio" }

                binding.tvDescripcionDetallePro.text =
                    solicitud.descripcion.ifBlank {
                        "Sin descripción detallada del problema."
                    }

                binding.tvUbicacionDetallePro.text =
                    listOf(solicitud.direccion, solicitud.ciudad)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")

                binding.tvHorarioDetallePro.text =
                    "⏱ Horario solicitado: ${solicitud.fechaServicio} · ${solicitud.horaServicio}"

                binding.tvPresupuestoDetallePro.text =
                    solicitud.presupuesto.ifBlank { "A convenir" }

                cargarNombreCliente(solicitud.clientId)

                if (solicitud.fotoProblema.isNotBlank()) {
                    binding.ivFotoProblemaDetalle.visibility = View.VISIBLE

                    Glide.with(this)
                        .load(solicitud.fotoProblema)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .centerCrop()
                        .into(binding.ivFotoProblemaDetalle)
                } else {
                    binding.ivFotoProblemaDetalle.setImageResource(
                        R.mipmap.ic_launcher
                    )
                }

                // No permitir propuestas a solicitudes ya asignadas o canceladas.
                val disponible = solicitud.estado == "PENDIENTE" &&
                        solicitud.profesionalId.isNullOrBlank()

                binding.btnEnviarPropuestaPro.isEnabled = disponible

                if (!disponible) {
                    binding.tvAyudaPrecioPro.text =
                        "Esta solicitud ya no está disponible para recibir propuestas."
                } else {
                    actualizarAyudaPrecio()
                }

            } else {
                solicitudActual = null
                binding.btnEnviarPropuestaPro.isEnabled = false

                Toast.makeText(
                    this,
                    "No se pudo cargar la información de la solicitud",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Deshabilitar el botón mientras Firebase guarda la propuesta.
        viewModel.enviandoPropuesta.observe(this) { enviando ->
            binding.btnEnviarPropuestaPro.isEnabled = !enviando &&
                    solicitudActual?.let {
                        it.estado == "PENDIENTE" &&
                                it.profesionalId.isNullOrBlank()
                    } == true

            binding.btnEnviarPropuestaPro.text =
                if (enviando) "Enviando propuesta..." else "Enviar propuesta"
        }

        // Mostrar éxito solamente cuando Firebase confirme el guardado.
        viewModel.resultadoPropuesta.observe(this) { resultado ->

            resultado.onSuccess {
                Toast.makeText(
                    this,
                    "¡Propuesta enviada correctamente al cliente!",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }

            resultado.onFailure { error ->
                Toast.makeText(
                    this,
                    error.message ?: "No se pudo enviar la propuesta",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Carga el nombre real del cliente desde Firestore.
    private fun cargarNombreCliente(clientId: String) {

        if (clientId.isBlank()) return

        lifecycleScope.launch {

            val resultado = firestoreRepository.obtenerPerfilUsuario(clientId)

            resultado.onSuccess { usuario ->

                if (usuario != null) {
                    val nombreCompleto =
                        "${usuario.nombre} ${usuario.apellido}".trim()

                    if (nombreCompleto.isNotBlank()) {
                        binding.tvClienteNombreDetallePro.text = nombreCompleto
                    }
                }
            }
        }
    }

    // Configura las opciones del formulario.
    private fun configurarFormularioPropuesta() {

        binding.rgTipoPropuestaPro.setOnCheckedChangeListener { _, _ ->
            actualizarAyudaPrecio()
        }
    }

    // Explica las condiciones del precio según el presupuesto del cliente.
    private fun actualizarAyudaPrecio() {

        val presupuesto = solicitudActual?.presupuesto.orEmpty()

        binding.tvAyudaPrecioPro.text =
            when (binding.rgTipoPropuestaPro.checkedRadioButtonId) {

                R.id.rbAceptarPresupuestoPro -> {
                    if (presupuesto.contains("convenir", ignoreCase = true)) {
                        "El cliente dejó el presupuesto a convenir. Ingresa el precio que cobrarías."
                    } else {
                        "Ingresa un precio exacto dentro del presupuesto del cliente: $presupuesto"
                    }
                }

                R.id.rbNuevoPrecioPro -> {
                    "Ingresa el nuevo precio que deseas ofrecer al cliente."
                }

                else -> {
                    "Selecciona una opción e ingresa el precio que cobrarías."
                }
            }
    }

    private fun configurarBotones() {

        binding.btnRegresarDetallePro.setOnClickListener {
            finish()
        }

        binding.btnEnviarPropuestaPro.setOnClickListener {
            validarYEnviarPropuesta()
        }
    }

    // Valida los campos antes de enviar la propuesta.
    private fun validarYEnviarPropuesta() {

        val solicitud = solicitudActual

        if (solicitud == null) {
            mostrarMensaje("Todavía no se han cargado los datos de la solicitud")
            return
        }

        val solicitudId = idSolicitud

        if (solicitudId.isNullOrBlank()) {
            mostrarMensaje("No se encontró la solicitud")
            return
        }

        val profesionalId = FirebaseAuth.getInstance().currentUser?.uid

        if (profesionalId.isNullOrBlank()) {
            mostrarMensaje("Debes iniciar sesión para enviar una propuesta")
            return
        }

        if (profesionalId == solicitud.clientId) {
            mostrarMensaje("No puedes enviar una propuesta a tu propia solicitud")
            return
        }

        if (solicitud.estado != "PENDIENTE" ||
            !solicitud.profesionalId.isNullOrBlank()
        ) {
            mostrarMensaje("Esta solicitud ya no recibe propuestas")
            return
        }

        val tipoPropuesta = when (
            binding.rgTipoPropuestaPro.checkedRadioButtonId
        ) {
            R.id.rbAceptarPresupuestoPro -> "ACEPTAR_PRESUPUESTO"
            R.id.rbNuevoPrecioPro -> "NUEVO_PRECIO"

            else -> {
                mostrarMensaje("Selecciona el tipo de propuesta")
                return
            }
        }

        val textoPrecio = binding.etPrecioPropuestaPro.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val precio = textoPrecio.replace(",", ".").toDoubleOrNull()

        if (precio == null || !precio.isFinite() || precio <= 0.0) {
            binding.etPrecioPropuestaPro.error = "Ingresa un precio válido"
            binding.etPrecioPropuestaPro.requestFocus()
            return
        }

        // Si acepta el presupuesto, el precio debe estar en el rango indicado.
        if (tipoPropuesta == "ACEPTAR_PRESUPUESTO") {

            val presupuestoTexto = solicitud.presupuesto.trim()

            if (!presupuestoTexto.contains("convenir", ignoreCase = true)) {

                val rango = obtenerRangoPresupuesto(presupuestoTexto)

                if (rango == null) {
                    mostrarMensaje(
                        "No se pudo interpretar el presupuesto del cliente. " +
                                "Selecciona 'Proponer otro precio'."
                    )
                    return
                }

                if (precio < rango.first || precio > rango.second) {
                    binding.etPrecioPropuestaPro.error =
                        "El precio debe estar entre " +
                                formatearPrecio(rango.first) +
                                " y " +
                                formatearPrecio(rango.second)

                    binding.etPrecioPropuestaPro.requestFocus()
                    return
                }
            }
        }

        val disponibilidad = when (
            binding.rgTiempoLlegadaPro.checkedRadioButtonId
        ) {
            R.id.rbLlegada15Pro -> "15 min"
            R.id.rbLlegada30Pro -> "30 min"
            R.id.rbLlegadaConvenioPro -> "A convenir"

            else -> {
                mostrarMensaje("Selecciona en cuánto tiempo puedes llegar")
                return
            }
        }

        val mensaje = binding.etMensajePropuestaPro.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val propuesta = Propuesta(
            solicitudId = solicitudId,
            profesionalId = profesionalId,
            clienteId = solicitud.clientId,
            tipoPropuesta = tipoPropuesta,
            precioPropuesto = precio,
            mensaje = mensaje,
            disponibilidad = disponibilidad,
            estado = "PENDIENTE"
        )

        viewModel.enviarPropuesta(propuesta)
    }

    // Obtiene un rango como "$15 - $30" o "$15.00 – $30.00".
    // También acepta un presupuesto con un único valor.
    private fun obtenerRangoPresupuesto(texto: String): Pair<Double, Double>? {

        val numeros = Regex("""\d+(?:[.,]\d{1,2})?""")
            .findAll(texto)
            .mapNotNull {
                it.value.replace(",", ".").toDoubleOrNull()
            }
            .toList()

        return when (numeros.size) {
            1 -> Pair(numeros[0], numeros[0])
            2 -> Pair(
                minOf(numeros[0], numeros[1]),
                maxOf(numeros[0], numeros[1])
            )
            else -> null
        }
    }

    private fun formatearPrecio(precio: Double): String {
        return String.format(Locale.US, "$%.2f", precio)
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}
