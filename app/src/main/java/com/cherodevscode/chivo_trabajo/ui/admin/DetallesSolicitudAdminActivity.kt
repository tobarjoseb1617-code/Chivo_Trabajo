package com.cherodevscode.chivo_trabajo.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetallesSolicitudAdminBinding
import java.text.SimpleDateFormat
import java.util.Locale
import com.cherodevscode.chivo_trabajo.R

class DetallesSolicitudAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallesSolicitudAdminBinding
    private val viewModel: DetallesSolicitudAdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetallesSolicitudAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVolverDetallesAdmin.setOnClickListener {
            finish()
        }

        observarDetalles()

        val idSolicitud = intent.getStringExtra("idSolicitud")

        if (idSolicitud.isNullOrBlank()) {
            Toast.makeText(this, "No se recibió la solicitud", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.cargarDetalles(idSolicitud)
    }

    private fun observarDetalles() {
        viewModel.detalles.observe(this) { detalles ->
            val solicitud = detalles.solicitud

            binding.tvIdSolicitudAdminDetalle.text =
                "Solicitud: ${solicitud.idSolicitud}"

            binding.tvEstadoSolicitudAdminDetalle.text = solicitud.estado.uppercase()

            when (solicitud.estado.trim().uppercase()) {
                "PENDIENTE" -> {
                    binding.tvEstadoSolicitudAdminDetalle.setBackgroundResource(
                        R.drawable.bg_m3_warning
                    )
                    binding.tvEstadoSolicitudAdminDetalle.setTextColor(
                        getColor(R.color.ui_warning)
                    )
                }

                "ASIGNADA" -> {
                    binding.tvEstadoSolicitudAdminDetalle.setBackgroundResource(
                        R.drawable.bg_estado_asignada
                    )
                    binding.tvEstadoSolicitudAdminDetalle.setTextColor(
                        getColor(R.color.ui_primary)
                    )
                }

                "CANCELADA" -> {
                    binding.tvEstadoSolicitudAdminDetalle.setBackgroundResource(
                        R.drawable.bg_m3_error
                    )
                    binding.tvEstadoSolicitudAdminDetalle.setTextColor(
                        getColor(R.color.ui_error)
                    )
                }

                "COMPLETADA" -> {
                    binding.tvEstadoSolicitudAdminDetalle.setBackgroundResource(
                        R.drawable.bg_m3_success
                    )
                    binding.tvEstadoSolicitudAdminDetalle.setTextColor(
                        getColor(R.color.ui_success)
                    )
                }
            }

            binding.tvTituloSolicitudAdminDetalle.text =
                solicitud.categoryId

            binding.tvFechaCreacionAdminDetalle.text =
                "Creada: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(solicitud.fechaCreacion.toDate())}"

            binding.tvNombreClienteAdminDetalle.text =
                detalles.nombreCliente

            binding.tvCategoriaAdminDetalle.text =
                solicitud.categoryId

            binding.tvDescripcionAdminDetalle.text =
                solicitud.descripcion.ifBlank { "Sin descripción" }

            val ubicacion = listOf(
                solicitud.direccion,
                solicitud.ciudad
            ).filter { it.isNotBlank() }.joinToString(", ")

            binding.tvUbicacionAdminDetalle.text =
                ubicacion.ifBlank { "Ubicación no disponible" }

            val fechaServicio = listOf(
                solicitud.fechaServicio,
                solicitud.horaServicio
            ).filter { it.isNotBlank() }.joinToString(" - ")

            binding.tvFechaServicioAdminDetalle.text =
                fechaServicio.ifBlank { "No especificada" }

            binding.tvPresupuestoAdminDetalle.text =
                solicitud.presupuesto.ifBlank { "No especificado" }

            binding.tvProfesionalAdminDetalle.text =
                detalles.nombreProfesional

            val foto = solicitud.fotoProblema.ifBlank {
                solicitud.fotosProblema.firstOrNull().orEmpty()
            }

            if (foto.isNotBlank()) {
                binding.ivFotoProblemaAdminDetalle.visibility = View.VISIBLE
                binding.tvSinFotoAdminDetalle.visibility = View.GONE

                Glide.with(this)
                    .load(foto)
                    .into(binding.ivFotoProblemaAdminDetalle)
            } else {
                binding.ivFotoProblemaAdminDetalle.visibility = View.GONE
                binding.tvSinFotoAdminDetalle.visibility = View.VISIBLE
            }
        }


        viewModel.error.observe(this) { mensaje ->
            if (!mensaje.isNullOrBlank()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }
}