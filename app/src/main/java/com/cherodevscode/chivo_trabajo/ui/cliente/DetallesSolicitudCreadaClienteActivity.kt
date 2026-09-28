package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetallesSolicitudCreadaClienteBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity

class DetallesSolicitudCreadaClienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetallesSolicitudCreadaClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallesSolicitudCreadaClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón Regresar
        binding.btnRegresarSeguimientoCliente.setOnClickListener {
            finish()
        }

        // Perfil superior
        binding.btnPerfilDetallesTop.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Roberto Méndez - Perfil
        binding.btnPerfilPro1.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Roberto Méndez - Chatear
        binding.btnChatearPro1.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // Roberto Méndez - Aceptar Propuesta
        binding.btnAceptarPro1.setOnClickListener {
            Toast.makeText(this, "¡Propuesta de Roberto Méndez aceptada! En camino...", Toast.LENGTH_LONG).show()
            val intent = Intent(this, SeguimientoActivity::class.java).apply {
                putExtra("EXTRA_PIN", "3821")
            }
            startActivity(intent)
            finish()
        }

        // Ver Propuesta 2 (Carlos Mendoza)
        binding.btnVerPropuesta2.setOnClickListener {
            Toast.makeText(this, "Viendo propuesta detallada de Carlos Mendoza", Toast.LENGTH_SHORT).show()
        }

        // Ver Propuesta 3 (David Ramos)
        binding.btnVerPropuesta3.setOnClickListener {
            Toast.makeText(this, "Viendo propuesta detallada de David Ramos", Toast.LENGTH_SHORT).show()
        }

        // Editar solicitud
        binding.btnEditarSolicitud.setOnClickListener {
            Toast.makeText(this, "Modificando detalles de la solicitud...", Toast.LENGTH_SHORT).show()
        }

        // Cancelar solicitud
        binding.btnCancelarSolicitud.setOnClickListener {
            Toast.makeText(this, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
