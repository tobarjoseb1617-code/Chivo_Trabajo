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
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.BuildConfig
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityCrearSolicitudBinding
import com.cherodevscode.chivo_trabajo.utils.CategoriasConfig
import com.cherodevscode.chivo_trabajo.utils.PinGenerator
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale

class CrearSolicitudActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearSolicitudBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    // Variables de estado del formulario
    private var categoriaSeleccionada: String = ""
    private val listaFotosUris = mutableListOf<Uri>() // Soporte para múltiples fotos (hasta 5)
    private var latitudServicio: Double = 13.6894
    private var longitudServicio: Double = -89.2361
    private var direccionServicio: String = ""
    private var ciudadServicio: String = "San Salvador"
    private var fechaServicioSeleccionada: String = "Lo antes posible"
    private var horaServicioSeleccionada: String = "Inmediato"
    private var presupuestoSeleccionado: String = "$15 – $30"

    // Selector múltiple de fotografías (hasta 5 imágenes con previsualización)
    private val seleccionarFotosLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            listaFotosUris.clear()
            listaFotosUris.addAll(uris.take(5)) // Límite máximo de 5 fotos
            actualizarGaleriaPreview()
            Toast.makeText(this, "✓ ${listaFotosUris.size} fotografía(s) adjuntada(s)", Toast.LENGTH_SHORT).show()
        }
    }

    // Receptor para el resultado de la pantalla de ubicación (SeleccionarUbicacionActivity)
    private val ubicacionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                latitudServicio = data.getDoubleExtra("EXTRA_LAT", 13.6894)
                longitudServicio = data.getDoubleExtra("EXTRA_LNG", -89.2361)
                direccionServicio = data.getStringExtra("EXTRA_DIRECCION") ?: "Ubicación seleccionada"
                ciudadServicio = data.getStringExtra("EXTRA_CIUDAD") ?: "San Salvador"

                // Actualizar UI con la dirección elegida
                binding.tvDireccionUbicacion.text = ciudadServicio
                binding.tvDetalleUbicacion.text = direccionServicio
                binding.tvTextoMapa.text = "📍 $ciudadServicio"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        // Configurar secciones del formulario
        configurarCategoria()
        configurarDescripcion()
        configurarFotografias()
        configurarUbicacion()
        configurarFechaHora()
        configurarPresupuesto()
        configurarBotonPublicar()
    }

    // 1. SELECCIÓN DE CATEGORÍA DE SERVICIO
    private fun configurarCategoria() {
        val seleccionarCat = {
            val categoriasArray = CategoriasConfig.listaCategorias.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Seleccione una Categoría")
                .setItems(categoriasArray) { _, which ->
                    categoriaSeleccionada = categoriasArray[which]
                    binding.tvCategoriaServicio.text = categoriaSeleccionada
                    binding.tvCategoriaServicio.setTextColor(Color.parseColor("#0B2545"))
                }
                .show()
        }

        binding.btnCambiarCategoria.setOnClickListener { seleccionarCat() }
        binding.tvCategoriaServicio.setOnClickListener { seleccionarCat() }
    }

    // 2. DESCRIPCIÓN DEL PROBLEMA (Con contador de 300 caracteres)
    private fun configurarDescripcion() {
        binding.etDetallesSolicitud.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val len = s?.length ?: 0
                binding.tvContadorCaracteres.text = "$len / 300 caracteres"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 3. FOTOGRAFÍAS (Selector múltiple y previsualización en miniatura)
    private fun configurarFotografias() {
        binding.btnAgregarFoto.setOnClickListener {
            seleccionarFotosLauncher.launch("image/*")
        }
    }

    private fun actualizarGaleriaPreview() {
        val layoutGaleria = binding.layoutGaleriaFotos
        // Mantener el botón de agregar y el límite, e insertar las miniaturas de previsualización
        // Guardamos las primeras dos vistas (botón agregar y tarjeta de límite)
        val botonAgregar = layoutGaleria.getChildAt(0)
        val tarjetaLimite = layoutGaleria.getChildAt(1)

        layoutGaleria.removeAllViews()
        layoutGaleria.addView(botonAgregar)

        // Dibujar miniaturas de cada foto seleccionada
        listaFotosUris.forEach { uri ->
            val cardThumbnail = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(68.dpToPx(), 68.dpToPx()).apply {
                    marginEnd = 8.dpToPx()
                }
                radius = 12f * resources.displayMetrics.density
                cardElevation = 2f
            }

            val imageView = ImageView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(this).load(uri).into(imageView)
            cardThumbnail.addView(imageView)
            layoutGaleria.addView(cardThumbnail)
        }

        if (listaFotosUris.size < 5) {
            layoutGaleria.addView(tarjetaLimite)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    // 4. UBICACIÓN (Abre SeleccionarUbicacionActivity con mapa y buscador)
    private fun configurarUbicacion() {
        val abrirMapa = {
            val intent = Intent(this, SeleccionarUbicacionActivity::class.java)
            ubicacionLauncher.launch(intent)
        }

        binding.tvEditarUbicacion.setOnClickListener { abrirMapa() }
        binding.cardUbicacion.setOnClickListener { abrirMapa() }
    }

    // 5. FECHA Y HORA (Tarjetas interactivas con sombreado sutil de selección)
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
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val fechaFormateada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
            
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                fechaServicioSeleccionada = fechaFormateada
                horaServicioSeleccionada = horaFormateada
                Toast.makeText(this, "Programado para: $fechaFormateada a las $horaFormateada", Toast.LENGTH_LONG).show()
                destacarCardFecha(3)
            }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true).show()

        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun destacarCardFecha(opcion: Int) {
        // Usar un sutil sombreado/tinte blanco semitransparente (#26FFFFFF) que no oculte el texto
        val tinteSeleccionado = Color.parseColor("#26FFFFFF")
        val colorNormal = Color.TRANSPARENT
        binding.cardLoAntesPosible.setCardBackgroundColor(if (opcion == 1) tinteSeleccionado else colorNormal)
        binding.cardManana.setCardBackgroundColor(if (opcion == 2) tinteSeleccionado else colorNormal)
        binding.cardProgramarFecha.setCardBackgroundColor(if (opcion == 3) tinteSeleccionado else colorNormal)
    }

    // 6. PRESUPUESTO (Rangos aproximados con sombreado sutil legible)
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
        val tinteSeleccionado = Color.parseColor("#26FFFFFF")
        val colorNormal = Color.TRANSPARENT
        binding.cardPresupuestoUno.setCardBackgroundColor(if (opcion == 1) tinteSeleccionado else colorNormal)
        binding.cardPresupuestoDos.setCardBackgroundColor(if (opcion == 2) tinteSeleccionado else colorNormal)
        binding.cardPresupuestoTres.setCardBackgroundColor(if (opcion == 3) tinteSeleccionado else colorNormal)
    }

    // 7. BOTÓN PUBLICAR / CONTINUAR (Sube fotos a Cloudinary y guarda en Firestore)
    private fun configurarBotonPublicar() {
        binding.btnPublicarSolicitud.setOnClickListener {
            val descripcion = binding.etDetallesSolicitud.text.toString().trim()

            if (categoriaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Por favor seleccione una categoría de servicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (descripcion.isEmpty()) {
                Toast.makeText(this, "Por favor describa el problema", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (direccionServicio.isEmpty()) {
                Toast.makeText(this, "Por favor seleccione una ubicación en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = authRepository.usuarioActual()
            if (currentUser == null) {
                Toast.makeText(this, "Debe iniciar sesión para publicar una solicitud", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Publicando solicitud...", Toast.LENGTH_SHORT).show()

            // Si hay fotografías adjuntas, subir la primera (o principal) a Cloudinary
            if (listaFotosUris.isNotEmpty()) {
                subirFotoYGuardarSolicitud(currentUser.uid, descripcion)
            } else {
                guardarSolicitudEnFirestore(currentUser.uid, descripcion, "")
            }
        }
    }

    // Subir la fotografía principal a Cloudinary
    private fun subirFotoYGuardarSolicitud(clientId: String, descripcion: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var uploadSuccess = false
            var secureUrl: String? = null
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            val presetConfig = BuildConfig.CLOUDINARY_UPLOAD_PRESET
            val presets = listOf(presetConfig, "chivo_trabajo", "chivo_trabajo_preset", "ml_default", "preset_chivo")

            for (preset in presets) {
                try {
                    val urlString = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.doInput = true

                    val boundary = "Boundary-${System.currentTimeMillis()}"
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                    val outputStream = DataOutputStream(connection.outputStream)
                    outputStream.writeBytes("--$boundary\r\n")
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                    outputStream.writeBytes("$preset\r\n")

                    outputStream.writeBytes("--$boundary\r\n")
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"solicitud_${System.currentTimeMillis()}.jpg\"\r\n")
                    outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")

                    contentResolver.openInputStream(listaFotosUris[0])?.use { inputStream ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }

                    outputStream.writeBytes("\r\n--$boundary--\r\n")
                    outputStream.flush()
                    outputStream.close()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                        val regex = "\"secure_url\"\\s*:\\s*\"(.*?)\"".toRegex()
                        val matchResult = regex.find(responseString)
                        secureUrl = matchResult?.groups?.get(1)?.value?.replace("\\/", "/")
                        if (!secureUrl.isNullOrBlank()) {
                            uploadSuccess = true
                            break
                        }
                    }
                } catch (ex: Exception) {
                    // Siguiente preset
                }
            }

            withContext(Dispatchers.Main) {
                if (uploadSuccess && !secureUrl.isNullOrBlank()) {
                    guardarSolicitudEnFirestore(clientId, descripcion, secureUrl!!)
                } else {
                    Toast.makeText(this@CrearSolicitudActivity, "Error al subir la foto a Cloudinary", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Guardar solicitud en Firestore en la colección "solicitudes"
    private fun guardarSolicitudEnFirestore(clientId: String, descripcion: String, fotoUrl: String) {
        val solicitud = Solicitud(
            clientId = clientId,
            categoryId = categoriaSeleccionada,
            descripcion = descripcion,
            fotoProblema = fotoUrl,
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

        CoroutineScope(Dispatchers.IO).launch {
            val resultado = firestoreRepository.guardarSolicitud(solicitud)
            withContext(Dispatchers.Main) {
                resultado.onSuccess { idSolicitud ->
                    val pinSeguridad = PinGenerator.generarPin()
                    Toast.makeText(this@CrearSolicitudActivity, "¡Solicitud publicada con éxito!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@CrearSolicitudActivity, SeguimientoActivity::class.java).apply {
                        putExtra("EXTRA_PIN", pinSeguridad)
                        putExtra("EXTRA_SOLICITUD_ID", idSolicitud)
                    }
                    startActivity(intent)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@CrearSolicitudActivity, "Error al publicar solicitud: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
