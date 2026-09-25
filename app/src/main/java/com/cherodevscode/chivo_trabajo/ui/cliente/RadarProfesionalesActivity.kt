package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityRadarProfesionalesBinding
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity

class RadarProfesionalesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRadarProfesionalesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRadarProfesionalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Perfil superior
        binding.btnPerfilRadar.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Ver Perfil de Roberto Méndez
        binding.btnVerPerfilRadarPro.setOnClickListener {
            Toast.makeText(this, "Viendo perfil de Roberto Méndez...", Toast.LENGTH_SHORT).show()
        }

        // Pedir Servicio (Solicitar)
        binding.btnPedirServicioRadar.setOnClickListener {
            val intent = Intent(this, CrearSolicitudActivity::class.java)
            startActivity(intent)
        }
    }
}
