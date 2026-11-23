package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import com.cherodevscode.chivo_trabajo.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.data.repository.ChatRepository
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.google.firebase.firestore.ListenerRegistration
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialChatsBinding
import com.cherodevscode.chivo_trabajo.ui.cliente.HistorialSolicitudesClienteActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.HistorialServiciosClienteActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.InicioActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.RadarProfesionalesActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.SolicitudesTrabajosCercanosActivity

class HistorialChatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialChatsBinding

    private var listener: ListenerRegistration? = null
    private val repository by lazy { ChatRepository(applicationContext) }
    private lateinit var adapter: ContactosChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isPro = intent.getBooleanExtra("EXTRA_IS_PRO", false)

        // Material 3: los IDs del menú conservan los destinos de los botones anteriores.
        binding.bottomNavigation.selectedItemId = R.id.navMensajesChat
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicioChat -> {
                    val targetIntent = if (isPro) {
                        Intent(this, InicioProfesionalActivity::class.java)
                    } else {
                        Intent(this, InicioActivity::class.java)
                    }
                    startActivity(targetIntent)
                    finish()
                }
                R.id.navSolicitudesChat -> {
                    val targetIntent = if (isPro) {
                        Intent(this, SolicitudesTrabajosCercanosActivity::class.java)
                    } else {
                        Intent(this, HistorialSolicitudesClienteActivity::class.java)
                    }
                    startActivity(targetIntent)
                    finish()
                }
                R.id.navServiciosChat -> {
                    val targetIntent = if (isPro) {
                        Intent(this, HistorialDeServiciosActivity::class.java).putExtra("EXTRA_IS_PRO", true)
                    } else {
                        Intent(this, HistorialServiciosClienteActivity::class.java)
                    }
                    startActivity(targetIntent)
                    finish()
                }
                R.id.navMensajesChat -> {
                    // Actual
                }
                R.id.navPerfilChat -> {
                    startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
                    finish()
                }
            }
            // Cada destino abre su Activity; el indicador identifica esta pantalla al regresar.
            item.itemId == R.id.navMensajesChat
        }

        // Detectar si el usuario actual es profesional o cliente mediante extra

        // Botón Regresar seguro al inicio correspondiente
        binding.btnRegresarHistorialChats.setOnClickListener {
            val targetIntent = if (isPro) {
                Intent(this, InicioProfesionalActivity::class.java)
            } else {
                Intent(this, InicioActivity::class.java)
            }.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(targetIntent)
            finish()
        }

        // Perfil superior
        binding.btnPerfilHistorialChatsTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        adapter = ContactosChatAdapter { contacto ->
            startActivity(Intent(this, ChatActivity::class.java).putExtra("EXTRA_CHAT_ID", contacto.id))
        }
        binding.listaChats.layoutManager = LinearLayoutManager(this)
        binding.listaChats.adapter = adapter

        // Buscar nuevo profesional en Radar
        binding.btnBuscarRadarChats.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }

    }
    override fun onStart() {
        super.onStart()
        val uid = AuthRepository(this).usuarioActual()?.uid
        if (uid == null) { binding.tvEstadoChats.text = "Inicia sesión para ver tus mensajes"; return }
        listener = repository.escucharConversaciones { chats, error ->
            binding.tvEstadoChats.text = when {
                error != null -> "No se pudieron cargar las conversaciones"
                chats.isNullOrEmpty() -> "Todavía no tienes conversaciones"
                else -> ""
            }
            binding.tvEstadoChats.visibility = if (error == null && !chats.isNullOrEmpty()) View.GONE else View.VISIBLE
            if (chats != null) adapter.mostrar(chats.map { chat ->
                val contacto = chat.participantes.firstOrNull { it != uid }.orEmpty()
                ContactoChat(chat.id, chat.nombres[contacto] ?: "Contacto", chat.ultimoMensaje.ifBlank { "Inicia la conversación" })
            })
        }
    }
    override fun onStop() { listener?.remove(); listener = null; super.onStop() }

}
