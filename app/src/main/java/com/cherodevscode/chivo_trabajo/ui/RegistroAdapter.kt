package com.cherodevscode.chivo_trabajo.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cherodevscode.chivo_trabajo.data.Registro
import com.cherodevscode.chivo_trabajo.databinding.ItemRegistroBinding


class RegistroAdapter : RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder>() {
    private val registros = mutableListOf<Registro>()

    fun actualizar(lista: List<Registro>) {
        registros.clear()
        registros.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {
        val binding = ItemRegistroBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RegistroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        holder.bind(registros[position])
    }

    override fun getItemCount(): Int = registros.size

    class RegistroViewHolder(
        private val binding: ItemRegistroBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(registro: Registro) {
            binding.tvTitulo.text = registro.titulo
            binding.tvDescripcion.text = registro.descripcion
        }
    }
}
