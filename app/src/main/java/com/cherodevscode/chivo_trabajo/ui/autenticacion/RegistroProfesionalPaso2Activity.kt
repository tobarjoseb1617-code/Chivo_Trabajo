package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.data.model.Profesional
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityRegistroProfesionalPaso2Binding
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroProfesionalPaso2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroProfesionalPaso2Binding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    // Servicios seleccionados
    private val serviciosSeleccionados = mutableSetOf<String>()

    // Años de experiencia
    private var aniosExperiencia = 0

    // Municipios seleccionados
    // El profesional comienza sin municipios seleccionados
    private val zonasSeleccionadas = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistroProfesionalPaso2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        // Configurar las diferentes partes de la pantalla
        configurarServicios()
        configurarExperiencia()
        configurarZonas()
        configurarDescripcion()

        // Botón regresar
        binding.btnRegresarRegistroProPaso2.setOnClickListener {
            finish()
        }

        // Botón completar registro
        binding.btnCompletarRegistroPro.setOnClickListener {

            val especialidad = binding.etEspecialidad.text.toString().trim()
            val descripcion = binding.etDescripcionPro.text.toString().trim()

            if (especialidad.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor ingrese su especialidad principal",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (serviciosSeleccionados.isEmpty()) {
                Toast.makeText(
                    this,
                    "Seleccione al menos un servicio",
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

            // Crear el objeto Profesional
            val profesional = Profesional(
                uid = uid,
                aniosExperiencia = aniosExperiencia.toLong(),
                calificacionPromedio = 0.0,
                cantidadCalificaciones = 0,
                descripcion = descripcion,
                disponible = false,
                especialidad = especialidad,
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

                    // Obtener el perfil actual del usuario
                    val perfilRes = withContext(Dispatchers.IO) {
                        firestoreRepository.obtenerPerfilUsuario(uid)
                    }

                    perfilRes.onSuccess { usuarioActual ->

                        if (usuarioActual != null) {

                            // Actualizar solamente el tipo de usuario
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


    // SERVICIOS ESPECÍFICOS

    private fun configurarServicios() {

        binding.tvServicioFugas.setOnClickListener {
            cambiarServicio(
                binding.tvServicioFugas,
                "Reparación de fugas"
            )
        }

        binding.tvServicioGriferia.setOnClickListener {
            cambiarServicio(
                binding.tvServicioGriferia,
                "Instalación de grifería"
            )
        }

        binding.tvServicioDestape.setOnClickListener {
            cambiarServicio(
                binding.tvServicioDestape,
                "Destape de tuberías"
            )
        }

        binding.tvServicioInodoros.setOnClickListener {
            cambiarServicio(
                binding.tvServicioInodoros,
                "Instalación de inodoros"
            )
        }

        binding.tvServicioCisterna.setOnClickListener {
            cambiarServicio(
                binding.tvServicioCisterna,
                "Mantenimiento cisterna"
            )
        }
    }


    private fun cambiarServicio(
        vista: TextView,
        servicio: String
    ) {

        if (serviciosSeleccionados.contains(servicio)) {

            // Deseleccionar
            serviciosSeleccionados.remove(servicio)

            vista.text = servicio
            vista.setTextColor(
                Color.parseColor("#4B5563")
            )

            vista.setBackgroundColor(
                Color.parseColor("#F1F5F9")
            )

        } else {

            // Seleccionar
            serviciosSeleccionados.add(servicio)

            vista.text = "✓ $servicio"
            vista.setTextColor(
                Color.WHITE
            )

            vista.setBackgroundColor(
                Color.parseColor("#0B2545")
            )
        }

        actualizarContadorServicios()
    }


    private fun actualizarContadorServicios() {

        val cantidad = serviciosSeleccionados.size

        binding.tvContadorServicios.text =
            "$cantidad seleccionados"
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

        AlertDialog.Builder(this)
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
                Color.parseColor("#0B2545")
            )

            textView.setBackgroundColor(
                Color.parseColor("#E0F2FE")
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