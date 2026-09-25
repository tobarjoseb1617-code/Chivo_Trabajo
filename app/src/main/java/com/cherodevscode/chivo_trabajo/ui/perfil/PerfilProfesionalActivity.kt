package com.cherodevscode.chivo_trabajo.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityPerfilProfesionalBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.CrearSolicitudActivity

class PerfilProfesionalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilProfesionalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
