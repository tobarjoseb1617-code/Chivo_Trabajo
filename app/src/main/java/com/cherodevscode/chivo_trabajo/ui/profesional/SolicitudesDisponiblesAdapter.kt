
package com.cherodevscode.chivo_trabajo.ui.profesional

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cherodevscode.chivo_trabajo.databinding.ItemSolicitudDisponibleProBinding
import com.google.firebase.firestore.DocumentSnapshot

class SolicitudesDisponiblesAdapter(
    private val solicitudes: MutableList<DocumentSnapshot>,
    private val onVerDetalles: (String) -> Unit
) : RecyclerView.Adapter<SolicitudesDisponiblesAdapter.SolicitudViewHolder>() {

    inner class SolicitudViewHolder(
        val binding: ItemSolicitudDisponibleProBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolicitudViewHolder {

        val binding = ItemSolicitudDisponibleProBinding.inflate(
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
        val documento = solicitudes[position]
        val binding = holder.binding

        val categoria = documento.getString("categoryId")
            .orEmpty()
            .ifBlank { "Sin categoría" }

        val descripcion = documento.getString("descripcion")
            .orEmpty()
            .ifBlank { "Sin descripción" }

        val ciudad = documento.getString("ciudad")
            .orEmpty()
            .ifBlank { "Ubicación no especificada" }

        val fecha = documento.getString("fechaServicio")
            .orEmpty()

        val hora = documento.getString("horaServicio")
            .orEmpty()

        // En Firestore el presupuesto puede estar guardado
        // como texto, por ejemplo: "$30 – $50".
        val presupuesto = when (
            val valor = documento.get("presupuesto")
        ) {
            is String -> valor.ifBlank { "Por definir" }
            is Number -> "$${valor}"
            else -> "Por definir"
        }

        binding.tvCategoriaSolicitudPro.text = categoria
        binding.tvDescripcionSolicitudPro.text = descripcion
        binding.tvUbicacionSolicitudPro.text = ciudad
        binding.tvEstadoSolicitudPro.text = "Disponible"
        binding.tvPresupuestoSolicitudPro.text = presupuesto

        binding.tvFechaSolicitudPro.text = when {
            fecha.isNotBlank() && hora.isNotBlank() ->
                "$fecha · $hora"

            fecha.isNotBlank() -> fecha

            else -> "Fecha por definir"
        }

        // Enviamos el ID real del documento de Firestore.
        binding.btnVerDetalleSolicitudPro.setOnClickListener {
            onVerDetalles(documento.id)
        }
    }

    override fun getItemCount(): Int {
        return solicitudes.size
    }

    fun actualizarSolicitudes(
        nuevasSolicitudes: List<DocumentSnapshot>
    ) {
        solicitudes.clear()
        solicitudes.addAll(nuevasSolicitudes)
        notifyDataSetChanged()
    }
}
