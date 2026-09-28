package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialSolicitudesClienteBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity

class HistorialSolicitudesClienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialSolicitudesClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialSolicitudesClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón Regresar
        binding.btnRegresarHistorialSolicitudesCliente.setOnClickListener {
            finish()
        }

        // Perfil superior
        binding.btnPerfilHistorialClienteTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        // Publicar nueva solicitud
        binding.btnPublicarNuevaSolicitud.setOnClickListener {
            startActivity(Intent(this, CrearSolicitudActivity::class.java))
        }

        // Ver 3 Propuestas (Solicitud 1) -> Ir a DetallesSolicitudCreadaClienteActivity
        binding.btnVerPropuestas1.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudCreadaClienteActivity::class.java))
        }

        // Gestionar (Solicitud 1) -> Ir a DetallesSolicitudCreadaClienteActivity
        binding.btnGestionar1.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudCreadaClienteActivity::class.java))
        }

        // Ver Propuesta (Solicitud 2) -> Ir a DetallesSolicitudCreadaClienteActivity
        binding.btnVerPropuesta2.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudCreadaClienteActivity::class.java))
        }

        // Seguimiento (Solicitud 3 - Técnico Asignado)
        binding.btnSeguimiento3.setOnClickListener {
            val intent = Intent(this, SeguimientoActivity::class.java).apply {
                putExtra("EXTRA_PIN", "8874")
            }
            startActivity(intent)
        }

        // Chat Directo (Solicitud 3)
        binding.btnChatDirecto3.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // Ver Recibo Digital (Historial)
        binding.btnVerReciboDigital4.setOnClickListener {
            Toast.makeText(this, "Generando recibo digital para #SOL-8760", Toast.LENGTH_SHORT).show()
        }

        // Solicitar de Nuevo (Historial)
        binding.btnSolicitarNuevo4.setOnClickListener {
            startActivity(Intent(this, CrearSolicitudActivity::class.java))
        }

        // Menú inferior: Inicio
        binding.navInicioSolCliente.setOnClickListener {
            startActivity(Intent(this, InicioActivity::class.java))
            finish()
        }

        // Menú inferior: Solicitudes (Actual)
        binding.navSolicitudesSolCliente.setOnClickListener {
            // Ya estamos aquí
        }

        // Menú inferior: Servicios
        binding.navServiciosSolCliente.setOnClickListener {
            startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
            finish()
        }

        // Menú inferior: Mensajes
        binding.navMensajesSolCliente.setOnClickListener {
            startActivity(Intent(this, HistorialChatsActivity::class.java))
            finish()
        }

        // Menú inferior: Perfil
        binding.navPerfilSolCliente.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
            finish()
        }
    }
}
