package com.cherodevscode.chivo_trabajo.ui.profesional

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
// Los iconos Material se resuelven con el R generado del módulo app.
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.BuildConfig
import com.cherodevscode.chivo_trabajo.data.model.Portafolio
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityPortafolioProfesionalBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

// Apariencia: los colores creados desde Kotlin respetan el modo elegido.
class PortafolioProfesionalActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    private lateinit var binding: ActivityPortafolioProfesionalBinding
    private lateinit var portafolioAdapter: PortafolioAdapter

    private val listaPortafolio = mutableListOf<Portafolio>()

    // Variables de estado para los diálogos modales (declaradas a nivel de clase por reglas de ciclo de vida de Android)
    private var imagenSeleccionadaUri: Uri? = null
    private var btnElegirFotoRef: android.widget.Button? = null

    // Launcher único declarado correctamente a nivel de clase
    private val seleccionarImagenLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionadaUri = uri
            btnElegirFotoRef?.text = "✓ Imagen seleccionada con éxito"
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPortafolioProfesionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Evitar que el header quede debajo de la barra de notificaciones
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.layoutHeaderPortafolio.setPadding(
                16.dpToPx(),
                systemBars.top,
                16.dpToPx(),
                0
            )
            insets
        }

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        configurarRecyclerView()
        configurarEventos()
        cargarPortafolio()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun configurarRecyclerView() {
        portafolioAdapter = PortafolioAdapter(listaPortafolio) { trabajo ->
            mostrarOpcionesTrabajo(trabajo)
        }

        binding.rvPortafolio.apply {
            layoutManager = GridLayoutManager(this@PortafolioProfesionalActivity, 2)
            adapter = portafolioAdapter
            setHasFixedSize(false)
        }
    }

    private fun configurarEventos() {
        binding.btnRegresarPortafolio.setOnClickListener {
            finish()
        }

        // Botón: Agregar Trabajo
        binding.btnAgregarTrabajo.setOnClickListener {
            mostrarDialogoAgregarTrabajo()
        }

        // Botón: Agregar Título / Acreditación
        binding.btnAgregarTituloPortafolio.setOnClickListener {
            mostrarDialogoAgregarTitulo()
        }

        binding.btnAgregarPrimerTrabajo.setOnClickListener {
            mostrarDialogoAgregarTrabajo()
        }
    }

    // Diálogo modal para agregar un Trabajo (Imagen + Título + Descripción)
    private fun mostrarDialogoAgregarTrabajo() {
        imagenSeleccionadaUri = null

        val inputTitulo = TextInputEditText(this).apply {
            hint = "Título del trabajo (Ej: Instalación eléctrica)"
        }
        val inputDescripcion = TextInputEditText(this).apply {
            hint = "Descripción breve del trabajo realizado"
            minLines = 2
        }
        val btnElegirFoto = MaterialButton(this).apply {
            text = "Seleccionar foto del trabajo"
            // MaterialButton conserva el ripple y usa colorPrimary/colorOnPrimary.
            setIconResource(R.drawable.ic_m3_camera)
        }
        btnElegirFotoRef = btnElegirFoto

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val margen = (16 * resources.displayMetrics.density).toInt()
            setPadding(margen, margen, margen, margen)
            addView(btnElegirFoto, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 12 })
            addView(campoMaterial(inputTitulo), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 12 })
            addView(campoMaterial(inputDescripcion), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        btnElegirFoto.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }

        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar Trabajo")
            .setView(contenedor)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = inputTitulo.text.toString().trim()
                val desc = inputDescripcion.text.toString().trim()
                val uri = imagenSeleccionadaUri

                if (titulo.isEmpty() || desc.isEmpty() || uri == null) {
                    Toast.makeText(this, "Complete todos los campos y seleccione una imagen", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                subirTrabajoACloudinaryYGuardar(uri, titulo, desc)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Diálogo modal para agregar un Título / Acreditación (Imagen + Título)
    private fun mostrarDialogoAgregarTitulo() {
        imagenSeleccionadaUri = null

        val inputTitulo = TextInputEditText(this).apply {
            hint = "Título o Especialidad (Ej: Técnico en Redes)"
        }
        val btnElegirFoto = MaterialButton(this).apply {
            text = "Seleccionar acreditación"
            // MaterialButton conserva el ripple y usa colorPrimary/colorOnPrimary.
            setIconResource(R.drawable.ic_m3_camera)
        }
        btnElegirFotoRef = btnElegirFoto

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val margen = (16 * resources.displayMetrics.density).toInt()
            setPadding(margen, margen, margen, margen)
            addView(btnElegirFoto, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 12 })
            addView(campoMaterial(inputTitulo), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        btnElegirFoto.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }

        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar Título o Acreditación")
            .setView(contenedor)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = inputTitulo.text.toString().trim()
                val uri = imagenSeleccionadaUri

                if (titulo.isEmpty() || uri == null) {
                    Toast.makeText(this, "Ingrese el título y seleccione una imagen", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                subirTrabajoACloudinaryYGuardar(uri, titulo, "Acreditación / Título Profesional")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Subir imagen a Cloudinary y guardar en Firestore
    private fun subirTrabajoACloudinaryYGuardar(imageUri: Uri, titulo: String, descripcion: String) {
        val currentUser = authRepository.usuarioActual()
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

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
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"portafolio_${System.currentTimeMillis()}.jpg\"\r\n")
                    outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")

                    contentResolver.openInputStream(imageUri)?.use { inputStream ->
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
                } catch (e: Exception) {
                    // Siguiente preset
                }
            }

            withContext(Dispatchers.Main) {
                if (uploadSuccess && !secureUrl.isNullOrBlank()) {
                    guardarEnFirestoreFinal(currentUser.uid, secureUrl!!, titulo, descripcion)
                } else {
                    Toast.makeText(this@PortafolioProfesionalActivity, "Error al subir imagen a Cloudinary", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun guardarEnFirestoreFinal(uid: String, url: String, titulo: String, descripcion: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val resultado = firestoreRepository.guardarTrabajoPortafolio(uid, url, titulo, descripcion)
            withContext(Dispatchers.Main) {
                resultado.onSuccess {
                    Toast.makeText(this@PortafolioProfesionalActivity, "¡Guardado con éxito!", Toast.LENGTH_SHORT).show()
                    cargarPortafolio()
                }.onFailure { e ->
                    Toast.makeText(this@PortafolioProfesionalActivity, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cargarPortafolio() {
        val currentUser = authRepository.usuarioActual() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val resultado = firestoreRepository.obtenerPortafolio(currentUser.uid)
            withContext(Dispatchers.Main) {
                resultado.onSuccess { trabajos ->
                    listaPortafolio.clear()
                    listaPortafolio.addAll(trabajos)
                    portafolioAdapter.actualizarLista(listaPortafolio)
                    actualizarEstadoPortafolio()
                }.onFailure { e ->
                    Toast.makeText(this@PortafolioProfesionalActivity, "Error al cargar portafolio: ${e.message}", Toast.LENGTH_LONG).show()
                    actualizarEstadoPortafolio()
                }
            }
        }
    }

    private fun actualizarEstadoPortafolio() {
        val cantidad = listaPortafolio.size
        binding.tvCantidadTrabajos.text = cantidad.toString()

        if (listaPortafolio.isEmpty()) {
            binding.layoutPortafolioVacio.visibility = View.VISIBLE
            binding.rvPortafolio.visibility = View.GONE
        } else {
            binding.layoutPortafolioVacio.visibility = View.GONE
            binding.rvPortafolio.visibility = View.VISIBLE
        }
    }

    private fun mostrarOpcionesTrabajo(trabajo: Portafolio) {
        val opciones = arrayOf("Editar descripción", "Eliminar trabajo")
        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Opciones del trabajo")
            .setItems(opciones) { _, opcion ->
                when (opcion) {
                    0 -> editarDescripcionTrabajo(trabajo)
                    1 -> confirmarEliminarTrabajo(trabajo)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarDescripcionTrabajo(trabajo: Portafolio) {
        val inputDescripcion = TextInputEditText(this).apply {
            setText(trabajo.descripcion)
            setSelection(text?.length ?: 0)
            minLines = 3
        }

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val margen = (20 * resources.displayMetrics.density).toInt()
            setPadding(margen, margen / 2, margen, 0)
            addView(campoMaterial(inputDescripcion), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Editar descripción")
            .setView(contenedor)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaDesc = inputDescripcion.text.toString().trim()
                if (nuevaDesc.isEmpty()) return@setPositiveButton

                val currentUser = authRepository.usuarioActual() ?: return@setPositiveButton
                CoroutineScope(Dispatchers.IO).launch {
                    val resultado = firestoreRepository.actualizarDescripcionPortafolio(currentUser.uid, trabajo.idFoto, nuevaDesc)
                    withContext(Dispatchers.Main) {
                        resultado.onSuccess {
                            Toast.makeText(this@PortafolioProfesionalActivity, "Actualizado", Toast.LENGTH_SHORT).show()
                            cargarPortafolio()
                        }.onFailure { e ->
                            Toast.makeText(this@PortafolioProfesionalActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminarTrabajo(trabajo: Portafolio) {
        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar trabajo")
            .setMessage("¿Deseas eliminar este elemento del portafolio?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarTrabajo(trabajo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarTrabajo(trabajo: Portafolio) {
        val currentUser = authRepository.usuarioActual() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val resultado = firestoreRepository.eliminarTrabajoPortafolio(currentUser.uid, trabajo.idFoto)
            withContext(Dispatchers.Main) {
                resultado.onSuccess {
                    Toast.makeText(this@PortafolioProfesionalActivity, "Eliminado", Toast.LENGTH_SHORT).show()
                    cargarPortafolio()
                }.onFailure { e ->
                    Toast.makeText(this@PortafolioProfesionalActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /** Envuelve los campos del diálogo sin cambiar sus referencias ni la lógica de guardado. */
    private fun campoMaterial(campo: EditText): TextInputLayout {
        val espacio = (16 * resources.displayMetrics.density).toInt()
        return TextInputLayout(this).apply {
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            hint = campo.hint
            campo.hint = null
            campo.background = null
            campo.minHeight = (56 * resources.displayMetrics.density).toInt()
            campo.setPadding(espacio, espacio, espacio, espacio)
            addView(campo, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
    }
}
