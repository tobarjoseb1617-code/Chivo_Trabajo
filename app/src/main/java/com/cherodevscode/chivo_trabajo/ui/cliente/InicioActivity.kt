package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioBinding
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity

class InicioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón Ver seguimiento / mapa en la tarjeta de servicio en curso
        binding.btnVerSeguimiento.setOnClickListener {
            val intent = Intent(this, SeguimientoActivity::class.java).apply {
                putExtra("EXTRA_PIN", "3821")
            }
            startActivity(intent)
        }

        // Botón Perfil superior
        binding.btnPerfilTop.setOnClickListener {
            val intent = Intent(this, ConfiguracionPerfilActivity::class.java)
            startActivity(intent)
        }

        // Navegación inferior - Perfil
        binding.navPerfil.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        // Ver todas las categorías
        binding.btnVerTodasCategorias.setOnClickListener {
            Toast.makeText(this, "Mostrando todas las categorías de servicios", Toast.LENGTH_SHORT).show()
        }
    }
}
