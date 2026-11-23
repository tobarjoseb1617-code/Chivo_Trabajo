package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialServiciosClienteBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import java.util.Locale

/**
 * Pantalla de Mis Servicios para el Cliente (Patrón MVVM).
 * Muestra el historial y estado de los servicios prestados al cliente categorizados en:
 * En curso, Pendientes / No aceptados y Realizados.
 */
class HistorialServiciosClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialServiciosClienteBinding
    private lateinit var viewModel: HistorialServiciosClienteViewModel
    private lateinit var adapter: SolicitudesClienteAdapter
    private val authRepository by lazy { AuthRepository(this) }

    private var filtroActual = "EN_CURSO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialServiciosClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[HistorialServiciosClienteViewModel::class.java]

        configurarRecyclerView()
        configurarNavegacion()
        configurarBotones()
        configurarFiltros()
        configurarBuscador()
        observarViewModel()
    }

    override fun onStart() {
        super.onStart()

        val uid = authRepository.usuarioActual()?.uid
        viewModel.escucharServiciosCliente(uid)
    }

    private fun configurarRecyclerView() {
        adapter = SolicitudesClienteAdapter { idSolicitud ->
            val doc = viewModel.serviciosFiltrados.value?.find { it.id == idSolicitud }
            val estado = doc?.getString("estado")?.uppercase(Locale.ROOT).orEmpty()

            val intent = if (estado in listOf("EN_CURSO", "EN CURSO", "EN_PROCESO", "EN PROCESO", "ACEPTADA", "ASIGNADA")) {
                Intent(this, SeguimientoClienteActivity::class.java).apply {
                    putExtra("idSolicitud", idSolicitud)
                }
            } else {
                Intent(this, DetallesSolicitudCreadaClienteActivity::class.java).apply {
                    putExtra("idSolicitud", idSolicitud)
                }
            }
            startActivity(intent)
        }

        binding.rvServiciosCliente.apply {
            layoutManager = LinearLayoutManager(this@HistorialServiciosClienteActivity)
            adapter = this@HistorialServiciosClienteActivity.adapter
        }
    }

    private fun observarViewModel() {
        viewModel.cargando.observe(this) { cargando ->
            binding.progressServiciosCliente.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.countEnCurso.observe(this) { count ->
            binding.tvCountEnCursoCliente.text = count.toString()
        }

        viewModel.countPendientes.observe(this) { count ->
            binding.tvCountPendientesCliente.text = count.toString()
        }

        viewModel.countRealizados.observe(this) { count ->
            binding.tvCountRealizadosCliente.text = count.toString()
        }

        viewModel.serviciosFiltrados.observe(this) { documentos ->
            adapter.actualizarSolicitudes(documentos)

            val esVacia = documentos.isEmpty()
            binding.layoutSinServiciosCliente.visibility = if (esVacia) View.VISIBLE else View.GONE
            binding.rvServiciosCliente.visibility = if (esVacia) View.GONE else View.VISIBLE

            actualizarEstiloFiltros()
        }
    }

    private fun configurarBuscador() {
        binding.etBuscarServicioCliente.doAfterTextChanged { texto ->
            viewModel.setBusqueda(texto?.toString().orEmpty())
        }
    }

    private fun configurarFiltros() {
        binding.btnFiltroEnCursoCliente.setOnClickListener {
            filtroActual = "EN_CURSO"
            viewModel.setFiltro("EN_CURSO")
        }

        binding.btnFiltroPendientesCliente.setOnClickListener {
            filtroActual = "ASIGNADAS"
            viewModel.setFiltro("ASIGNADAS")
        }

        binding.btnFiltroRealizadosCliente.setOnClickListener {
            filtroActual = "REALIZADOS"
            viewModel.setFiltro("REALIZADOS")
        }
    }

    private fun actualizarEstiloFiltros() {
        val filtros = listOf(
            Triple(binding.btnFiltroEnCursoCliente, binding.tvLabelEnCursoCliente, "EN_CURSO"),
            Triple(binding.btnFiltroPendientesCliente, binding.tvLabelPendientesCliente, "ASIGNADAS"),
            Triple(binding.btnFiltroRealizadosCliente, binding.tvLabelRealizadosCliente, "REALIZADOS")
        )

        filtros.forEach { (contenedor, label, filtro) ->
            val seleccionado = filtroActual == filtro
            val colorFondo = if (seleccionado) R.color.ui_brand else R.color.ui_surface
            val colorTexto = if (seleccionado) android.R.color.white else R.color.ui_text

            contenedor.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, colorFondo)
            )
            label.setTextColor(ContextCompat.getColor(this, colorTexto))
        }
    }

    private fun configurarBotones() {
        binding.btnRegresarServiciosCliente.setOnClickListener {
            finish()
        }

        binding.btnPerfilServiciosClienteTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.selectedItemId = R.id.navServicios
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                    true
                }
                R.id.navSolicitudes -> {
                    startActivity(Intent(this, HistorialSolicitudesClienteActivity::class.java))
                    finish()
                    true
                }
                R.id.navServicios -> true
                R.id.navMensajes -> {
                    val intent = Intent(this, HistorialChatsActivity::class.java).apply {
                        putExtra("EXTRA_IS_PRO", false)
                    }
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navPerfil -> {
                    startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
