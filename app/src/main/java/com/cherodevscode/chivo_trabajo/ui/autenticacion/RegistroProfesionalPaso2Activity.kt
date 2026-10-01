package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.data.model.Profesional
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityRegistroProfesionalPaso2Binding
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity
import com.cherodevscode.chivo_trabajo.utils.CategoriasConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Apariencia: los colores creados desde Kotlin respetan el modo elegido.
class RegistroProfesionalPaso2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroProfesionalPaso2Binding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    // Especialidades seleccionadas (múltiples)
    private val especialidadesSeleccionadas = mutableSetOf<String>()

    // Sub-servicios específicos seleccionados
    private val serviciosSeleccionados = mutableSetOf<String>()

    // Años de experiencia
    private var aniosExperiencia = 0

    // Municipios seleccionados
    private val zonasSeleccionadas = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistroProfesionalPaso2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        // Configurar componentes de la pantalla
        configurarEspecialidadesMultiples()
        configurarExperiencia()
        configurarZonas()
        configurarDescripcion()

        // Botón regresar
        binding.btnRegresarRegistroProPaso2.setOnClickListener {
            finish()
        }

        // Botón completar registro
        binding.btnCompletarRegistroPro.setOnClickListener {

            if (especialidadesSeleccionadas.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor seleccione al menos una especialidad",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (serviciosSeleccionados.isEmpty()) {
                Toast.makeText(
                    this,
                    "Seleccione al menos un servicio específico",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (zonasSeleccionadas.isEmpty()) {
                Toast.makeText(
                    this,
                    "Seleccione al menos un municipio",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val uid = authRepository.usuarioActual()?.uid

            if (uid == null) {
                Toast.makeText(
                    this,
                    "Error: No hay sesión activa",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val especialidadPrincipal = especialidadesSeleccionadas.first()
            val descripcion = binding.etDescripcionPro.text.toString().trim()

            // Crear el objeto Profesional con múltiples especialidades y servicios dinámicos
            val profesional = Profesional(
                uid = uid,
                aniosExperiencia = aniosExperiencia.toLong(),
                calificacionPromedio = 0.0,
                cantidadCalificaciones = 0,
                descripcion = descripcion,
                disponible = false,
                especialidad = especialidadPrincipal,
                especialidades = especialidadesSeleccionadas.toList(),
                latitud = 0.0,
                longitud = 0.0,
                serviciosOfrecidos = serviciosSeleccionados.toList(),
                verificado = false,
                zonaTrabajo = zonasSeleccionadas.toList()
            )

            CoroutineScope(Dispatchers.Main).launch {

                // Guardar profesional en Firestore
                val profesionalRes = withContext(Dispatchers.IO) {
                    firestoreRepository.guardarProfesional(profesional)
                }

                profesionalRes.onSuccess {

                    // Obtener perfil actual del usuario
                    val perfilRes = withContext(Dispatchers.IO) {
                        firestoreRepository.obtenerPerfilUsuario(uid)
                    }

                    perfilRes.onSuccess { usuarioActual ->

                        if (usuarioActual != null) {

                            // Actualizar tipo de usuario a PROFESIONAL
                            val usuarioActualizado = usuarioActual.copy(
                                tipoUsuario = "PROFESIONAL"
                            )

                            val saveRes = withContext(Dispatchers.IO) {
                                firestoreRepository.guardarPerfilUsuario(
                                    usuarioActualizado
                                )
                            }

                            saveRes.onSuccess {

                                Toast.makeText(
                                    this@RegistroProfesionalPaso2Activity,
                                    "¡Perfil profesional completado con éxito!",
                                    Toast.LENGTH_LONG
                                ).show()

                                irAInicioPro()

                            }.onFailure { e ->

                                Toast.makeText(
                                    this@RegistroProfesionalPaso2Activity,
                                    "Error al actualizar usuario: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {

                            Toast.makeText(
                                this@RegistroProfesionalPaso2Activity,
                                "¡Perfil profesional guardado!",
                                Toast.LENGTH_LONG
                            ).show()

                            irAInicioPro()
                        }

                    }.onFailure { e ->

                        Toast.makeText(
                            this@RegistroProfesionalPaso2Activity,
                            "Error al obtener usuario: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }.onFailure { e ->

                    Toast.makeText(
                        this@RegistroProfesionalPaso2Activity,
                        "Error al guardar profesional: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // SELECCIÓN MÚLTIPLE DE ESPECIALIDADES Y CARGA DINÁMICA DE SUB-SERVICIOS
    private fun configurarEspecialidadesMultiples() {
        binding.etEspecialidad.isFocusable = false
        binding.etEspecialidad.isClickable = true
        binding.etEspecialidad.setOnClickListener {
            mostrarSelectorEspecialidadesMultiples()
        }
    }

    private fun mostrarSelectorEspecialidadesMultiples() {
        val categoriasArray = CategoriasConfig.listaCategorias.toTypedArray()
        val checkedItems = BooleanArray(categoriasArray.size) { i ->
            especialidadesSeleccionadas.contains(categoriasArray[i])
        }

        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Seleccione sus Especialidades (Puede elegir varias)")
            .setMultiChoiceItems(categoriasArray, checkedItems) { _, which, isChecked ->
                val categoria = categoriasArray[which]
                if (isChecked) {
                    especialidadesSeleccionadas.add(categoria)
                } else {
                    especialidadesSeleccionadas.remove(categoria)
                }
            }
            .setPositiveButton("Aceptar") { _, _ ->
                if (especialidadesSeleccionadas.isNotEmpty()) {
                    binding.etEspecialidad.setText(especialidadesSeleccionadas.joinToString(", "))
                } else {
                    binding.etEspecialidad.setText("")
                }
                actualizarServiciosDinamicos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // PINTAR DINÁMICAMENTE LOS SERVICIOS ESPECÍFICOS SEGÚN LAS ESPECIALIDADES ELEGIDAS
    private fun actualizarServiciosDinamicos() {
        val layoutContainer = binding.layoutServiciosDinamicos
        layoutContainer.removeAllViews()
        serviciosSeleccionados.clear()
        actualizarContadorServicios()

        // Obtener todos los sub-servicios de las especialidades seleccionadas
        val subServiciosDisponibles = mutableListOf<String>()
        especialidadesSeleccionadas.forEach { esp ->
            CategoriasConfig.mapaCategoriasServicios[esp]?.let { lista ->
                subServiciosDisponibles.addAll(lista)
            }
        }

        if (subServiciosDisponibles.isEmpty()) {
            val tvVacio = TextView(this).apply {
                text = "Seleccione arriba una especialidad para ver sus servicios específicos."
                textSize = 12f
                setTextColor(androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_text_secondary))
            }
            layoutContainer.addView(tvVacio)
            return
        }

        // Crear filas de 2 elementos para los servicios específicos
        var filaActual: LinearLayout? = null
        subServiciosDisponibles.forEachIndexed { index, servicio ->
            if (index % 2 == 0) {
                filaActual = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 0, 0, 8) }
                }
                layoutContainer.addView(filaActual)
            }

            val tvServicio = TextView(this).apply {
                text = servicio
                textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_text_secondary))
                setBackgroundColor(androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_surface_variant))
                setPadding(24, 16, 24, 16)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    if (index % 2 == 0) setMargins(0, 0, 8, 0) else setMargins(8, 0, 0, 0)
                }
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    toggleServicioEspecifico(this, servicio)
                }
            }

            filaActual?.addView(tvServicio)
        }
    }

    private fun toggleServicioEspecifico(vista: TextView, servicio: String) {
        if (serviciosSeleccionados.contains(servicio)) {
            serviciosSeleccionados.remove(servicio)
            vista.text = servicio
            vista.setTextColor(androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_text_secondary))
            vista.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_surface_variant))
        } else {
            serviciosSeleccionados.add(servicio)
            vista.text = "✓ $servicio"
            vista.setTextColor(Color.WHITE)
            vista.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_brand))
        }
        actualizarContadorServicios()
    }


    private fun actualizarContadorServicios() {
        val cantidad = serviciosSeleccionados.size
        binding.tvContadorServicios.text = "$cantidad seleccionados"
    }


    // EXPERIENCIA LABORAL

    private fun configurarExperiencia() {

        binding.btnMenosExperiencia.setOnClickListener {

            if (aniosExperiencia > 0) {

                aniosExperiencia--

                actualizarExperiencia()
            }
        }

        binding.btnMasExperiencia.setOnClickListener {

            if (aniosExperiencia < 50) {

                aniosExperiencia++

                actualizarExperiencia()
            }
        }
    }


    private fun actualizarExperiencia() {

        val texto = if (aniosExperiencia == 1) {
            "1 año"
        } else {
            "$aniosExperiencia años"
        }

        binding.tvAniosExperiencia.text = texto
    }


    // ZONAS DE COBERTURA

    private fun configurarZonas() {

        // Botón para agregar municipios
        binding.tvAgregarMunicipio.setOnClickListener {
            mostrarMunicipios()
        }

        // Actualizar la pantalla al entrar
        actualizarZonas()
    }


    private fun eliminarZona(zona: String) {

        zonasSeleccionadas.remove(zona)

        actualizarZonas()

        Toast.makeText(
            this,
            "$zona eliminado",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun mostrarMunicipios() {

        val municipiosDisponibles = arrayOf(
            "San Salvador",
            "Sonsonate",
            "Sonzacate",
            "Soyapango",
            "Acajutla",
            "Apopa"
        )

        // Mostrar solamente los que todavía no han sido seleccionados
        val municipiosFiltrados = municipiosDisponibles.filter {
            !zonasSeleccionadas.contains(it)
        }.toTypedArray()

        if (municipiosFiltrados.isEmpty()) {

            Toast.makeText(
                this,
                "Ya seleccionaste todos los municipios disponibles",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar municipio")
            .setItems(municipiosFiltrados) { _, which ->

                val municipioSeleccionado =
                    municipiosFiltrados[which]

                zonasSeleccionadas.add(
                    municipioSeleccionado
                )

                // Actualizar la pantalla
                actualizarZonas()

                Toast.makeText(
                    this,
                    "$municipioSeleccionado agregado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }


    private fun actualizarZonas() {

        val layout = binding.layoutZonasSeleccionadas

        // Limpiar las zonas que se muestran actualmente
        layout.removeAllViews()

        // Crear visualmente cada municipio seleccionado
        zonasSeleccionadas.forEach { zona ->

            val textView = TextView(this)

            textView.text = "$zona  ×"
            textView.textSize = 11f
            textView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            textView.setTextColor(
                androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_primary)
            )

            textView.setBackgroundColor(
                androidx.core.content.ContextCompat.getColor(this@RegistroProfesionalPaso2Activity, com.cherodevscode.chivo_trabajo.R.color.ui_info_background)
            )

            textView.setPadding(
                10,
                6,
                10,
                6
            )

            val parametros = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            parametros.bottomMargin = 8

            textView.layoutParams = parametros

            // Al tocar el municipio se elimina
            textView.setOnClickListener {
                eliminarZona(zona)
            }

            layout.addView(textView)
        }
    }


    // DESCRIPCIÓN

    private fun configurarDescripcion() {

        binding.etDescripcionPro.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val cantidad = s?.length ?: 0

                    binding.tvContadorDescripcion.text =
                        "$cantidad / 300"
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {
                }
            }
        )
    }


    // IR AL INICIO

    private fun irAInicioPro() {

        val intent = Intent(
            this,
            InicioProfesionalActivity::class.java
        )

        startActivity(intent)
        finish()
    }
}
