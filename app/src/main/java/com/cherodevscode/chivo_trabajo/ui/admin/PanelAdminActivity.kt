package com.cherodevscode.chivo_trabajo.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivityPanelAdminBinding
import com.cherodevscode.chivo_trabajo.ui.autenticacion.IniciarSesionActivity

class PanelAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelAdminBinding

    private val viewModel: PanelAdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPanelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarLogo()
        configurarBotones()
        observarViewModel()
    }

    private fun configurarLogo() {
        Glide.with(this)
            .load("https://res.cloudinary.com/djwvfjt7k/image/upload/v1790744838/o8ipleapa27fyd72nsll.png")
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.ivLogoAdmin)
    }

    private fun configurarBotones() {
        binding.btnCerrarSesionAdmin.setOnClickListener {
            viewModel.cerrarSesion()
        }

        binding.btnVerSolicitudesAdmin.setOnClickListener {
            val intent = Intent(
                this,
                SolicitudesAdminActivity::class.java
            )
            startActivity(intent)
        }
    }

    private fun observarViewModel() {
        viewModel.estadisticas.observe(this) { estadisticas ->

            binding.tvTotalSolicitudesAdmin.text =
                estadisticas.total.toString()

            binding.tvPendientesAdmin.text =
                estadisticas.pendientes.toString()

            binding.tvAsignadasAdmin.text =
                estadisticas.asignadas.toString()

            binding.tvEnCursoAdmin.text =
                estadisticas.enCurso.toString()

            binding.tvCompletadasAdmin.text =
                estadisticas.completadas.toString()

            binding.tvCanceladasAdmin.text =
                estadisticas.canceladas.toString()
        }

        viewModel.error.observe(this) { mensaje ->
            if (!mensaje.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    mensaje,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.sesionCerrada.observe(this) { cerrada ->
            if (cerrada == true) {
                val intent = Intent(
                    this,
                    IniciarSesionActivity::class.java
                )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarEstadisticas()
    }
}
