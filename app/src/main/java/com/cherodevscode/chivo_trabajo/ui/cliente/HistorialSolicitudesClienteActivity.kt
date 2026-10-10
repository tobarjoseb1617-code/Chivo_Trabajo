package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialSolicitudesClienteBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity

/**
 * Vista en arquitectura MVVM para el historial de solicitudes del cliente.
 * Observa los cambios del HistorialSolicitudesViewModel y actualiza el adaptador.
 */
class HistorialSolicitudesClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialSolicitudesClienteBinding
    private lateinit var viewModel: HistorialSolicitudesViewModel
    private lateinit var adapter: SolicitudesClienteAdapter

    private var filtroActual = "TODAS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialSolicitudesClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar ViewModel (Patrón MVVM)
        viewModel = ViewModelProvider(this)[HistorialSolicitudesViewModel::class.java]

        configurarRecyclerView()
        configurarNavegacion()
        configurarBotones()
        configurarFiltros()
        configurarBuscador()

        // Observar datos provenientes del ViewModel
        observarViewModel()
    }

    override fun onStart() {
        super.onStart()
        // Iniciar escucha en tiempo real en el ViewModel
        viewModel.escucharSolicitudesCliente()
    }

    private fun configurarRecyclerView() {
        adapter = SolicitudesClienteAdapter { idSolicitud ->
            val intent = Intent(this, DetallesSolicitudCreadaClienteActivity::class.java).apply {
                putExtra("idSolicitud", idSolicitud)
            }
            startActivity(intent)
        }

        binding.rvSolicitudesCliente.apply {
            layoutManager = LinearLayoutManager(this@HistorialSolicitudesClienteActivity)
            adapter = this@HistorialSolicitudesClienteActivity.adapter
            isNestedScrollingEnabled = false
        }
    }

    // Observar LiveData del ViewModel
    private fun observarViewModel() {
        viewModel.cargando.observe(this) { cargando ->
            binding.progressSolicitudesCliente.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.solicitudesFiltradas.observe(this) { documentos ->
            adapter.actualizarSolicitudes(documentos)

            val cantidad = documentos.size
            binding.tvCantidadSolicitudesCliente.text = if (cantidad == 1) "1 solicitud" else "$cantidad solicitudes"

            binding.layoutSinSolicitudesCliente.visibility = if (cantidad == 0) View.VISIBLE else View.GONE
            binding.rvSolicitudesCliente.visibility = if (cantidad > 0) View.VISIBLE else View.GONE

            actualizarEstiloFiltros()
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarSolicitudCliente.doAfterTextChanged { texto ->
            viewModel.setBusqueda(texto?.toString().orEmpty())
        }
    }

    private fun configurarFiltros() {
        binding.btnFiltroTodasCliente.setOnClickListener {
            filtroActual = "TODAS"
            viewModel.setFiltro("TODAS")
        }

        binding.btnFiltroAbiertasCliente.setOnClickListener {
            filtroActual = "ABIERTAS"
            viewModel.setFiltro("ABIERTAS")
        }

        binding.btnFiltroProcesoCliente.setOnClickListener {
            filtroActual = "EN_PROCESO"
            viewModel.setFiltro("EN_PROCESO")
        }

        binding.btnFiltroFinalizadasCliente.setOnClickListener {
            filtroActual = "FINALIZADAS"
            viewModel.setFiltro("FINALIZADAS")
        }
    }

    private fun actualizarEstiloFiltros() {
        val filtros = listOf(
            binding.btnFiltroTodasCliente to "TODAS",
            binding.btnFiltroAbiertasCliente to "ABIERTAS",
            binding.btnFiltroProcesoCliente to "EN_PROCESO",
            binding.btnFiltroFinalizadasCliente to "FINALIZADAS"
        )

        filtros.forEach { (boton, filtro) ->
            val seleccionado = filtroActual == filtro
            val colorFondo = if (seleccionado) R.color.ui_brand else R.color.ui_surface
            val colorTexto = if (seleccionado) android.R.color.white else R.color.ui_text_secondary

            boton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, colorFondo)
            )
            boton.setTextColor(ContextCompat.getColor(this, colorTexto))
        }
    }

    private fun configurarBotones() {
        binding.btnRegresarHistorialSolicitudesCliente.setOnClickListener {
            finish()
        }

        binding.btnPerfilHistorialClienteTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }

        binding.btnPublicarNuevaSolicitud.setOnClickListener {
            startActivity(Intent(this, CrearSolicitudActivity::class.java))
        }
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.selectedItemId = R.id.navSolicitudesSolCliente
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicioSolCliente -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                    true
                }
                R.id.navSolicitudesSolCliente -> true
                R.id.navServiciosSolCliente -> {
                    startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
                    finish()
                    true
                }
                R.id.navMensajesSolCliente -> {
                    startActivity(Intent(this, HistorialChatsActivity::class.java))
                    finish()
                    true
                }
                R.id.navPerfilSolCliente -> {
                    startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
