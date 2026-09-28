package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivitySolicitudesTrabajosCercanosBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity

class SolicitudesTrabajosCercanosActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySolicitudesTrabajosCercanosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudesTrabajosCercanosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón Regresar seguro al inicio profesional
        binding.btnRegresarSolicitudesPro.setOnClickListener {
            val intent = Intent(this, InicioProfesionalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        // Perfil superior y menú inferior - Perfil
        binding.btnPerfilSolicitudesTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }
        binding.navPerfilPro?.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        // Ver propuestas enviadas (Banner superior) -> Ir a Historial de Chats
        binding.btnVerPropuestasEnviadas.setOnClickListener {
            startActivity(Intent(this, HistorialChatsActivity::class.java))
        }

        // Ver detalles de la solicitud -> Ir a DetallesSolicitudTrabajoProfesionalActivity
        binding.btnDetallesSolicitud1.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // Cotizar solicitud 1 -> Ir a DetallesSolicitudTrabajoProfesionalActivity
        binding.btnCotizar1.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // Enviar oferta 2
        binding.btnEnviarOferta2.setOnClickListener {
            Toast.makeText(this, "Oferta personalizada enviada a Mariana Palacios", Toast.LENGTH_SHORT).show()
        }

        // Aceptar tarifa & postular 3
        binding.btnAceptarPostular3.setOnClickListener {
            Toast.makeText(this, "¡Postulación exitosa para Roberto Alvarenga!", Toast.LENGTH_SHORT).show()
        }

        // Activar WhatsApp
        binding.btnActivarWhatsapp.setOnClickListener {
            Toast.makeText(this, "Alertas sonoras por WhatsApp activadas correctamente", Toast.LENGTH_SHORT).show()
        }

        // Menú inferior: Inicio
        binding.navInicioPro?.setOnClickListener {
            startActivity(Intent(this, InicioProfesionalActivity::class.java))
            finish()
        }

        // Menú inferior: Solicitudes (Actual)
        binding.navSolicitudesPro?.setOnClickListener {
            // Ya estamos aquí
        }

        // Menú inferior: Servicios
        binding.navServiciosPro?.setOnClickListener {
            startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
            finish()
        }

        // Menú inferior: Mensajes
        binding.navMensajesPro?.setOnClickListener {
            startActivity(Intent(this, HistorialChatsActivity::class.java))
            finish()
        }
    }
}
