
package com.cherodevscode.chivo_trabajo.ui.cliente

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ItemSolicitudClienteBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class SolicitudesClienteAdapter(
    private val onVerDetalles: (String) -> Unit
) : RecyclerView.Adapter<SolicitudesClienteAdapter.SolicitudViewHolder>() {

    private val solicitudes = mutableListOf<DocumentSnapshot>()

    inner class SolicitudViewHolder(
        val binding: ItemSolicitudClienteBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolicitudViewHolder {

        val binding = ItemSolicitudClienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SolicitudViewHolder(binding)
    }

    override fun getItemCount(): Int = solicitudes.size

    override fun onBindViewHolder(
        holder: SolicitudViewHolder,
        position: Int
    ) {
        val documento = solicitudes[position]
        val binding = holder.binding

        val idSolicitud =
            documento.getString("idSolicitud")
                ?.takeIf { it.isNotBlank() }
                ?: documento.id

        val descripcion =
            documento.getString("descripcion") ?: "Sin descripción"

        val categoria =
            documento.getString("categoryId") ?: "Sin categoría"

        val direccion =
            documento.getString("direccion") ?: ""

        val ciudad =
            documento.getString("ciudad") ?: ""

        val presupuesto =
            documento.getString("presupuesto") ?: "No especificado"

        val estado =
            documento.getString("estado")
                ?.uppercase(Locale.ROOT)
                ?: "PENDIENTE"

        val fechaCreacion =
            documento.getTimestamp("fechaCreacion")

        binding.tvIdSolicitudItem.text = "#SOL-${idSolicitud.take(8)}"
        binding.tvDescripcionSolicitudItem.text = descripcion
        binding.tvCategoriaSolicitudItem.text = categoria
        cargarProfesionalAsignado(documento, binding.tvProfesionalSolicitudItem)

        binding.tvUbicacionSolicitudItem.text =
            listOf(direccion, ciudad)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(", ")
                .ifBlank { "Ubicación no especificada" }

        binding.tvPresupuestoSolicitudItem.text = presupuesto

        binding.tvFechaSolicitudItem.text =
            formatearFecha(fechaCreacion)

        binding.tvEstadoSolicitudItem.text = when (estado) {
            "PENDIENTE" -> "Pendiente"
            "ABIERTA", "PUBLICADA" -> "Abierta"
            "EN_CURSO", "EN CURSO", "EN_PROCESO", "EN PROCESO" -> "En curso"
            "ACEPTADA", "ASIGNADA" -> "Profesional asignado"
            "FINALIZADA", "FINALIZADO", "COMPLETADA" -> "Finalizada"
            "CANCELADA" -> "Cancelada"
            else -> estado.replace("_", " ")
        }

        val contexto = binding.root.context

        val (colorTexto, fondo) = when (estado) {
            "PENDIENTE" -> Pair(
                R.color.ui_warning,
                R.drawable.bg_m3_warning
            )

            "FINALIZADA", "FINALIZADO", "COMPLETADA" -> Pair(
                R.color.ui_success,
                R.drawable.bg_m3_success
            )

            else -> Pair(
                R.color.ui_primary,
                R.drawable.bg_m3_info
            )
        }

        binding.tvEstadoSolicitudItem.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                contexto,
                colorTexto
            )
        )

        binding.tvEstadoSolicitudItem.setBackgroundResource(fondo)

        binding.btnVerDetalleSolicitudItem.setOnClickListener {
            onVerDetalles(documento.id)
        }
    }

    fun actualizarSolicitudes(nuevasSolicitudes: List<DocumentSnapshot>) {
        solicitudes.clear()
        solicitudes.addAll(nuevasSolicitudes)
        notifyDataSetChanged()
    }

    private fun cargarProfesionalAsignado(
        solicitud: DocumentSnapshot,
        etiqueta: android.widget.TextView
    ) {
        val profesionalId = solicitud.getString("profesionalId").orEmpty()
        val estado = solicitud.getString("estado")?.uppercase(Locale.ROOT).orEmpty()
        val tieneAsignacion = profesionalId.isNotBlank() && estado != "CANCELADA"

        etiqueta.tag = profesionalId
        etiqueta.visibility = if (tieneAsignacion) android.view.View.VISIBLE else android.view.View.GONE
        etiqueta.text = if (tieneAsignacion) "Profesional asignado: Cargando..." else ""
        if (!tieneAsignacion) return

        FirebaseFirestore.getInstance().collection("Usuario").document(profesionalId).get()
            .addOnSuccessListener { usuario ->
                if (etiqueta.tag != profesionalId || !usuario.exists()) return@addOnSuccessListener
                val nombre = listOf(
                    usuario.getString("nombre").orEmpty(),
                    usuario.getString("apellido").orEmpty()
                ).filter { it.isNotBlank() }.joinToString(" ")
                etiqueta.text = if (nombre.isNotBlank()) "Profesional: $nombre" else "Profesional asignado"
            }
            .addOnFailureListener {
                if (etiqueta.tag == profesionalId) etiqueta.text = "Profesional asignado"
            }
    }

    private fun formatearFecha(timestamp: Timestamp?): String {
        if (timestamp == null) {
            return "Fecha no disponible"
        }

        val formato = SimpleDateFormat(
            "dd/MM/yyyy · hh:mm a",
            Locale("es", "SV")
        )

        return formato.format(timestamp.toDate())
    }
}
