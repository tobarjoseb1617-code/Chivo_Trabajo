package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivitySolicitudesTrabajosCercanosBinding
import com.cherodevscode.chivo_trabajo.databinding.ItemEncabezadoSolicitudesProBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SolicitudesTrabajosCercanosActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudesTrabajosCercanosBinding
    private lateinit var adapter: SolicitudesDisponiblesAdapter
    private lateinit var encabezadoAdapter: EncabezadoAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var listenerSolicitudes: ListenerRegistration? = null
    private var solicitudesDisponibles = listOf<DocumentSnapshot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySolicitudesTrabajosCercanosBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        configurarRecyclerView()
        configurarNavegacion()
        escucharSolicitudes()
    }

    private fun configurarRecyclerView() {

        encabezadoAdapter = EncabezadoAdapter()

        adapter = SolicitudesDisponiblesAdapter(
            mutableListOf()
        ) { idSolicitud ->

            val intent = Intent(
                this,
                DetallesSolicitudTrabajoProfesionalActivity::class.java
            ).apply {
                putExtra("idSolicitud", idSolicitud)
            }

            startActivity(intent)
        }

        binding.rvSolicitudesDisponiblesPro.apply {
            layoutManager = LinearLayoutManager(
                this@SolicitudesTrabajosCercanosActivity
            )
            adapter = ConcatAdapter(encabezadoAdapter, this@SolicitudesTrabajosCercanosActivity.adapter)
            setHasFixedSize(false)
        }
    }

    private fun escucharSolicitudes() {

        val uidActual = auth.currentUser?.uid

        if (uidActual == null) {
            Toast.makeText(
                this,
                "Debes iniciar sesión",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        encabezadoAdapter.mostrarCarga(true)

        listenerSolicitudes = firestore
            .collection("Solicitudes")
            .whereEqualTo("estado", "PENDIENTE")
            .addSnapshotListener { snapshot, error ->

                encabezadoAdapter.mostrarCarga(false)

                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al cargar solicitudes: ${error.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addSnapshotListener
                }

                solicitudesDisponibles = snapshot?.documents
                    ?.filter { documento ->
                        documento.getString("clientId") != uidActual &&
                                documento.getString("profesionalId").isNullOrBlank()
                    }
                    ?.sortedByDescending {
                        it.getTimestamp("fechaCreacion")?.seconds ?: 0L
                    }
                    ?: emptyList()

                mostrarSolicitudes(solicitudesDisponibles)
            }
    }

    private fun mostrarSolicitudes(
        solicitudes: List<DocumentSnapshot>
    ) {
        adapter.actualizarSolicitudes(solicitudes)
        encabezadoAdapter.actualizarCantidad(solicitudes.size)
    }

    private fun configurarNavegacion() {

        binding.bottomNavigation.selectedItemId =
            R.id.navSolicitudesPro

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.navInicioPro -> {
                    startActivity(Intent(this, InicioProfesionalActivity::class.java))
                    finish()
                    true
                }

                R.id.navSolicitudesPro -> true

                R.id.navServiciosPro -> {
                    startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
                    finish()
                    true
                }

                R.id.navMensajesPro -> {
                    startActivity(Intent(this, HistorialChatsActivity::class.java))
                    finish()
                    true
                }

                R.id.navPerfilPro -> {
                    startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroy() {
        listenerSolicitudes?.remove()
        super.onDestroy()
    }

    // Encabezado del RecyclerView
    inner class EncabezadoAdapter :
        RecyclerView.Adapter<EncabezadoAdapter.EncabezadoViewHolder>() {

        private var cantidad = 0
        private var cargando = false

        inner class EncabezadoViewHolder(
            val binding: ItemEncabezadoSolicitudesProBinding
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): EncabezadoViewHolder {

            val binding = ItemEncabezadoSolicitudesProBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            return EncabezadoViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: EncabezadoViewHolder,
            position: Int
        ) {
            val b = holder.binding

            b.tvCantidadSolicitudesPro.text = "$cantidad disponibles"
            b.progressSolicitudesPro.visibility =
                if (cargando) View.VISIBLE else View.GONE
            b.tvSinSolicitudesPro.visibility =
                if (!cargando && cantidad == 0) View.VISIBLE else View.GONE

            b.btnRegresarSolicitudesPro.setOnClickListener {
                startActivity(
                    Intent(
                        this@SolicitudesTrabajosCercanosActivity,
                        InicioProfesionalActivity::class.java
                    )
                )
                finish()
            }

            b.btnPerfilSolicitudesTop.setOnClickListener {
                startActivity(
                    Intent(
                        this@SolicitudesTrabajosCercanosActivity,
                        ConfiguracionPerfilActivity::class.java
                    )
                )
            }

            b.btnVerPropuestasEnviadas.setOnClickListener {
                startActivity(
                    Intent(
                        this@SolicitudesTrabajosCercanosActivity,
                        HistorialChatsActivity::class.java
                    )
                )
            }

            b.btnFiltroTodasPro.setOnClickListener {
                mostrarSolicitudes(solicitudesDisponibles)
            }

            b.btnFiltroRecientesPro.setOnClickListener {
                mostrarSolicitudes(
                    solicitudesDisponibles.sortedByDescending {
                        it.getTimestamp("fechaCreacion")?.seconds ?: 0L
                    }
                )
            }

            b.btnFiltroCategoriaPro.setOnClickListener {
                Toast.makeText(
                    this@SolicitudesTrabajosCercanosActivity,
                    "Próximamente: solicitudes de tu especialidad",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun getItemCount(): Int = 1

        fun actualizarCantidad(nuevaCantidad: Int) {
            cantidad = nuevaCantidad
            notifyItemChanged(0)
        }

        fun mostrarCarga(mostrar: Boolean) {
            cargando = mostrar
            notifyItemChanged(0)
        }
    }
}
