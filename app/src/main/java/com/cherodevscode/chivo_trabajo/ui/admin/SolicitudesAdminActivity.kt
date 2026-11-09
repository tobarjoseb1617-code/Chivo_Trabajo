
package com.cherodevscode.chivo_trabajo.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.databinding.ActivitySolicitudesAdminBinding

class SolicitudesAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudesAdminBinding

    private val viewModel: SolicitudesAdminViewModel by viewModels()

    private lateinit var adapter: SolicitudesAdminAdapter

    private var filtroActual = "TODAS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySolicitudesAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarFiltros()
        observarSolicitudes()

        binding.btnVolverAdmin.setOnClickListener {
            finish()
        }

        viewModel.cargarSolicitudes()
    }

    private fun configurarRecyclerView() {

        adapter = SolicitudesAdminAdapter { solicitud ->

            val intent = Intent(
                this,
                DetallesSolicitudAdminActivity::class.java
            )

            intent.putExtra("idSolicitud", solicitud.idSolicitud)

            startActivity(intent)
        }

        binding.rvSolicitudesAdmin.layoutManager =
            LinearLayoutManager(this)

        binding.rvSolicitudesAdmin.adapter = adapter
    }

    private fun configurarFiltros() {

        binding.btnFiltroTodas.setOnClickListener {
            filtroActual = "TODAS"
            aplicarFiltro()
        }

        binding.btnFiltroPendientes.setOnClickListener {
            filtroActual = "PENDIENTE"
            aplicarFiltro()
        }

        binding.btnFiltroAsignadas.setOnClickListener {
            filtroActual = "ASIGNADA"
            aplicarFiltro()
        }

        binding.btnFiltroCompletadas.setOnClickListener {
            filtroActual = "COMPLETADA"
            aplicarFiltro()
        }

        binding.btnFiltroCanceladas.setOnClickListener {
            filtroActual = "CANCELADA"
            aplicarFiltro()
        }
    }

    private fun observarSolicitudes() {

        viewModel.solicitudes.observe(this) {
            aplicarFiltro()
        }

        viewModel.cargando.observe(this) { cargando ->

            binding.progressSolicitudesAdmin.visibility =
                if (cargando) View.VISIBLE else View.GONE

            if (cargando) {
                binding.tvSinSolicitudesAdmin.visibility = View.GONE
            }
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
    }

    private fun aplicarFiltro() {

        val todasLasSolicitudes =
            viewModel.solicitudes.value ?: emptyList()

        val solicitudesFiltradas =
            if (filtroActual == "TODAS") {
                todasLasSolicitudes
            } else {
                todasLasSolicitudes.filter {
                    it.estado.equals(filtroActual, ignoreCase = true)
                }
            }

        adapter.actualizarLista(solicitudesFiltradas)

        binding.tvCantidadSolicitudesAdmin.text =
            "Solicitudes encontradas: ${solicitudesFiltradas.size}"

        val cargando = viewModel.cargando.value ?: false

        binding.tvSinSolicitudesAdmin.visibility =
            if (solicitudesFiltradas.isEmpty() && !cargando) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }
}
