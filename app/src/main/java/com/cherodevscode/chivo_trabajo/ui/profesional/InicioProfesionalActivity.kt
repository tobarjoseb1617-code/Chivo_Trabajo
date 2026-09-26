package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioProfesionalBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity

class InicioProfesionalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioProfesionalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Perfil superior
        binding.btnPerfilProTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        // Navegación inferior - Perfil
        binding.navPerfilPro.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        // Ver ruta GPS (Servicio en curso)
        binding.btnVerRutaGps.setOnClickListener {
            startActivity(Intent(this, EjecucionActivity::class.java))
        }

        // Abrir chat (Servicio en curso)
        binding.btnAbrirChatPro.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
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
