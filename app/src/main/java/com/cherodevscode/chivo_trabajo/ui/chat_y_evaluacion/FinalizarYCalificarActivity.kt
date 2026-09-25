package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityFinalizarYCalificarBinding
import com.cherodevscode.chivo_trabajo.ui.cliente.InicioActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity

class FinalizarYCalificarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFinalizarYCalificarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinalizarYCalificarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón regresar
        binding.btnRegresarCalificar.setOnClickListener {
            finish()
        }

        // Perfil superior
        binding.btnPerfilCalificarTop.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // Confirmar Pago y Finalizar
        binding.btnConfirmarPagoFinalizar.setOnClickListener {
            Toast.makeText(this, "¡Pago de $25.00 USD confirmado y reseña guardada con éxito!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
}
