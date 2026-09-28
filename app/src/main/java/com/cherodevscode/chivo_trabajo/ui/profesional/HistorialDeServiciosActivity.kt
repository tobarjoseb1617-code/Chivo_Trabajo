package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialDeServiciosBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.CrearSolicitudActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity

class HistorialDeServiciosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialDeServiciosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialDeServiciosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón Regresar seguro al inicio profesional
        binding.btnRegresarServicios.setOnClickListener {
            val intent = Intent(this, InicioProfesionalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        // Perfil superior
        binding.btnPerfilServiciosTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        // Seguimiento servicio activo -> Ir a Gestionar Servicio en Curso
        binding.btnSeguimientoServicio.setOnClickListener {
            startActivity(Intent(this, GestionarServicioEnCursoActivity::class.java))
        }

        // Ver orden
        binding.btnVerOrdenServicio.setOnClickListener {
            Toast.makeText(this, "Viendo orden #SRV-2041", Toast.LENGTH_SHORT).show()
        }

        // Ver recibo 1
        binding.btnVerRecibo1.setOnClickListener {
            Toast.makeText(this, "Generando recibo de pago para #SRV-1988", Toast.LENGTH_SHORT).show()
        }

        // Pedir de nuevo 1
        binding.btnPedirDeNuevo1.setOnClickListener {
            startActivity(Intent(this, CrearSolicitudActivity::class.java))
        }

        // Ver detalles historial 2 -> Ir a Detalles Solicitud Trabajo Profesional
        binding.btnVerDetallesHistoria2.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // Ver detalles historial 3 -> Ir a Detalles Solicitud Trabajo Profesional
        binding.btnVerDetallesHistoria3.setOnClickListener {
            startActivity(Intent(this, DetallesSolicitudTrabajoProfesionalActivity::class.java))
        }

        // Menú inferior: Inicio
        binding.navInicioServicios.setOnClickListener {
            startActivity(Intent(this, InicioProfesionalActivity::class.java))
            finish()
        }

        // Menú inferior: Solicitudes
        binding.navSolicitudesServicios.setOnClickListener {
            startActivity(Intent(this, SolicitudesTrabajosCercanosActivity::class.java))
            finish()
        }

        // Menú inferior: Servicios (Actual)
        binding.navServiciosServicios.setOnClickListener {
            // Ya estamos aquí
        }

        // Menú inferior: Mensajes
        binding.navMensajesServicios.setOnClickListener {
            startActivity(Intent(this, HistorialChatsActivity::class.java))
            finish()
        }

        // Menú inferior: Perfil
        binding.navPerfilServicios.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
            finish()
        }
    }
}
