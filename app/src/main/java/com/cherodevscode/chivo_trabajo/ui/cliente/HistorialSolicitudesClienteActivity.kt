package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialSolicitudesClienteBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class HistorialSolicitudesClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialSolicitudesClienteBinding
    private lateinit var adapter: SolicitudesClienteAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var listenerSolicitudes: ListenerRegistration? = null

    private var todasLasSolicitudes = listOf<DocumentSnapshot>()
    private var filtroActual = "TODAS"
    private var busquedaActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistorialSolicitudesClienteBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        configurarRecyclerView()
        configurarNavegacion()
        configurarBotones()
        configurarFiltros()
        configurarBuscador()
    }

    override fun onStart() {
        super.onStart()
        escucharSolicitudes()
    }

    override fun onStop() {
        listenerSolicitudes?.remove()
        listenerSolicitudes = null
        super.onStop()
    }

    private fun configurarRecyclerView() {

        adapter = SolicitudesClienteAdapter { idSolicitud ->

            val intent = Intent(
                this,
                DetallesSolicitudCreadaClienteActivity::class.java
            )

            intent.putExtra("idSolicitud", idSolicitud)

            startActivity(intent)
        }

        binding.rvSolicitudesCliente.layoutManager =
            LinearLayoutManager(this)

        binding.rvSolicitudesCliente.adapter = adapter

        binding.rvSolicitudesCliente.isNestedScrollingEnabled = false
    }

    private fun escucharSolicitudes() {

        listenerSolicitudes?.remove()
        listenerSolicitudes = null

        val uid = auth.currentUser?.uid

        if (uid == null) {
            todasLasSolicitudes = emptyList()
            binding.progressSolicitudesCliente.visibility = View.GONE
            mostrarSolicitudes()
            Toast.makeText(
                this,
                "Debes iniciar sesión para ver tus solicitudes",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        binding.progressSolicitudesCliente.visibility = View.VISIBLE

        listenerSolicitudes = firestore
            .collection("Solicitudes")
            .whereEqualTo("clientId", uid)
            .addSnapshotListener { snapshot, error ->

                binding.progressSolicitudesCliente.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al cargar solicitudes: ${error.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addSnapshotListener
                }

                todasLasSolicitudes = snapshot
                    ?.documents
                    ?.sortedByDescending {
                        it.getTimestamp("fechaCreacion")?.seconds ?: 0L
                    }
                    ?: emptyList()

                mostrarSolicitudes()
            }
    }

    private fun configurarBuscador() {

        binding.etBuscarSolicitudCliente.doAfterTextChanged { texto ->
            busquedaActual = texto?.toString()?.trim().orEmpty()
            mostrarSolicitudes()
        }
    }

    private fun configurarFiltros() {

        binding.btnFiltroTodasCliente.setOnClickListener {
            filtroActual = "TODAS"
            mostrarSolicitudes()
        }

        binding.btnFiltroAbiertasCliente.setOnClickListener {
            filtroActual = "ABIERTAS"
            mostrarSolicitudes()
        }

        binding.btnFiltroProcesoCliente.setOnClickListener {
            filtroActual = "EN_PROCESO"
            mostrarSolicitudes()
        }

        binding.btnFiltroFinalizadasCliente.setOnClickListener {
            filtroActual = "FINALIZADAS"
            mostrarSolicitudes()
        }
    }

    private fun mostrarSolicitudes() {

        val solicitudesFiltradas = todasLasSolicitudes.filter { documento ->

            val estado = documento.getString("estado")
                ?.uppercase(Locale.ROOT)
                .orEmpty()

            val coincideEstado = when (filtroActual) {

                "ABIERTAS" -> estado in listOf(
                    "PENDIENTE",
                    "ABIERTA",
                    "PUBLICADA"
                )

                "EN_PROCESO" -> estado in listOf(
                    "EN_PROCESO",
                    "EN PROCESO",
                    "ACEPTADA",
                    "ASIGNADA"
                )

                "FINALIZADAS" -> estado in listOf(
                    "FINALIZADA",
                    "FINALIZADO",
                    "COMPLETADA"
                )

                else -> true
            }

            val textoBusqueda = listOf(
                documento.getString("descripcion").orEmpty(),
                documento.getString("categoryId").orEmpty(),
                documento.getString("idSolicitud").orEmpty(),
                documento.id
            ).joinToString(" ")

            val coincideBusqueda = textoBusqueda.contains(
                busquedaActual,
                ignoreCase = true
            )

            coincideEstado && coincideBusqueda
        }

        adapter.actualizarSolicitudes(solicitudesFiltradas)

        val cantidad = solicitudesFiltradas.size

        binding.tvCantidadSolicitudesCliente.text =
            if (cantidad == 1) {
                "1 solicitud"
            } else {
                "$cantidad solicitudes"
            }

        binding.layoutSinSolicitudesCliente.visibility =
            if (cantidad == 0) View.VISIBLE else View.GONE

        binding.rvSolicitudesCliente.visibility =
            if (cantidad > 0) View.VISIBLE else View.GONE

        actualizarEstiloFiltros()
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

            val colorFondo = if (seleccionado) {
                R.color.ui_brand
            } else {
                R.color.ui_surface
            }

            val colorTexto = if (seleccionado) {
                android.R.color.white
            } else {
                R.color.ui_text_secondary
            }

            boton.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(
                        this,
                        colorFondo
                    )
                )

            boton.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                    this,
                    colorTexto
                )
            )
        }
    }


    private fun configurarBotones() {

        binding.btnRegresarHistorialSolicitudesCliente.setOnClickListener {
            finish()
        }

        binding.btnPerfilHistorialClienteTop.setOnClickListener {
            startActivity(
                Intent(this, ConfiguracionPerfilActivity::class.java)
            )
        }

        binding.btnPublicarNuevaSolicitud.setOnClickListener {
            startActivity(
                Intent(this, CrearSolicitudActivity::class.java)
            )
        }
    }

    private fun configurarNavegacion() {

        binding.bottomNavigation.selectedItemId =
            R.id.navSolicitudesSolCliente

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.navInicioSolCliente -> {
                    startActivity(
                        Intent(this, InicioActivity::class.java)
                    )
                    finish()
                    true
                }

                R.id.navSolicitudesSolCliente -> true

                R.id.navServiciosSolCliente -> {
                    startActivity(
                        Intent(this, HistorialDeServiciosActivity::class.java)
                    )
                    finish()
                    true
                }

                R.id.navMensajesSolCliente -> {
                    startActivity(
                        Intent(this, HistorialChatsActivity::class.java)
                    )
                    finish()
                    true
                }

                R.id.navPerfilSolCliente -> {
                    startActivity(
                        Intent(this, ConfiguracionPerfilActivity::class.java)
                    )
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}
