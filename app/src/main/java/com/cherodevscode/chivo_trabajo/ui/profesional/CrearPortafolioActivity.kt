package com.cherodevscode.chivo_trabajo.ui.profesional

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.BuildConfig
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityCrearPortafolioBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class CrearPortafolioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearPortafolioBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    // URIs para la foto del trabajo y el carnet/acreditación (estrictamente imágenes)
    private var fotoTrabajoUri: Uri? = null
    private var carnetDocumentoUri: Uri? = null

    // Selector para la foto del trabajo con previsualización inmediata
    private val seleccionarFotoTrabajoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoTrabajoUri = uri
            binding.tvEstadoFotoTrabajo.text = "¡Foto seleccionada!"
            binding.layoutPlaceholderTrabajo.visibility = View.GONE
            binding.ivPreviewTrabajo.visibility = View.VISIBLE
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.ivPreviewTrabajo)
        }
    }

    // Selector para el carnet o título profesional (restringido a imágenes)
    private val seleccionarCarnetLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            carnetDocumentoUri = uri
            binding.tvNombreArchivoAdjunto.text = "Imagen de acreditación adjuntada"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPortafolioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        // Cargar descripción previa del profesional al iniciar la pantalla
        cargarDatosProfesional()

        // Botón regresar
        binding.btnRegresarCrearPortafolio.setOnClickListener {
            finish()
        }

        // Selección de foto de trabajo
        binding.layoutSubirFotoTrabajo.setOnClickListener {
            seleccionarFotoTrabajoLauncher.launch("image/*")
        }

        // Selección de carnet o documento profesional (solo imágenes)
        binding.layoutAdjuntarDocumento.setOnClickListener {
            seleccionarCarnetLauncher.launch("image/*")
        }

        // Botón guardar portafolio y registrar trabajo
        binding.btnGuardarCrearPortafolio.setOnClickListener {
            val sobreTi = binding.etSobreTiCrearPortafolio.text.toString().trim()
            val tituloTrabajo = binding.etTituloTrabajo.text.toString().trim()
            val descripcionTrabajo = binding.etDescripcionTrabajo.text.toString().trim()

            if (tituloTrabajo.isEmpty() || descripcionTrabajo.isEmpty()) {
                Toast.makeText(this, "Por favor complete el título y descripción del trabajo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fotoTrabajoUri == null) {
                Toast.makeText(this, "Por favor suba una foto del trabajo realizado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = authRepository.usuarioActual()
            if (currentUser == null) {
                Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Guardando trabajo y actualizando perfil...", Toast.LENGTH_SHORT).show()

            // Subir imagen a Cloudinary y guardar en Firestore
            subirTrabajoYActualizarPerfil(currentUser.uid, sobreTi, fotoTrabajoUri!!, tituloTrabajo, descripcionTrabajo)
        }
    }

    // Cargar datos actuales del profesional (descripción) desde Firestore
    private fun cargarDatosProfesional() {
        val currentUser = authRepository.usuarioActual() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val resultado = firestoreRepository.obtenerProfesional(currentUser.uid)
            withContext(Dispatchers.Main) {
                resultado.onSuccess { profesional ->
                    if (profesional != null) {
                        if (profesional.descripcion.isNotBlank()) {
                            binding.etSobreTiCrearPortafolio.setText(profesional.descripcion)
                        }
                    }
                }
            }
        }
    }

    // Subir imagen a Cloudinary con respaldo de múltiples presets
    private fun subirTrabajoYActualizarPerfil(uid: String, nuevaDescripcion: String, imageUri: Uri, tituloTrabajo: String, descripcionTrabajo: String) {
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

                    // Escribir preset
                    outputStream.writeBytes("--$boundary\r\n")
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                    outputStream.writeBytes("$preset\r\n")

                    // Escribir archivo de imagen
                    outputStream.writeBytes("--$boundary\r\n")
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"trabajo_${System.currentTimeMillis()}.jpg\"\r\n")
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
                    // Intentar con el siguiente preset si falla
                }
            }

            withContext(Dispatchers.Main) {
                if (uploadSuccess && !secureUrl.isNullOrBlank()) {
                    // Actualizar descripción y guardar trabajo con título y descripción independientes en Firestore
                    guardarEnFirestoreFinal(uid, nuevaDescripcion, secureUrl, tituloTrabajo, descripcionTrabajo)
                } else {
                    Toast.makeText(this@CrearPortafolioActivity, "Error al subir la imagen a Cloudinary", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Persistir cambios en Firestore y abrir PortafolioProfesionalActivity
    private fun guardarEnFirestoreFinal(uid: String, nuevaDescripcion: String, imageUrl: String, tituloTrabajo: String, descripcionTrabajo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Actualizar la descripción profesional principal si fue modificada
            if (nuevaDescripcion.isNotBlank()) {
                firestoreRepository.actualizarDescripcionProfesional(uid, nuevaDescripcion)
            }

            // Guardar el trabajo en la subcolección Portafolio incluyendo el título y la descripción
            val resultadoTrabajo = firestoreRepository.guardarTrabajoPortafolio(uid, imageUrl, tituloTrabajo, descripcionTrabajo)

            withContext(Dispatchers.Main) {
                resultadoTrabajo.onSuccess {
                    Toast.makeText(this@CrearPortafolioActivity, "¡Portafolio y trabajo guardados con éxito!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@CrearPortafolioActivity, PortafolioProfesionalActivity::class.java)
                    startActivity(intent)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@CrearPortafolioActivity, "Error al guardar trabajo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
