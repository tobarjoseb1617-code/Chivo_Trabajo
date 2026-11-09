package com.cherodevscode.chivo_trabajo.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.databinding.ItemSolicitudAdminBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SolicitudesAdminAdapter(
    private val onVerDetalles: (Solicitud) -> Unit
) : RecyclerView.Adapter<SolicitudesAdminAdapter.SolicitudViewHolder>() {

    private var listaSolicitudes = emptyList<Solicitud>()

    fun actualizarLista(nuevaLista: List<Solicitud>) {
        listaSolicitudes = nuevaLista
        notifyDataSetChanged()
    }

    inner class SolicitudViewHolder(
        private val binding: ItemSolicitudAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun vincular(solicitud: Solicitud) {

            binding.tvCategoriaSolicitudAdmin.text =
                solicitud.categoryId

            binding.tvDescripcionSolicitudAdmin.text =
                solicitud.descripcion

            binding.tvCiudadSolicitudAdmin.text =
                "Ciudad: ${solicitud.ciudad}"

            val formatoFecha = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

            binding.tvFechaSolicitudAdmin.text =
                "Fecha: ${formatoFecha.format(solicitud.fechaCreacion.toDate())}"

            val estado = solicitud.estado.uppercase(Locale.ROOT)

            binding.tvEstadoSolicitudAdmin.text = when (estado) {
                "PENDIENTE" -> "Pendiente"
                "ASIGNADA" -> "Profesional asignado"
                "COMPLETADA" -> "Finalizada"
                "CANCELADA" -> "Cancelada"
                else -> estado.replace("_", " ")
            }

            val (colorTexto, fondo) = when (estado) {

                "PENDIENTE" -> Pair(
                    R.color.ui_warning,
                    R.drawable.bg_m3_warning
                )

                "ASIGNADA" -> Pair(
                    R.color.ui_primary,
                    R.drawable.bg_estado_asignada
                )

                "COMPLETADA" -> Pair(
                    R.color.ui_success,
                    R.drawable.bg_m3_success
                )

                "CANCELADA" -> Pair(
                    R.color.ui_error,
                    R.drawable.bg_m3_error
                )

                else -> Pair(
                    R.color.ui_primary,
                    R.drawable.bg_m3_info
                )
            }

            binding.tvEstadoSolicitudAdmin.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    colorTexto
                )
            )

            binding.tvEstadoSolicitudAdmin.setBackgroundResource(fondo)

            binding.btnDetallesSolicitudAdmin.setOnClickListener {
                onVerDetalles(solicitud)
            }

            binding.root.setOnClickListener {
                onVerDetalles(solicitud)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolicitudViewHolder {

        val binding = ItemSolicitudAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SolicitudViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SolicitudViewHolder,
        position: Int
    ) {
        holder.vincular(listaSolicitudes[position])
    }

    override fun getItemCount(): Int = listaSolicitudes.size
}
