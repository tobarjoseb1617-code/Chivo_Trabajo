package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.databinding.ActivityHistorialDeServiciosBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.cliente.CrearSolicitudActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistorialDeServiciosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialDeServiciosBinding

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistorialDeServiciosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar las solicitudes automáticamente al abrir Servicios
        cargarServiciosAsignados()

        binding.tabAsignados.setOnClickListener {
            binding.contenedorServiciosAsignados.visibility = View.VISIBLE
            cargarServiciosAsignados()
        }

        binding.bottomNavigation.selectedItemId =
            R.id.navServiciosServicios

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.navInicioServicios -> {
                    startActivity(
                        Intent(this, InicioProfesionalActivity::class.java)
                    )
                    finish()
                }

                R.id.navSolicitudesServicios -> {
                    startActivity(
                        Intent(
                            this,
                            SolicitudesTrabajosCercanosActivity::class.java
                        )
                    )
                    finish()
                }

                R.id.navServiciosServicios -> {
                    binding.contenedorServiciosAsignados.visibility =
                        View.VISIBLE
                    cargarServiciosAsignados()
                }

                R.id.navMensajesServicios -> {
                    startActivity(
                        Intent(this, HistorialChatsActivity::class.java)
                    )
                    finish()
                }

                R.id.navPerfilServicios -> {
                    startActivity(
                        Intent(this, ConfiguracionPerfilActivity::class.java)
                    )
                    finish()
                }
            }

            item.itemId == R.id.navServiciosServicios
        }

        binding.btnRegresarServicios.setOnClickListener {
            val intent = Intent(
                this,
                InicioProfesionalActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            startActivity(intent)
            finish()
        }

        binding.btnPerfilServiciosTop.setOnClickListener {
            startActivity(
                Intent(this, ConfiguracionPerfilActivity::class.java)
            )
        }

        binding.btnSeguimientoServicio.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    GestionarServicioEnCursoActivity::class.java
                )
            )
        }

        binding.btnVerOrdenServicio.setOnClickListener {
            Toast.makeText(
                this,
                "Viendo orden #SRV-2041",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnVerRecibo1.setOnClickListener {
            Toast.makeText(
                this,
                "Generando recibo de pago para #SRV-1988",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnPedirDeNuevo1.setOnClickListener {
            startActivity(
                Intent(this, CrearSolicitudActivity::class.java)
            )
        }

        binding.btnVerDetallesHistoria2.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DetallesSolicitudTrabajoProfesionalActivity::class.java
                )
            )
        }

        binding.btnVerDetallesHistoria3.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DetallesSolicitudTrabajoProfesionalActivity::class.java
                )
            )
        }
    }

    private fun cargarServiciosAsignados() {
        val uidProfesional = auth.currentUser?.uid

        if (uidProfesional == null) {
            Toast.makeText(
                this,
                "No se encontró la sesión del profesional",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        db.collection("Solicitudes")
            .whereEqualTo("profesionalId", uidProfesional)
            .whereEqualTo("estado", "ASIGNADA")
            .get()
            .addOnSuccessListener { documentos ->

                binding.txtCantidadAsignados.text =
                    documentos.size().toString()

                val contenedor = binding.listaServiciosAsignados
                contenedor.removeAllViews()

                binding.contenedorServiciosAsignados.visibility =
                    View.VISIBLE

                if (documentos.isEmpty) {
                    val mensaje = TextView(this).apply {
                        text = "No tienes trabajos asignados por ahora."
                        textSize = 14f
                        gravity = Gravity.CENTER
                        setTextColor(
                            getColor(R.color.ui_text_secondary)
                        )
                        setPadding(
                            dp(16),
                            dp(28),
                            dp(16),
                            dp(28)
                        )
                    }

                    contenedor.addView(mensaje)
                    return@addOnSuccessListener
                }

                documentos.forEach { documento ->
                    val categoria =
                        documento.getString("categoryId")
                            ?: "Servicio solicitado"

                    val descripcion =
                        documento.getString("descripcion")
                            ?: "Sin descripción disponible"

                    val ciudad =
                        documento.getString("ciudad")
                            ?: "Ubicación no disponible"

                    val direccion =
                        documento.getString("direccion")
                            ?: "Dirección no disponible"

                    val tarjeta = MaterialCardView(this).apply {
                        radius = dp(18).toFloat()
                        cardElevation = dp(2).toFloat()
                        strokeWidth = dp(1)
                        strokeColor = getColor(R.color.ui_outline)
                        setCardBackgroundColor(
                            getColor(R.color.ui_surface)
                        )

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = dp(14)
                        }
                    }

                    val contenido = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(
                            dp(16),
                            dp(16),
                            dp(16),
                            dp(16)
                        )
                    }

                    val filaTitulo = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val titulo = TextView(this).apply {
                        text = categoria
                        textSize = 17f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(getColor(R.color.ui_text))

                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            marginEnd = dp(8)
                        }
                    }

                    val estado = TextView(this).apply {
                        text = "ASIGNADA"
                        textSize = 10f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(getColor(R.color.ui_success))
                        setPadding(
                            dp(9),
                            dp(6),
                            dp(9),
                            dp(6)
                        )

                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = dp(12).toFloat()
                            setColor(
                                getColor(R.color.ui_background)
                            )
                        }
                    }

                    filaTitulo.addView(titulo)
                    filaTitulo.addView(estado)

                    val textoDescripcion = TextView(this).apply {
                        text = descripcion
                        textSize = 14f
                        setTextColor(getColor(R.color.ui_text))
                        setPadding(0, dp(12), 0, dp(14))
                    }

                    val separador = View(this).apply {
                        setBackgroundColor(
                            getColor(R.color.ui_outline)
                        )

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(1)
                        )
                    }

                    val etiquetaUbicacion = TextView(this).apply {
                        text = "UBICACIÓN"
                        textSize = 11f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(
                            getColor(R.color.ui_text_secondary)
                        )
                        setPadding(0, dp(12), 0, dp(5))
                    }

                    val textoUbicacion = TextView(this).apply {
                        text = "$ciudad\n$direccion"
                        textSize = 13f
                        setTextColor(
                            getColor(R.color.ui_text_secondary)
                        )
                    }

                    val botonIniciar = MaterialButton(this).apply {
                        text = "Iniciar servicio"
                        textSize = 14f
                        isAllCaps = false
                        cornerRadius = dp(24)
                        minHeight = dp(48)

                        setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                getColor(R.color.ui_primary)
                            )
                        )

                        setTextColor(
                            getColor(R.color.ui_background)
                        )

                        setOnClickListener {
                            Toast.makeText(
                                this@HistorialDeServiciosActivity,
                                "Servicio seleccionado: ${documento.id}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = dp(16)
                        }
                    }

                    contenido.addView(filaTitulo)
                    contenido.addView(textoDescripcion)
                    contenido.addView(separador)
                    contenido.addView(etiquetaUbicacion)
                    contenido.addView(textoUbicacion)
                    contenido.addView(botonIniciar)

                    tarjeta.addView(contenido)
                    contenedor.addView(tarjeta)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Error al cargar servicios: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun dp(valor: Int): Int {
        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }
}
