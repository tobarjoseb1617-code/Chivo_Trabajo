package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivitySeguimientoClienteBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity

/**
 * Pantalla de Seguimiento en Vivo para el Cliente (Patrón MVVM).
 * Muestra el estado del servicio en tiempo real, notificaciones de llegada del técnico,
 * verificación del PIN de seguridad, repuestos registrados por el profesional (solo lectura),
 * fotografías de evidencia y opciones para chatear o ver el mapa GPS.
 */
class SeguimientoClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguimientoClienteBinding
    private lateinit var viewModel: SeguimientoClienteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySeguimientoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SeguimientoClienteViewModel::class.java]

        val extraPin = intent.getStringExtra("EXTRA_PIN")
        viewModel.cargarDatosSeguimiento(extraPin)

        configurarBotones()
        observarViewModel()
    }

    private fun configurarBotones() {
        binding.btnRegresarSeguimientoCliente.setOnClickListener {
            finish()
        }

        binding.btnPerfilTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        binding.btnChatConProfesional.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("EXTRA_IS_PRO", false)
            }
            startActivity(intent)
        }

        binding.btnVerMapaCliente.setOnClickListener {
            Toast.makeText(
                this,
                "Visualizando mapa en vivo y ubicación en tiempo real del técnico",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observarViewModel() {
        viewModel.pinSeguridad.observe(this) { pin ->
            binding.tvPinCodigoCliente.text = "#$pin"
        }

        viewModel.tecnicoLlego.observe(this) { llego ->
            binding.layoutAlertaLlegada.visibility = if (llego) View.VISIBLE else View.GONE
        }

        viewModel.pinVerificado.observe(this) { verificado ->
            binding.layoutAlertaPinVerificado.visibility = if (verificado) View.VISIBLE else View.GONE
        }

        viewModel.fotoAntesUrl.observe(this) { url ->
            if (!url.isNullOrBlank()) {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(binding.ivFotoAntesCliente)
            }
        }

        viewModel.fotoDespuesUrl.observe(this) { url ->
            if (!url.isNullOrBlank()) {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(binding.ivFotoDespuesCliente)
            }
        }
    }
}
