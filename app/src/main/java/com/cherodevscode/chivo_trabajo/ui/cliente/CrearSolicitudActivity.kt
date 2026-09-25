package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityCrearSolicitudBinding
import com.cherodevscode.chivo_trabajo.utils.PinGenerator

class CrearSolicitudActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearSolicitudBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPublicarSolicitud.setOnClickListener {
            val detalles = binding.etDetallesSolicitud.text.toString().trim()
            if (detalles.isEmpty()) {
                Toast.makeText(this, "Por favor describa el servicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pinSeguridad = PinGenerator.generarPin()
            Toast.makeText(this, "¡Solicitud creada! PIN: $pinSeguridad", Toast.LENGTH_LONG).show()

            val intent = Intent(this, SeguimientoActivity::class.java).apply {
                putExtra("EXTRA_PIN", pinSeguridad)
            }
            startActivity(intent)
            finish()
        }
    }
}
