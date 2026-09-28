package com.cherodevscode.chivo_trabajo.ui.profesional

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityDetalleSolicitudTrabajoProfesionalBinding

class DetallesSolicitudTrabajoProfesionalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleSolicitudTrabajoProfesionalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleSolicitudTrabajoProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón Regresar
        binding.btnRegresarDetallePro.setOnClickListener {
            finish()
        }

        // Enviar Propuesta
        binding.btnEnviarPropuestaPro.setOnClickListener {
            Toast.makeText(this, "¡Propuesta de $25.00 USD enviada con éxito a Carlos Rivera!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
