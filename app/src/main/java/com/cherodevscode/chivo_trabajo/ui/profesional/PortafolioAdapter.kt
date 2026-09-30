package com.cherodevscode.chivo_trabajo.ui.profesional

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.model.Portafolio
import com.cherodevscode.chivo_trabajo.databinding.ItemPortafolioProfesionalBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PortafolioAdapter(
    private var listaPortafolio: List<Portafolio>,
    private val onOpcionesClick: (Portafolio) -> Unit
) : RecyclerView.Adapter<PortafolioAdapter.PortafolioViewHolder>() {

    inner class PortafolioViewHolder(
        private val binding: ItemPortafolioProfesionalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(portafolio: Portafolio) {

            // Título del trabajo (si existe)
            if (portafolio.titulo.isNotBlank()) {
                binding.tvTituloTrabajo.text = portafolio.titulo
                binding.tvTituloTrabajo.visibility = View.VISIBLE
            } else {
                binding.tvTituloTrabajo.visibility = View.GONE
            }

            // Descripción del trabajo
            binding.tvDescripcionTrabajo.text = portafolio.descripcion

            // Imagen almacenada en Cloudinary
            Glide.with(binding.root.context)
                .load(portafolio.url)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(binding.ivFotoTrabajo)

            // Fecha del trabajo
            val formatoFecha = SimpleDateFormat(
                "dd MMM yyyy",
                Locale("es", "SV")
            )

            binding.tvFechaTrabajo.text =
                formatoFecha.format(portafolio.fecha.toDate())

            // Menú de opciones
            binding.btnOpcionesTrabajo.setOnClickListener {
                onOpcionesClick(portafolio)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PortafolioViewHolder {

        val binding = ItemPortafolioProfesionalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PortafolioViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PortafolioViewHolder,
        position: Int
    ) {
        holder.bind(listaPortafolio[position])
    }

    override fun getItemCount(): Int {
        return listaPortafolio.size
    }

    fun actualizarLista(nuevaLista: List<Portafolio>) {
        listaPortafolio = nuevaLista
        notifyDataSetChanged()
    }
}
