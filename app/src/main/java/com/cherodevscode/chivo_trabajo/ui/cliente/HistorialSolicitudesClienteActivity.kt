package com.cherodevscode.chivo_trabajo.ui.cliente

import com.cherodevscode.chivo_trabajo.R
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

        // Material 3: los IDs del menú conservan los destinos de los botones anteriores.
        binding.bottomNavigation.selectedItemId = R.id.navSolicitudesSolCliente
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicioSolCliente -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                }
                R.id.navSolicitudesSolCliente -> {
                    // Ya estamos aquí
                }
                R.id.navServiciosSolCliente -> {
                    startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
                    finish()
                }
                R.id.navMensajesSolCliente -> {
                    startActivity(Intent(this, HistorialChatsActivity::class.java))
                    finish()
                }
                R.id.navPerfilSolCliente -> {
                    startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
                    finish()
                }
            }
            // Cada destino abre su Activity; el indicador identifica esta pantalla al regresar.
            item.itemId == R.id.navSolicitudesSolCliente
        }

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

    }
}
