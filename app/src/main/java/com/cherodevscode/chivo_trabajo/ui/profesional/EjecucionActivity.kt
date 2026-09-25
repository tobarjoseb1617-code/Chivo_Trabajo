package com.cherodevscode.chivo_trabajo.ui.profesional

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityEjecucionBinding

class EjecucionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEjecucionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEjecucionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnValidarPin.setOnClickListener {
            val pin = binding.etPinIngresado.text.toString().trim()
            if (pin.length == 4) {
                Toast.makeText(this, "¡PIN validado con éxito!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Ingrese un PIN de 4 dígitos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
