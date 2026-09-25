package com.cherodevscode.chivo_trabajo.ui.cliente

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivitySeguimientoBinding

class SeguimientoActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeguimientoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeguimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pin = intent.getStringExtra("EXTRA_PIN") ?: "1234"
        binding.tvPinSeguridad.text = pin
    }
}
