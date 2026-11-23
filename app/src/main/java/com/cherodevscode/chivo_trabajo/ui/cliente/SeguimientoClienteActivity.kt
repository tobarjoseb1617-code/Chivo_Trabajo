package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.model.DetalleServicioCliente
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivitySeguimientoClienteBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import java.util.Locale

class SeguimientoClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguimientoClienteBinding
    private lateinit var viewModel: SeguimientoClienteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySeguimientoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SeguimientoClienteViewModel::class.java]

        configurarBotones()
        observarViewModel()
        cargarDatos()
    }

    private fun cargarDatos() {
        val idSolicitud = intent.getStringExtra("EXTRA_SOLICITUD_ID")
            ?: intent.getStringExtra("idSolicitud")
            ?: ""

        val clienteId = AuthRepository(this).usuarioActual()?.uid

        if (idSolicitud.isBlank() || clienteId.isNullOrBlank()) {
            Toast.makeText(
                this,
                "No se pudo identificar la solicitud.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        viewModel.cargarDatosSolicitud(idSolicitud, clienteId)
    }

    private fun configurarBotones() {

        binding.btnRegresarSeguimientoCliente.setOnClickListener {
            finish()
        }

        binding.btnPerfilTop.setOnClickListener {
            startActivity(
                Intent(this, ConfiguracionPerfilActivity::class.java)
            )
        }

        binding.btnChatConProfesional.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("EXTRA_IS_PRO", false)
            }
            startActivity(intent)
        }

        binding.btnVerMapaCliente.setOnClickListener {
            Toast.makeText(
                this,
                "La función de mapa todavía no está disponible.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observarViewModel() {

        viewModel.detalleServicio.observe(this) { detalle ->
            if (detalle != null) {
                mostrarSolicitud(detalle)
                mostrarPropuesta(detalle)
                mostrarProfesional(detalle)
            }
        }

        viewModel.mensajeError.observe(this) { mensaje ->
            if (!mensaje.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    mensaje,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mostrarSolicitud(detalle: DetalleServicioCliente) {

        val solicitud = detalle.solicitud

        binding.tvDescripcionSolicitud.text =
            solicitud.descripcion.ifBlank { "Sin descripción" }

        binding.tvFechaServicio.text =
            "Fecha: ${solicitud.fechaServicio.ifBlank { "No especificada" }}"

        binding.tvHoraServicio.text =
            "Hora: ${solicitud.horaServicio.ifBlank { "No especificada" }}"

        binding.tvDireccionSolicitud.text =
            "Dirección: ${solicitud.direccion.ifBlank { "No especificada" }}"

        binding.tvCiudadSolicitud.text =
            "Ciudad: ${solicitud.ciudad.ifBlank { "No especificada" }}"

        binding.tvCategoriaSolicitud.text =
            solicitud.categoryId.ifBlank { "Categoría no especificada" }

        binding.tvEstadoServicio.text =
            formatearEstado(solicitud.estado)

        actualizarProgresoServicio(solicitud.estado)

        // Se muestra el mismo PIN guardado al crear la solicitud.
        val pin = solicitud.pinSeguridad

        binding.layoutPinSeguimientoCliente.isVisible =
            pin.isNotBlank()

        binding.tvPinCodigoCliente.text =
            if (pin.isNotBlank()) "#$pin" else ""

        binding.layoutAlertaLlegada.isVisible = false
        binding.layoutAlertaPinVerificado.isVisible = false

        // Fotografías adjuntadas por el cliente al crear la solicitud.
        cargarFotosProblema(
            solicitud.fotosProblema,
            solicitud.fotoProblema
        )
    }

    private fun actualizarProgresoServicio(estado: String) {

        val estadoNormalizado = estado
            .trim()
            .uppercase(Locale.ROOT)
            .replace(" ", "_")

        val pasoActual = when (estadoNormalizado) {
            "PENDIENTE" -> 1
            "ASIGNADA", "ACEPTADA" -> 2
            "EN_CURSO", "EN_PROCESO" -> 3
            "COMPLETADA", "FINALIZADA", "FINALIZADO" -> 4
            "CANCELADA" -> 0
            else -> 1
        }

        val colorCompletado = ContextCompat.getColor(
            this,
            R.color.ui_success
        )

        val colorActivo = ContextCompat.getColor(
            this,
            R.color.ui_primary
        )

        val colorPendiente = ContextCompat.getColor(
            this,
            R.color.ui_outline
        )

        val colorTextoPendiente = ContextCompat.getColor(
            this,
            R.color.ui_text_secondary
        )

        val colorBlanco = ContextCompat.getColor(
            this,
            android.R.color.white
        )

        val pasos = listOf(
            binding.tvIconoPasoSolicitud,
            binding.tvIconoPasoAsignado,
            binding.tvIconoPasoEnCurso,
            binding.tvIconoPasoFinalizado
        )

        pasos.forEachIndexed { index, icono ->

            val numeroPaso = index + 1

            when {
                pasoActual == 4 || numeroPaso < pasoActual -> {
                    icono.text = "✓"
                    icono.backgroundTintList =
                        ColorStateList.valueOf(colorCompletado)
                    icono.setTextColor(colorBlanco)
                }

                numeroPaso == pasoActual -> {
                    icono.text = numeroPaso.toString()
                    icono.backgroundTintList =
                        ColorStateList.valueOf(colorActivo)
                    icono.setTextColor(colorBlanco)
                }

                else -> {
                    icono.text = numeroPaso.toString()
                    icono.backgroundTintList =
                        ColorStateList.valueOf(colorPendiente)
                    icono.setTextColor(colorTextoPendiente)
                }
            }
        }

        val lineas = listOf(
            binding.lineaPasoSolicitud,
            binding.lineaPasoAsignado,
            binding.lineaPasoEnCurso
        )

        lineas.forEachIndexed { index, linea ->

            val pasoCompletado = pasoActual == 4 ||
                    pasoActual > index + 1

            linea.setBackgroundColor(
                if (pasoCompletado) colorCompletado
                else colorPendiente
            )
        }

        binding.tvPasoProgreso.text = when (pasoActual) {
            0 -> "Cancelado"
            else -> "Paso $pasoActual de 4"
        }
    }

    private fun mostrarPropuesta(detalle: DetalleServicioCliente) {

        val propuesta = detalle.propuesta

        binding.layoutPropuestaCliente.isVisible =
            propuesta != null

        if (propuesta == null) {
            return
        }

        binding.tvPrecioPropuesta.text =
            String.format(
                Locale.getDefault(),
                "$%.2f",
                propuesta.precioPropuesto
            )

        binding.tvDisponibilidadPropuesta.text =
            propuesta.disponibilidad.ifBlank {
                "No especificada"
            }

        binding.tvMensajePropuesta.text =
            propuesta.mensaje.ifBlank {
                "El profesional no agregó un mensaje."
            }
    }

    private fun mostrarProfesional(detalle: DetalleServicioCliente) {

        val usuario = detalle.usuarioProfesional
        val profesional = detalle.profesional

        if (usuario == null) {
            binding.tvNombreProfesional.text =
                "Profesional no disponible"

            binding.tvCalificacionProfesional.text =
                "Información no disponible"

            binding.ivFotoProfesional.setImageResource(
                R.mipmap.ic_launcher
            )

            return
        }

        val nombreCompleto =
            "${usuario.nombre} ${usuario.apellido}".trim()

        binding.tvNombreProfesional.text =
            nombreCompleto.ifBlank {
                "Profesional asignado"
            }

        if (usuario.fotoPerfil.isNotBlank()) {
            Glide.with(this)
                .load(usuario.fotoPerfil)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(binding.ivFotoProfesional)
        } else {
            binding.ivFotoProfesional.setImageResource(
                R.mipmap.ic_launcher
            )
        }

        val especialidad = profesional?.especialidad
            ?.takeIf { it.isNotBlank() }
            ?: "Especialidad no especificada"

        val informacionCalificacion = if (profesional != null) {
            if (profesional.cantidadCalificaciones > 0) {
                String.format(
                    Locale.getDefault(),
                    "⭐ %.1f (%d calificaciones)",
                    profesional.calificacionPromedio,
                    profesional.cantidadCalificaciones
                )
            } else {
                "Sin calificaciones"
            }
        } else {
            "Calificación no disponible"
        }

        binding.tvCalificacionProfesional.text =
            "Especialidad: $especialidad\n$informacionCalificacion"
    }

    private fun cargarFotosProblema(
        fotos: List<String>,
        fotoPrincipal: String
    ) {
        val listaFotos = if (fotos.isNotEmpty()) {
            fotos.filter { it.isNotBlank() }
        } else if (fotoPrincipal.isNotBlank()) {
            listOf(fotoPrincipal)
        } else {
            emptyList()
        }

        binding.layoutFotosProblemaCliente.isVisible =
            listaFotos.isNotEmpty()

        cargarImagen(
            listaFotos.getOrNull(0),
            binding.ivFotoAntesCliente
        )

        cargarImagen(
            listaFotos.getOrNull(1),
            binding.ivFotoDespuesCliente
        )
    }

    private fun cargarImagen(
        url: String?,
        imageView: ImageView
    ) {
        if (url.isNullOrBlank()) {
            imageView.isVisible = false
            return
        }

        imageView.isVisible = true

        Glide.with(this)
            .load(url)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .centerCrop()
            .into(imageView)
    }

    private fun formatearEstado(estado: String): String {

        val estadoNormalizado = estado
            .trim()
            .uppercase(Locale.ROOT)
            .replace(" ", "_")

        return when (estadoNormalizado) {
            "PENDIENTE" -> "Solicitud creada"

            "ASIGNADA", "ACEPTADA" ->
                "Profesional asignado"

            "EN_CURSO", "EN_PROCESO" ->
                "Servicio en curso"

            "COMPLETADA", "FINALIZADA", "FINALIZADO" ->
                "Servicio finalizado"

            "CANCELADA" ->
                "Solicitud cancelada"

            else -> estado.replace("_", " ")
                .lowercase(Locale.ROOT)
                .replaceFirstChar { it.uppercase() }
        }
    }
}
