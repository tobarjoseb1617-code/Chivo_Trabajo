
package com.cherodevscode.chivo_trabajo.ui.cliente

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityCrearSolicitudBinding
import com.cherodevscode.chivo_trabajo.utils.CategoriasConfig
import com.cherodevscode.chivo_trabajo.utils.PinGenerator
import com.google.android.material.card.MaterialCardView
import java.util.Calendar
import java.util.Locale

class CrearSolicitudActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearSolicitudBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: CrearSolicitudViewModel

    // Control del modo creación o edición.
    private var modoEdicion = false
    private var idSolicitudEditar = ""
    private var solicitudOriginal: Solicitud? = null
    private var datosCargados = false
    private var cargando = false

    private var categoriaSeleccionada = ""
    private var latitudServicio = 13.6894
    private var longitudServicio = -89.2361
    private var direccionServicio = ""
    private var ciudadServicio = "San Salvador"
    private var fechaServicioSeleccionada = "Lo antes posible"
    private var horaServicioSeleccionada = "Inmediato"
    private var presupuestoSeleccionado = "$15 – $30"

    // Fotografías nuevas y guardadas anteriormente.
    private val listaFotosUris = mutableListOf<Uri>()
    private val fotosExistentes = mutableListOf<String>()

    private lateinit var botonAgregarFoto: View
    private lateinit var tarjetaLimiteFotos: View

    private val seleccionarFotosLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->

        if (uris.isNotEmpty()) {

            val disponibles = 5 -
                    fotosExistentes.size -
                    listaFotosUris.size

            if (disponibles <= 0) {
                mostrarMensaje("Ya tienes el máximo de cinco fotografías")
                return@registerForActivityResult
            }

            val nuevas = uris
                .distinct()
                .filter { it !in listaFotosUris }
                .take(disponibles)

            listaFotosUris.addAll(nuevas)
            actualizarGaleriaPreview()

            mostrarMensaje(
                "${fotosExistentes.size + listaFotosUris.size} de 5 fotografías"
            )
        }
    }

    private val ubicacionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {

            result.data?.let { data ->

                latitudServicio = data.getDoubleExtra(
                    "EXTRA_LAT",
                    latitudServicio
                )

                longitudServicio = data.getDoubleExtra(
                    "EXTRA_LNG",
                    longitudServicio
                )

                direccionServicio = data.getStringExtra(
                    "EXTRA_DIRECCION"
                ) ?: direccionServicio

                ciudadServicio = data.getStringExtra(
                    "EXTRA_CIUDAD"
                ) ?: ciudadServicio

                actualizarVistaUbicacion()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)

        viewModel = ViewModelProvider(this)[
            CrearSolicitudViewModel::class.java
        ]

        idSolicitudEditar = intent.getStringExtra("idSolicitud")
            .orEmpty()

        modoEdicion = idSolicitudEditar.isNotBlank()

        binding.tvTituloFormularioSolicitud.text =
            if (modoEdicion) "Editar solicitud" else "Nueva solicitud"

        botonAgregarFoto = binding.btnAgregarFoto
        tarjetaLimiteFotos = binding.layoutGaleriaFotos.getChildAt(1)

        observarViewModel()

        configurarCategoria()
        configurarDescripcion()
        configurarFotografias()
        configurarUbicacion()
        configurarFechaHora()
        configurarPresupuesto()
        configurarBotonPublicar()

        if (modoEdicion) {

            binding.btnPublicarSolicitud.text = "Cargando solicitud..."
            binding.btnPublicarSolicitud.isEnabled = false

            viewModel.cargarSolicitudParaEditar(idSolicitudEditar)

        } else {

            destacarCardFecha(1)
            destacarCardPresupuesto(1)
        }
    }

    // Observa la creación, carga y edición de solicitudes.
    private fun observarViewModel() {

        viewModel.cargando.observe(this) { estaCargando ->

            cargando = estaCargando

            binding.btnPublicarSolicitud.isEnabled =
                !estaCargando && (!modoEdicion || datosCargados)

            binding.btnPublicarSolicitud.text = when {

                estaCargando && modoEdicion && !datosCargados ->
                    "Cargando solicitud..."

                estaCargando && modoEdicion ->
                    "Guardando cambios..."

                estaCargando ->
                    "Publicando..."

                modoEdicion ->
                    "Guardar cambios"

                else ->
                    "Continuar →"
            }
        }

        viewModel.resultadoCreacion.observe(this) { resultado ->

            if (!modoEdicion) {

                resultado.onSuccess { idSolicitud ->

                    val pinSeguridad = PinGenerator.generarPin()

                    mostrarMensaje("¡Solicitud publicada con éxito!")

                    val intent = Intent(
                        this,
                        SeguimientoActivity::class.java
                    ).apply {

                        putExtra("EXTRA_PIN", pinSeguridad)
                        putExtra("EXTRA_SOLICITUD_ID", idSolicitud)
                    }

                    startActivity(intent)
                    finish()

                }.onFailure { error ->

                    mostrarMensaje(
                        "Error al publicar: ${error.message}"
                    )
                }
            }
        }

        viewModel.solicitudParaEditar.observe(this) { resultado ->

            if (!modoEdicion) return@observe

            resultado.onSuccess { solicitud ->

                if (solicitud == null) {
                    mostrarMensaje("No se encontró la solicitud")
                    finish()
                    return@onSuccess
                }

                val uidActual = authRepository.usuarioActual()?.uid

                // Comprueba que el cliente pueda editar la solicitud.
                if (solicitud.clientId != uidActual) {

                    mostrarMensaje(
                        "No tienes permiso para editar esta solicitud"
                    )

                    finish()
                    return@onSuccess
                }

                if (
                    solicitud.estado != "PENDIENTE" ||
                    !solicitud.profesionalId.isNullOrBlank()
                ) {

                    mostrarMensaje(
                        "Esta solicitud ya no se puede editar"
                    )

                    finish()
                    return@onSuccess
                }

                if (!datosCargados) {

                    solicitudOriginal = solicitud
                    cargarDatosEnFormulario(solicitud)

                    datosCargados = true

                    binding.btnPublicarSolicitud.isEnabled = !cargando
                    binding.btnPublicarSolicitud.text = "Guardar cambios"
                }

            }.onFailure { error ->

                mostrarMensaje(
                    "Error al cargar solicitud: ${error.message}"
                )

                finish()
            }
        }

        viewModel.resultadoEdicion.observe(this) { resultado ->

            if (!modoEdicion) return@observe

            resultado.onSuccess {

                mostrarMensaje("¡Solicitud actualizada correctamente!")

                setResult(RESULT_OK)
                finish()

            }.onFailure { error ->

                mostrarMensaje(
                    "Error al guardar cambios: ${error.message}"
                )
            }
        }
    }

    // Carga los datos originales en el formulario de edición.
    private fun cargarDatosEnFormulario(solicitud: Solicitud) {

        categoriaSeleccionada = solicitud.categoryId

        binding.tvCategoriaServicio.text = categoriaSeleccionada

        binding.tvCategoriaServicio.setTextColor(
            Color.parseColor("#0B2545")
        )

        binding.etDetallesSolicitud.setText(solicitud.descripcion)

        direccionServicio = solicitud.direccion
        ciudadServicio = solicitud.ciudad
        latitudServicio = solicitud.latitud
        longitudServicio = solicitud.longitud

        actualizarVistaUbicacion()

        fechaServicioSeleccionada = solicitud.fechaServicio
        horaServicioSeleccionada = solicitud.horaServicio

        when (fechaServicioSeleccionada) {
            "Lo antes posible" -> destacarCardFecha(1)
            "Mañana" -> destacarCardFecha(2)
            else -> destacarCardFecha(3)
        }

        presupuestoSeleccionado = solicitud.presupuesto

        when (presupuestoSeleccionado) {
            "$15 – $30" -> destacarCardPresupuesto(1)
            "$30 – $50" -> destacarCardPresupuesto(2)
            else -> destacarCardPresupuesto(3)
        }

        // Recupera las fotografías almacenadas en Cloudinary.
        fotosExistentes.clear()
        listaFotosUris.clear()

        if (solicitud.fotosProblema.isNotEmpty()) {

            fotosExistentes.addAll(
                solicitud.fotosProblema.take(5)
            )

        } else if (solicitud.fotoProblema.isNotBlank()) {

            fotosExistentes.add(solicitud.fotoProblema)
        }

        actualizarGaleriaPreview()
    }

    private fun actualizarVistaUbicacion() {

        binding.tvDireccionUbicacion.text = ciudadServicio
        binding.tvDetalleUbicacion.text = direccionServicio
        binding.tvTextoMapa.text = "📍 $ciudadServicio"
    }

    private fun configurarCategoria() {

        val seleccionarCategoria = {

            val categorias = CategoriasConfig.listaCategorias
                .toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Seleccione una categoría")
                .setItems(categorias) { _, posicion ->

                    categoriaSeleccionada = categorias[posicion]

                    binding.tvCategoriaServicio.text =
                        categoriaSeleccionada

                    binding.tvCategoriaServicio.setTextColor(
                        Color.parseColor("#0B2545")
                    )
                }
                .show()
        }

        binding.btnCambiarCategoria.setOnClickListener {
            seleccionarCategoria()
        }

        binding.tvCategoriaServicio.setOnClickListener {
            seleccionarCategoria()
        }
    }

    private fun configurarDescripcion() {

        binding.etDetallesSolicitud.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val cantidad = s?.length ?: 0

                    binding.tvContadorCaracteres.text =
                        "$cantidad / 300 caracteres"
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {}
            }
        )
    }

    private fun configurarFotografias() {

        binding.btnAgregarFoto.setOnClickListener {

            val cantidadActual =
                fotosExistentes.size + listaFotosUris.size

            if (cantidadActual >= 5) {

                mostrarMensaje("Solo puedes agregar cinco fotografías")

            } else {

                seleccionarFotosLauncher.launch("image/*")
            }
        }
    }

    // Actualiza la galería y permite quitar fotos existentes o nuevas.
    private fun actualizarGaleriaPreview() {

        val galeria = binding.layoutGaleriaFotos

        galeria.removeAllViews()
        galeria.addView(botonAgregarFoto)

        fotosExistentes.toList().forEach { url ->

            agregarMiniatura(Uri.parse(url)) {

                if (!cargando) {
                    fotosExistentes.remove(url)
                    actualizarGaleriaPreview()
                }
            }
        }

        listaFotosUris.toList().forEach { uri ->

            agregarMiniatura(uri) {

                if (!cargando) {
                    listaFotosUris.remove(uri)
                    actualizarGaleriaPreview()
                }
            }
        }

        val cantidadTotal =
            fotosExistentes.size + listaFotosUris.size

        if (cantidadTotal < 5) {
            galeria.addView(tarjetaLimiteFotos)
        }

        botonAgregarFoto.visibility =
            if (cantidadTotal >= 5) View.GONE else View.VISIBLE
    }

    // Crea la miniatura con el botón X para quitarla.
    private fun agregarMiniatura(
        uri: Uri,
        alEliminar: () -> Unit
    ) {

        val tarjeta = MaterialCardView(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                76.dpToPx(),
                76.dpToPx()
            ).apply {
                marginEnd = 8.dpToPx()
            }

            radius = 12f * resources.displayMetrics.density
            cardElevation = 2f
        }

        val contenedor = FrameLayout(this).apply {

            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val imagen = ImageView(this).apply {

            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        Glide.with(this)
            .load(uri)
            .into(imagen)

        val botonEliminar = TextView(this).apply {

            text = "×"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            contentDescription = "Quitar fotografía"

            setBackgroundColor(
                Color.parseColor("#CCB3261E")
            )

            layoutParams = FrameLayout.LayoutParams(
                28.dpToPx(),
                28.dpToPx(),
                Gravity.TOP or Gravity.END
            )

            setOnClickListener {
                alEliminar()
            }
        }

        contenedor.addView(imagen)
        contenedor.addView(botonEliminar)
        tarjeta.addView(contenedor)

        binding.layoutGaleriaFotos.addView(tarjeta)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun configurarUbicacion() {

        val abrirMapa = {

            val intent = Intent(
                this,
                SeleccionarUbicacionActivity::class.java
            )

            ubicacionLauncher.launch(intent)
        }

        binding.tvEditarUbicacion.setOnClickListener {
            abrirMapa()
        }

        binding.cardUbicacion.setOnClickListener {
            abrirMapa()
        }
    }

    private fun configurarFechaHora() {

        binding.cardLoAntesPosible.setOnClickListener {

            fechaServicioSeleccionada = "Lo antes posible"
            horaServicioSeleccionada = "Inmediato"

            destacarCardFecha(1)
        }

        binding.cardManana.setOnClickListener {

            fechaServicioSeleccionada = "Mañana"
            horaServicioSeleccionada = "Durante la mañana o tarde"

            destacarCardFecha(2)
        }

        binding.cardProgramarFecha.setOnClickListener {
            mostrarSelectorFechaHora()
        }
    }

    private fun mostrarSelectorFechaHora() {

        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                val fecha = String.format(
                    Locale.getDefault(),
                    "%02d/%02d/%d",
                    day,
                    month + 1,
                    year
                )

                TimePickerDialog(
                    this,
                    { _, hora, minuto ->

                        val horaFormateada = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hora,
                            minuto
                        )

                        fechaServicioSeleccionada = fecha
                        horaServicioSeleccionada = horaFormateada

                        destacarCardFecha(3)

                        mostrarMensaje(
                            "Programado para $fecha a las $horaFormateada"
                        )
                    },
                    calendario.get(Calendar.HOUR_OF_DAY),
                    calendario.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).apply {

            datePicker.minDate = System.currentTimeMillis() - 1000

        }.show()
    }

    private fun destacarCardFecha(opcion: Int) {

        val seleccionado = Color.parseColor("#26FFFFFF")
        val normal = Color.TRANSPARENT

        binding.cardLoAntesPosible.setCardBackgroundColor(
            if (opcion == 1) seleccionado else normal
        )

        binding.cardManana.setCardBackgroundColor(
            if (opcion == 2) seleccionado else normal
        )

        binding.cardProgramarFecha.setCardBackgroundColor(
            if (opcion == 3) seleccionado else normal
        )
    }

    private fun configurarPresupuesto() {

        binding.cardPresupuestoUno.setOnClickListener {

            presupuestoSeleccionado = "$15 – $30"
            destacarCardPresupuesto(1)
        }

        binding.cardPresupuestoDos.setOnClickListener {

            presupuestoSeleccionado = "$30 – $50"
            destacarCardPresupuesto(2)
        }

        binding.cardPresupuestoTres.setOnClickListener {

            presupuestoSeleccionado = "Flexible A convenir"
            destacarCardPresupuesto(3)
        }
    }

    private fun destacarCardPresupuesto(opcion: Int) {

        val seleccionado = Color.parseColor("#26FFFFFF")
        val normal = Color.TRANSPARENT

        binding.cardPresupuestoUno.setCardBackgroundColor(
            if (opcion == 1) seleccionado else normal
        )

        binding.cardPresupuestoDos.setCardBackgroundColor(
            if (opcion == 2) seleccionado else normal
        )

        binding.cardPresupuestoTres.setCardBackgroundColor(
            if (opcion == 3) seleccionado else normal
        )
    }

    // Valida los campos y guarda una solicitud nueva o editada.
    private fun configurarBotonPublicar() {

        binding.btnPublicarSolicitud.setOnClickListener {

            if (cargando) return@setOnClickListener

            val descripcion = binding.etDetallesSolicitud
                .text
                .toString()
                .trim()

            if (categoriaSeleccionada.isBlank()) {

                mostrarMensaje("Selecciona una categoría")
                return@setOnClickListener
            }

            if (descripcion.isBlank()) {

                mostrarMensaje("Describe el problema")
                return@setOnClickListener
            }

            if (direccionServicio.isBlank()) {

                mostrarMensaje("Selecciona una ubicación")
                return@setOnClickListener
            }

            val usuarioActual = authRepository.usuarioActual()

            if (usuarioActual == null) {

                mostrarMensaje("Debes iniciar sesión")
                return@setOnClickListener
            }

            if (modoEdicion) {

                val original = solicitudOriginal

                if (original == null) {

                    mostrarMensaje("No se cargó la solicitud")
                    return@setOnClickListener
                }

                // Conserva los campos protegidos de la solicitud original.
                val solicitudEditada = original.copy(
                    categoryId = categoriaSeleccionada,
                    descripcion = descripcion,
                    direccion = direccionServicio,
                    ciudad = ciudadServicio,
                    latitud = latitudServicio,
                    longitud = longitudServicio,
                    fechaServicio = fechaServicioSeleccionada,
                    horaServicio = horaServicioSeleccionada,
                    presupuesto = presupuestoSeleccionado,
                    fotoProblema = fotosExistentes.firstOrNull().orEmpty(),
                    fotosProblema = fotosExistentes.toList()
                )

                viewModel.editarSolicitud(
                    contentResolver,
                    solicitudEditada,
                    listaFotosUris.toList(),
                    usuarioActual.uid
                )

            } else {

                val solicitudNueva = Solicitud(
                    clientId = usuarioActual.uid,
                    categoryId = categoriaSeleccionada,
                    descripcion = descripcion,
                    direccion = direccionServicio,
                    ciudad = ciudadServicio,
                    latitud = latitudServicio,
                    longitud = longitudServicio,
                    fechaServicio = fechaServicioSeleccionada,
                    horaServicio = horaServicioSeleccionada,
                    presupuesto = presupuestoSeleccionado,
                    estado = "PENDIENTE",
                    profesionalId = null
                )

                viewModel.publicarSolicitud(
                    contentResolver,
                    solicitudNueva,
                    listaFotosUris.toList()
                )
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
