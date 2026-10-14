
package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetallesSolicitudCreadaClienteBinding
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class DetallesSolicitudCreadaClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesSolicitudCreadaClienteBinding

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var idSolicitud = ""
    private var operacionEnCurso = false
    private var solicitudPermiteAceptar = false

    private var listenerPropuestas: ListenerRegistration? = null
    private var listenerSolicitud: ListenerRegistration? = null

    // Actualiza la pantalla después de editar una solicitud.
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

    // ==========================================
    // BOTONES PRINCIPALES
    // ==========================================

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

            if (operacionEnCurso || !solicitudPermiteAceptar) {
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
            if (solicitudPermiteAceptar && !operacionEnCurso) {
                confirmarCancelacion()
            }
        }
    }

    // ==========================================
    // CARGAR DETALLES DE LA SOLICITUD
    // ==========================================

    private fun cargarDetalleSolicitud() {

        listenerSolicitud?.remove()

        listenerSolicitud = firestore.collection("Solicitudes")
            .document(idSolicitud)
            .addSnapshotListener { documento, error ->

                if (isFinishing || isDestroyed) {
                    return@addSnapshotListener
                }

                if (error != null) {
                    mostrarMensaje(
                        "Error al cargar la solicitud: ${error.localizedMessage}"
                    )
                    return@addSnapshotListener
                }

                if (documento == null || !documento.exists()) {
                    mostrarMensaje("No se encontró la solicitud")
                    finish()
                    return@addSnapshotListener
                }

                val uid = auth.currentUser?.uid
                val clienteId = documento.getString("clientId")

                if (uid == null || clienteId != uid) {
                    mostrarMensaje(
                        "No tienes permiso para ver esta solicitud"
                    )
                    finish()
                    return@addSnapshotListener
                }

                val estado = documento.getString("estado")
                    ?: "PENDIENTE"

                val profesionalId = documento.getString("profesionalId")

                val permisoAnterior = solicitudPermiteAceptar

                solicitudPermiteAceptar =
                    estado.uppercase(Locale.ROOT) == "PENDIENTE" &&
                            profesionalId.isNullOrBlank()

                actualizarBotonesSolicitud()

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
                    "FINALIZADA", "FINALIZADO", "COMPLETADA" ->
                        "Finalizada"
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

                // Si cambió la posibilidad de aceptar propuestas,
                // actualizamos los botones de todas las tarjetas.
                if (permisoAnterior != solicitudPermiteAceptar) {
                    escucharPropuestasRecibidas()
                } else if (listenerPropuestas == null) {
                    escucharPropuestasRecibidas()
                }
            }
    }

    private fun actualizarBotonesSolicitud() {

        val visibilidad =
            if (solicitudPermiteAceptar) View.VISIBLE else View.GONE

        binding.btnEditarSolicitud.visibility = visibilidad
        binding.btnCancelarSolicitud.visibility = visibilidad
    }

    // ==========================================
    // CANCELAR SOLICITUD
    // ==========================================

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

            val estado = documento.getString("estado")
            val profesionalId = documento.getString("profesionalId")

            if (estado != "PENDIENTE" ||
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

        }.addOnSuccessListener {

            operacionEnCurso = false
            mostrarMensaje("Solicitud cancelada correctamente")

        }.addOnFailureListener { error ->

            operacionEnCurso = false

            mostrarMensaje(
                error.localizedMessage
                    ?: "No se pudo cancelar la solicitud"
            )
        }
    }

    // ==========================================
    // ESCUCHAR PROPUESTAS EN TIEMPO REAL
    // ==========================================

    private fun escucharPropuestasRecibidas() {

        val uid = auth.currentUser?.uid ?: return

        listenerPropuestas?.remove()
        listenerPropuestas = null

        binding.tvEstadoPropuestasDetalle.visibility = View.VISIBLE
        binding.tvEstadoPropuestasDetalle.text = "Cargando propuestas..."

        binding.contenedorPropuestasDetalle.removeAllViews()
        binding.contenedorPropuestasDetalle.visibility = View.GONE

        listenerPropuestas = firestore.collection("Propuestas")
            .whereEqualTo("solicitudId", idSolicitud)
            .whereEqualTo("clienteId", uid)
            .addSnapshotListener { documentos, error ->

                if (isFinishing || isDestroyed) {
                    return@addSnapshotListener
                }

                if (error != null) {

                    binding.tvEstadoPropuestasDetalle.text =
                        "No se pudieron cargar las propuestas."

                    binding.contenedorPropuestasDetalle.visibility = View.GONE

                    return@addSnapshotListener
                }

                val propuestas = documentos?.documents.orEmpty()

                binding.contenedorPropuestasDetalle.removeAllViews()

                if (propuestas.isEmpty()) {

                    binding.tvEstadoPropuestasDetalle.text =
                        "Todavía no has recibido propuestas de profesionales."

                    binding.contenedorPropuestasDetalle.visibility = View.GONE

                    return@addSnapshotListener
                }

                binding.tvEstadoPropuestasDetalle.text =
                    if (propuestas.size == 1) {
                        "Has recibido 1 propuesta."
                    } else {
                        "Has recibido ${propuestas.size} propuestas."
                    }

                binding.contenedorPropuestasDetalle.visibility = View.VISIBLE

                propuestas.forEach { documento ->

                    val profesionalId =
                        documento.getString("profesionalId").orEmpty()

                    val precio =
                        documento.getDouble("precioPropuesto") ?: 0.0

                    val disponibilidad =
                        documento.getString("disponibilidad").orEmpty()

                    val mensaje =
                        documento.getString("mensaje").orEmpty()

                    val estado =
                        documento.getString("estado") ?: "PENDIENTE"

                    agregarTarjetaPropuesta(
                        profesionalId = profesionalId,
                        precio = precio,
                        disponibilidad = disponibilidad,
                        mensaje = mensaje,
                        estado = estado
                    )
                }
            }
    }

    // ==========================================
    // TARJETA DE CADA PROPUESTA
    // ==========================================

    private fun agregarTarjetaPropuesta(
        profesionalId: String,
        precio: Double,
        disponibilidad: String,
        mensaje: String,
        estado: String
    ) {

        val tarjeta = MaterialCardView(this).apply {

            radius = dp(12).toFloat()
            cardElevation = dp(2).toFloat()

            setCardBackgroundColor(
                ContextCompat.getColor(
                    this@DetallesSolicitudCreadaClienteActivity,
                    R.color.ui_surface
                )
            )

            strokeWidth = dp(1)

            strokeColor = ContextCompat.getColor(
                this@DetallesSolicitudCreadaClienteActivity,
                R.color.ui_text_secondary
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        // Encabezado con fotografía y datos del profesional.
        val encabezado = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val fotoProfesional = ImageView(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                dp(58),
                dp(58)
            ).apply {
                marginEnd = dp(12)
            }

            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.mipmap.ic_launcher)

            contentDescription = "Fotografía del profesional"
        }

        val informacionProfesional = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val nombreProfesional = crearTextoPropuesta(
            texto = "Cargando profesional...",
            tamano = 16f,
            negrita = true
        )

        val verificacionProfesional = crearTextoPropuesta(
            texto = "Consultando verificación...",
            tamano = 12f,
            negrita = false
        )

        informacionProfesional.addView(nombreProfesional)
        informacionProfesional.addView(verificacionProfesional)

        encabezado.addView(fotoProfesional)
        encabezado.addView(informacionProfesional)

        contenido.addView(encabezado)

        // Precio ofrecido.
        contenido.addView(
            crearTextoPropuesta(
                texto = "Precio ofrecido: ${
                    String.format(Locale.US, "$%.2f", precio)
                }",
                tamano = 15f,
                negrita = true
            )
        )

        // Disponibilidad del profesional.
        contenido.addView(
            crearTextoPropuesta(
                texto = "Tiempo de llegada: ${
                    disponibilidad.ifBlank { "No especificado" }
                }",
                tamano = 13f,
                negrita = false
            )
        )

        // Mensaje opcional.
        if (mensaje.isNotBlank()) {

            contenido.addView(
                crearTextoPropuesta(
                    texto = "Mensaje: $mensaje",
                    tamano = 13f,
                    negrita = false
                )
            )
        }

        // Estado de la propuesta.
        val estadoFormateado = when (
            estado.uppercase(Locale.ROOT)
        ) {
            "PENDIENTE" -> "Pendiente"
            "ACEPTADA" -> "Aceptada"
            "RECHAZADA" -> "Rechazada"
            else -> estado.replace("_", " ")
        }

        contenido.addView(
            crearTextoPropuesta(
                texto = "Estado: $estadoFormateado",
                tamano = 13f,
                negrita = false
            )
        )

        // ==========================================
        // BOTONES ACEPTAR Y RECHAZAR
        // ==========================================

        val propuestaPendiente =
            estado.uppercase(Locale.ROOT) == "PENDIENTE"

        if (propuestaPendiente && solicitudPermiteAceptar) {

            // Botón Aceptar.
            val btnAceptar = Button(this).apply {

                text = "Aceptar propuesta"
                isAllCaps = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(48)
                ).apply {
                    topMargin = dp(12)
                }

                setOnClickListener {
                    confirmarAceptarPropuesta(profesionalId)
                }
            }

            contenido.addView(btnAceptar)

            // Botón Rechazar.
            val btnRechazar = Button(this).apply {

                text = "Rechazar propuesta"
                isAllCaps = false

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(48)
                ).apply {
                    topMargin = dp(8)
                }

                setOnClickListener {
                    confirmarRechazoPropuesta(profesionalId)
                }
            }

            contenido.addView(btnRechazar)
        }

        // Las demás propuestas siguen visibles aunque
        // la solicitud ya tenga un profesional asignado.
        if (propuestaPendiente && !solicitudPermiteAceptar) {

            contenido.addView(
                crearTextoPropuesta(
                    texto = "Esta solicitud ya no admite propuestas.",
                    tamano = 12f,
                    negrita = false
                )
            )
        }

        tarjeta.addView(contenido)

        binding.contenedorPropuestasDetalle.addView(tarjeta)

        // ==========================================
        // CARGAR DATOS REALES DEL PROFESIONAL
        // ==========================================

        if (profesionalId.isNotBlank()) {

            firestore.collection("Usuario")
                .document(profesionalId)
                .get()
                .addOnSuccessListener { usuario ->

                    if (isFinishing || isDestroyed) {
                        return@addOnSuccessListener
                    }

                    val nombre = usuario.getString("nombre").orEmpty()
                    val apellido = usuario.getString("apellido").orEmpty()

                    val fotoPerfil =
                        usuario.getString("fotoPerfil").orEmpty()

                    val estadoVerificacion =
                        usuario.getString("estadoVerificacion")
                            .orEmpty()
                            .uppercase(Locale.ROOT)

                    nombreProfesional.text =
                        "$nombre $apellido".trim()
                            .ifBlank { "Profesional" }

                    verificacionProfesional.text =
                        when (estadoVerificacion) {
                            "VERIFICADO", "APROBADO" ->
                                "✓ Verificado"
                            "PENDIENTE" ->
                                "Verificación pendiente"
                            else ->
                                "Sin verificar"
                        }

                    if (fotoPerfil.isNotBlank()) {

                        Glide.with(this)
                            .load(fotoPerfil)
                            .apply(
                                RequestOptions()
                                    .transform(CircleCrop())
                                    .placeholder(R.mipmap.ic_launcher)
                                    .error(R.mipmap.ic_launcher)
                            )
                            .into(fotoProfesional)

                    } else {

                        fotoProfesional.setImageResource(
                            R.mipmap.ic_launcher
                        )
                    }
                }
                .addOnFailureListener {

                    if (!isFinishing && !isDestroyed) {

                        nombreProfesional.text = "Profesional"

                        verificacionProfesional.text =
                            "Verificación no disponible"
                    }
                }

        } else {

            nombreProfesional.text = "Profesional"
            verificacionProfesional.text = "Sin verificar"
        }
    }

    // ACEPTAR PROPUESTA
    private fun confirmarAceptarPropuesta(profesionalId: String) {

        if (operacionEnCurso) return

        if (!solicitudPermiteAceptar || profesionalId.isBlank()) {
            mostrarMensaje("Esta propuesta ya no se puede aceptar")
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Aceptar propuesta")
            .setMessage(
                "¿Deseas aceptar esta propuesta y asignar el trabajo " +
                        "a este profesional? Después no podrás aceptar " +
                        "otra propuesta para la misma solicitud."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Sí, aceptar") { _, _ ->
                aceptarPropuesta(profesionalId)
            }
            .show()
    }

    private fun aceptarPropuesta(profesionalId: String) {

        val uid = auth.currentUser?.uid ?: run {
            mostrarMensaje("Debes iniciar sesión")
            return
        }

        if (operacionEnCurso || !solicitudPermiteAceptar) return

        operacionEnCurso = true

        val idPropuesta = "${idSolicitud}_${profesionalId}"

        val referenciaPropuesta = firestore.collection("Propuestas")
            .document(idPropuesta)

        val referenciaSolicitud = firestore.collection("Solicitudes")
            .document(idSolicitud)

        firestore.runTransaction { transaccion ->

            // Leer los dos documentos antes de modificarlos.
            val solicitud = transaccion.get(referenciaSolicitud)
            val propuesta = transaccion.get(referenciaPropuesta)

            if (!solicitud.exists() || !propuesta.exists()) {
                throw IllegalStateException(
                    "La solicitud o la propuesta ya no existe"
                )
            }

            if (solicitud.getString("clientId") != uid ||
                propuesta.getString("clienteId") != uid ||
                propuesta.getString("solicitudId") != idSolicitud ||
                propuesta.getString("profesionalId") != profesionalId
            ) {
                throw IllegalStateException(
                    "No tienes permiso para aceptar esta propuesta"
                )
            }

            if (solicitud.getString("estado") != "PENDIENTE" ||
                !solicitud.getString("profesionalId").isNullOrBlank()
            ) {
                throw IllegalStateException(
                    "Esta solicitud ya tiene un profesional asignado"
                )
            }

            if (propuesta.getString("estado") != "PENDIENTE") {
                throw IllegalStateException(
                    "Esta propuesta ya fue respondida"
                )
            }

            // Actualizar la propuesta elegida.
            transaccion.update(
                referenciaPropuesta,
                "estado",
                "ACEPTADA"
            )

            // Asignar el profesional a la solicitud.
            transaccion.update(
                referenciaSolicitud,
                mapOf(
                    "estado" to "ASIGNADA",
                    "profesionalId" to profesionalId,
                    "fechaActualizacion" to FieldValue.serverTimestamp()
                )
            )

            null

        }.addOnSuccessListener {

            operacionEnCurso = false
            solicitudPermiteAceptar = false

            actualizarBotonesSolicitud()

            mostrarMensaje("Propuesta aceptada correctamente")

            // Los listeners actualizarán los datos automáticamente.
            escucharPropuestasRecibidas()

        }.addOnFailureListener { error ->

            operacionEnCurso = false

            mostrarMensaje(
                error.localizedMessage
                    ?: "No se pudo aceptar la propuesta"
            )
        }
    }

    // RECHAZAR PROPUESTA
    private fun confirmarRechazoPropuesta(profesionalId: String) {

        if (operacionEnCurso) return

        if (!solicitudPermiteAceptar || profesionalId.isBlank()) {
            mostrarMensaje("Esta propuesta ya no se puede rechazar")
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Rechazar propuesta")
            .setMessage(
                "¿Estás seguro de que deseas rechazar esta propuesta? " +
                        "El profesional ya no podrá ser seleccionado " +
                        "mediante esta propuesta."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Sí, rechazar") { _, _ ->
                rechazarPropuesta(profesionalId)
            }
            .show()
    }

    private fun rechazarPropuesta(profesionalId: String) {

        val uid = auth.currentUser?.uid ?: run {
            mostrarMensaje("Debes iniciar sesión")
            return
        }

        if (operacionEnCurso || !solicitudPermiteAceptar) return

        operacionEnCurso = true

        val idPropuesta = "${idSolicitud}_${profesionalId}"

        val referencia = firestore.collection("Propuestas")
            .document(idPropuesta)

        firestore.runTransaction { transaccion ->

            val documento = transaccion.get(referencia)

            if (!documento.exists()) {
                throw IllegalStateException(
                    "La propuesta ya no existe"
                )
            }

            if (documento.getString("clienteId") != uid ||
                documento.getString("solicitudId") != idSolicitud ||
                documento.getString("profesionalId") != profesionalId
            ) {
                throw IllegalStateException(
                    "No tienes permiso para rechazar esta propuesta"
                )
            }

            if (documento.getString("estado") != "PENDIENTE") {
                throw IllegalStateException(
                    "Esta propuesta ya fue respondida"
                )
            }

            transaccion.update(
                referencia,
                "estado",
                "RECHAZADA"
            )

            null

        }.addOnSuccessListener {

            operacionEnCurso = false

            mostrarMensaje("Propuesta rechazada correctamente")

            // El listener actualizará automáticamente la tarjeta.

        }.addOnFailureListener { error ->

            operacionEnCurso = false

            mostrarMensaje(
                error.localizedMessage
                    ?: "No se pudo rechazar la propuesta"
            )
        }
    }

    // FUNCIONES AUXILIARES

    private fun crearTextoPropuesta(
        texto: String,
        tamano: Float,
        negrita: Boolean
    ): TextView {

        return TextView(this).apply {

            this.text = texto
            textSize = tamano

            setTextColor(
                ContextCompat.getColor(
                    this@DetallesSolicitudCreadaClienteActivity,
                    R.color.ui_text
                )
            )

            if (negrita) {
                setTypeface(null, Typeface.BOLD)
            }

            setPadding(0, 0, 0, dp(8))
        }
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    // Elimina los listeners al cerrar la pantalla.
    override fun onDestroy() {

        listenerSolicitud?.remove()
        listenerPropuestas?.remove()

        listenerSolicitud = null
        listenerPropuestas = null

        super.onDestroy()
    }
}
