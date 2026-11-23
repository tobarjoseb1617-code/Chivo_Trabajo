package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cherodevscode.chivo_trabajo.databinding.ItemContactoChatBinding

data class ContactoChat(val id: String, val nombre: String, val detalle: String, val fotoUrl: String = "")
class ContactosChatAdapter(private val abrir: (ContactoChat) -> Unit) : RecyclerView.Adapter<ContactosChatAdapter.Holder>() {
    private var contactos = emptyList<ContactoChat>()
    fun mostrar(datos: List<ContactoChat>) { contactos = datos; notifyDataSetChanged() }
    class Holder(val binding: ItemContactoChatBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(ItemContactoChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun getItemCount() = contactos.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val contacto = contactos[position]
        holder.binding.fotoContacto.visibility = if (contacto.fotoUrl.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
        com.bumptech.glide.Glide.with(holder.binding.fotoContacto).load(contacto.fotoUrl)
            .placeholder(com.cherodevscode.chivo_trabajo.R.mipmap.ic_launcher).into(holder.binding.fotoContacto)
        holder.binding.tvNombre.text = contacto.nombre
        holder.binding.tvDetalle.text = contacto.detalle
        holder.binding.btnChat.setOnClickListener { abrir(contacto) }
    }
}
