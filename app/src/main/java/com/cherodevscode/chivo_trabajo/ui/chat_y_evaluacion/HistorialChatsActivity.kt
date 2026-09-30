package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialChatsBinding
import com.cherodevscode.chivo_trabajo.ui.cliente.HistorialSolicitudesClienteActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.InicioActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.RadarProfesionalesActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.SolicitudesTrabajosCercanosActivity

class HistorialChatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialChatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Detectar si el usuario actual es profesional o cliente mediante extra
        val isPro = intent.getBooleanExtra("EXTRA_IS_PRO", false)

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

        // Chat con Roberto Méndez (Activo)
        binding.cardChatRoberto.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // Buscar nuevo profesional en Radar
        binding.btnBuscarRadarChats.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }

        // Menú inferior: Inicio
        binding.navInicioChat.setOnClickListener {
            val targetIntent = if (isPro) {
                Intent(this, InicioProfesionalActivity::class.java)
            } else {
                Intent(this, InicioActivity::class.java)
            }
            startActivity(targetIntent)
            finish()
        }

        // Menú inferior: Solicitudes
        binding.navSolicitudesChat.setOnClickListener {
            val targetIntent = if (isPro) {
                Intent(this, SolicitudesTrabajosCercanosActivity::class.java)
            } else {
                Intent(this, HistorialSolicitudesClienteActivity::class.java)
            }
            startActivity(targetIntent)
            finish()
        }

        // Menú inferior: Servicios
        binding.navServiciosChat.setOnClickListener {
            startActivity(Intent(this, HistorialDeServiciosActivity::class.java).putExtra("EXTRA_IS_PRO", isPro))
            finish()
        }

        // Menú inferior: Mensajes (Ya estamos aquí)
        binding.navMensajesChat.setOnClickListener {
            // Actual
        }

        // Menú inferior: Perfil
        binding.navPerfilChat.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
            finish()
        }
    }
}
