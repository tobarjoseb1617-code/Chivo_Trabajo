package com.cherodevscode.chivo_trabajo.ui.perfil

import com.cherodevscode.chivo_trabajo.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityPerfilProfesionalBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.CrearSolicitudActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.SolicitudesTrabajosCercanosActivity

class PerfilProfesionalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilProfesionalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Material 3: los IDs del menú conservan los destinos de los botones anteriores.
        binding.bottomNavigation.selectedItemId = R.id.navPerfilPerfilPro
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicioPerfilPro -> {
                    startActivity(Intent(this, InicioProfesionalActivity::class.java))
                    finish()
                }
                R.id.navSolicitudesPerfilPro -> {
                    startActivity(Intent(this, SolicitudesTrabajosCercanosActivity::class.java))
                    finish()
                }
                R.id.navServiciosPerfilPro -> {
                    startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
                    finish()
                }
                R.id.navMensajesPerfilPro -> {
                    startActivity(Intent(this, HistorialChatsActivity::class.java))
                    finish()
                }
                R.id.navPerfilPerfilPro -> {
                    // Ya estamos aquí
                }
            }
            // Cada destino abre su Activity; el indicador identifica esta pantalla al regresar.
            item.itemId == R.id.navPerfilPerfilPro
        }

        // Botón Regresar
        binding.btnRegresarPerfilPro.setOnClickListener {
            finish()
        }

        // Botón Contratar / Solicitar
        binding.btnContratarPerfil.setOnClickListener {
            val intent = Intent(this, CrearSolicitudActivity::class.java)
            startActivity(intent)
        }

        // Botón Chat
        binding.btnChatPerfil.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // Botón Compartir
        binding.btnCompartirPerfil.setOnClickListener {
            Toast.makeText(this, "Enlace del perfil de Roberto Méndez copiado", Toast.LENGTH_SHORT).show()
        }

    }
}
