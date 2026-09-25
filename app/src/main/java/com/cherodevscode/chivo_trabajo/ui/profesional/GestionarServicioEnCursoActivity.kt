package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityGestionarServicioEnCursoBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity

class GestionarServicioEnCursoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGestionarServicioEnCursoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionarServicioEnCursoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón regresar
        binding.btnRegresarGestion.setOnClickListener {
            finish()
        }

        // Perfil superior
        binding.btnPerfilGestionTop.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Llamar cliente
        binding.btnLlamarClienteGestion.setOnClickListener {
            Toast.makeText(this, "Llamando a Carlos Rivera...", Toast.LENGTH_SHORT).show()
        }

        // Abrir GPS / Waze
        binding.btnAbrirGpsWaze.setOnClickListener {
            Toast.makeText(this, "Abriendo GPS / Waze hacia Res. Cumbres de Cuscatlán", Toast.LENGTH_SHORT).show()
        }

        // Chat con Carlos
        binding.btnChatConCarlos.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // Registrar repuestos
        binding.btnRegistrarRepuestos.setOnClickListener {
            Toast.makeText(this, "Módulo de registro de repuestos o piezas extra", Toast.LENGTH_SHORT).show()
        }

        // Tomar foto Después
        binding.btnTomarFotoDespues.setOnClickListener {
            Toast.makeText(this, "Abriendo cámara para evidencia de garantía 'Después'...", Toast.LENGTH_SHORT).show()
        }

        // ¡He llegado a la ubicación del cliente!
        binding.btnLlegueUbicacion.setOnClickListener {
            Toast.makeText(this, "¡Has llegado! Carlos Rivera ha sido notificado automáticamente.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
