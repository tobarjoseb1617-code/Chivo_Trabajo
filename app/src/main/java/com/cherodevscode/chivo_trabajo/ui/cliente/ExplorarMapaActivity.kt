package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityClienteExplorarMapaBinding
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity

class ExplorarMapaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClienteExplorarMapaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteExplorarMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de Perfil superior
        binding.btnPerfilExplorar.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Botón Ver Perfil del técnico Roberto Méndez
        binding.btnVerPerfilPro.setOnClickListener {
            Toast.makeText(this, "Viendo perfil de Roberto Méndez...", Toast.LENGTH_SHORT).show()
        }

        // Botón Solicitar Ya del técnico Roberto Méndez
        binding.btnSolicitarYa.setOnClickListener {
            val intent = Intent(this, CrearSolicitudActivity::class.java)
            startActivity(intent)
        }
    }
}
