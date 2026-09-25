package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivitySolicitudesTrabajosCercanosBinding
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity

class SolicitudesTrabajosCercanosActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySolicitudesTrabajosCercanosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudesTrabajosCercanosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Perfil superior
        binding.btnPerfilSolicitudesTop.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Cotizar solicitud 1
        binding.btnCotizar1.setOnClickListener {
            Toast.makeText(this, "Cotización de $25 USD enviada a Carlos Rivera", Toast.LENGTH_SHORT).show()
        }

        // Enviar oferta 2
        binding.btnEnviarOferta2.setOnClickListener {
            Toast.makeText(this, "Oferta personalizada enviada a Mariana Palacios", Toast.LENGTH_SHORT).show()
        }

        // Aceptar tarifa & postular 3
        binding.btnAceptarPostular3.setOnClickListener {
            Toast.makeText(this, "¡Postulación exitosa para Roberto Alvarenga!", Toast.LENGTH_SHORT).show()
        }

        // Activar WhatsApp
        binding.btnActivarWhatsapp.setOnClickListener {
            Toast.makeText(this, "Alertas sonoras por WhatsApp activadas correctamente", Toast.LENGTH_SHORT).show()
        }
    }
}
