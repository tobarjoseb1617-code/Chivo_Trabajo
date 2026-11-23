package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.model.Mensaje
import com.cherodevscode.chivo_trabajo.databinding.ItemMensajeBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MensajesAdapter(private val uid: String) : ListAdapter<Mensaje, MensajesAdapter.Holder>(object : DiffUtil.ItemCallback<Mensaje>() {
    override fun areItemsTheSame(a: Mensaje, b: Mensaje) = a.id == b.id
    override fun areContentsTheSame(a: Mensaje, b: Mensaje) = a == b
}) {
    class Holder(val binding: ItemMensajeBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(ItemMensajeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val mensaje = getItem(position)
        val b = holder.binding
        b.root.gravity = if (mensaje.remitenteUid == uid) Gravity.END else Gravity.START
        b.burbuja.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(b.root.context,
            if (mensaje.remitenteUid == uid) R.color.ui_brand else R.color.ui_surface))
        val propio = mensaje.remitenteUid == uid
        b.tvTexto.setTextColor(if (propio) android.graphics.Color.WHITE else androidx.core.content.ContextCompat.getColor(b.root.context, R.color.ui_text))
        b.tvHora.setTextColor(if (propio) android.graphics.Color.WHITE else androidx.core.content.ContextCompat.getColor(b.root.context, R.color.ui_text_secondary))
        b.tvTexto.text = when(mensaje.tipo) { "imagen" -> "Ver foto"; "video" -> "▶ Reproducir video"; "audio" -> "▶ Reproducir audio"; else -> mensaje.texto }
        b.tvHora.text = mensaje.creadoEn?.toDate()?.let { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(it) } ?: "Enviando…"
        b.imagen.visibility = if (mensaje.tipo == "imagen") View.VISIBLE else View.GONE
        Glide.with(b.imagen).clear(b.imagen)
        if (mensaje.tipo == "imagen") Glide.with(b.imagen).load(mensaje.archivoUrl).into(b.imagen)
        b.burbuja.setOnClickListener(if (mensaje.tipo == "texto") null else View.OnClickListener {
            try {
                val contexto = b.root.context
                contexto.startActivity(Intent(contexto, VisorMultimediaActivity::class.java)
                    .putExtra("URL", mensaje.archivoUrl).putExtra("TIPO", mensaje.tipo))
            } catch (_: Exception) { Toast.makeText(b.root.context, "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show() }
        })
    }
}
