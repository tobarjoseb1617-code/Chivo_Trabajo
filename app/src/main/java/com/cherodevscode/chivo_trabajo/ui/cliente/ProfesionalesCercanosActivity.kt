package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityProfesionalesCercanosBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity

class ProfesionalesCercanosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfesionalesCercanosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfesionalesCercanosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ir a perfil superior
        binding.btnPerfilProCercanosTop.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Ir a Radar de Profesionales
        binding.btnVerRadarCercanos.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }

        // Contactar profesional 1
        binding.btnContactar1.setOnClickListener {
            Toast.makeText(this, "Iniciando contacto con Roberto Méndez...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}
