package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioProfesionalBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity

class InicioProfesionalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioProfesionalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Servicio en curso ahora mismo (Tarjeta) -> Ir a Gestionar Servicio en Curso
        binding.cardServicioEnCursoPro.setOnClickListener {
            startActivity(Intent(this, GestionarServicioEnCursoActivity::class.java))
        }

        // 2. Ver ruta GPS -> Ir a Detalles de Solicitud de Trabajo Profesional
        binding.btnVerRutaGps.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // 3. Abrir chat -> Ir a ChatActivity
        binding.btnAbrirChatPro.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // 4. Ver detalles de oportunidad 1 -> Ir a Detalles de Solicitud de Trabajo Profesional
        binding.btnDetallesOp1.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // 5. Ver detalles de oportunidad 2 -> Ir a Detalles de Solicitud de Trabajo Profesional
        binding.btnDetallesOp2.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // 6. Menú inferior: Solicitudes -> Ir a Solicitudes Trabajos Cercanos
        binding.navSolicitudesPro.setOnClickListener {
            startActivity(Intent(this, SolicitudesTrabajosCercanosActivity::class.java))
        }

        // 7. Menú inferior: Servicios -> Ir a Historial de Servicios (Paquete profesional)
        binding.navServiciosPro.setOnClickListener {
            startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
        }

        // 8. Menú inferior: Mensajes -> Ir a Historial de Chats (con indicador pro)
        binding.navMensajesPro.setOnClickListener {
            startActivity(Intent(this, HistorialChatsActivity::class.java).putExtra("EXTRA_IS_PRO", true))
        }

        // 9. Menú inferior y botón superior: Perfil / Configuración
        binding.navPerfilPro.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }
        binding.btnPerfilProTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        // Propuesta rápida 1
        binding.btnPropuestaRapida1.setOnClickListener {
            Toast.makeText(this, "¡Propuesta rápida enviada para Fuga en Cisterna!", Toast.LENGTH_SHORT).show()
        }

        // Propuesta rápida 2
        binding.btnPropuestaRapida2.setOnClickListener {
            Toast.makeText(this, "¡Propuesta rápida enviada para Instalación de Grifería!", Toast.LENGTH_SHORT).show()
        }
    }
}
