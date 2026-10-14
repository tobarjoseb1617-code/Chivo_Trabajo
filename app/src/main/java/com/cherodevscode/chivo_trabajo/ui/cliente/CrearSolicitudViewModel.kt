package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.BuildConfig
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * ViewModel para gestionar la lógica de negocio de la creación de solicitudes
 * y subida de múltiples imágenes a Cloudinary (Patrón MVVM).
 */
class CrearSolicitudViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    private val _resultadoCreacion = MutableLiveData<Result<String>>()
    val resultadoCreacion: LiveData<Result<String>> get() = _resultadoCreacion


    // Almacena el resultado de la consulta de una solicitud.
    private val _solicitudParaEditar = MutableLiveData<Result<Solicitud?>>()
    val solicitudParaEditar: LiveData<Result<Solicitud?>>
        get() = _solicitudParaEditar

    // Almacena el resultado de la actualización.
    private val _resultadoEdicion = MutableLiveData<Result<Unit>>()
    val resultadoEdicion: LiveData<Result<Unit>>
        get() = _resultadoEdicion


    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    // Publicar solicitud con múltiples fotografías
    fun publicarSolicitud(contentResolver: ContentResolver, solicitud: Solicitud, fotosUris: List<Uri>) {
        _cargando.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val urlsSubidas = mutableListOf<String>()

                // Subir cada foto seleccionada a Cloudinary
                for (uri in fotosUris) {
                    val url = subirFotoACloudinary(contentResolver, uri)
                    if (!url.isNullOrBlank()) {
                        urlsSubidas.add(url)
                    }
                }

                // Asignar foto principal y lista completa de fotos
                val fotoPrincipal = if (urlsSubidas.isNotEmpty()) urlsSubidas[0] else ""
                val solicitudFinal = solicitud.copy(
                    fotoProblema = fotoPrincipal,
                    fotosProblema = urlsSubidas
                )

                val resultado = firestoreRepository.guardarSolicitud(solicitudFinal)

                withContext(Dispatchers.Main) {
                    _cargando.value = false
                    _resultadoCreacion.value = resultado
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _cargando.value = false
                    _resultadoCreacion.value = Result.failure(e)
                }
            }
        }
    }


    // Busca una solicitud existente para mostrar sus datos.
    fun cargarSolicitudParaEditar(idSolicitud: String) {

        _cargando.value = true

        // Ejecuta la consulta sin bloquear la pantalla.
        viewModelScope.launch {

            val resultado = withContext(Dispatchers.IO) {
                firestoreRepository.obtenerSolicitudPorId(idSolicitud)
            }

            _cargando.value = false

            // Envía el resultado a la Activity.
            _solicitudParaEditar.value = resultado
        }
    }

    // Guarda los cambios realizados por el cliente.
    fun editarSolicitud(
        contentResolver: ContentResolver,
        solicitud: Solicitud,
        fotosNuevas: List<Uri>,
        uidCliente: String
    ) {

        // Evita ejecutar varias operaciones al mismo tiempo.
        if (_cargando.value == true) return

        _cargando.value = true

        viewModelScope.launch(Dispatchers.IO) {

            try {

                // Conserva las fotografías existentes.
                val fotosActualizadas = solicitud.fotosProblema.toMutableList()

                // Sube las nuevas fotografías a Cloudinary.
                for (uri in fotosNuevas) {

                    // Utiliza la función que ya existe en el ViewModel.
                    val url = subirFotoACloudinary(contentResolver, uri)

                    // Comprueba que la fotografía se haya subido.
                    if (url.isNullOrBlank()) {
                        throw IllegalStateException(
                            "No se pudo subir una de las fotografías"
                        )
                    }

                    // Agrega la fotografía nueva.
                    fotosActualizadas.add(url)
                }

                if (fotosActualizadas.size > 5) {
                    throw IllegalStateException(
                        "Solo se permiten cinco fotografías por solicitud"
                    )
                }

                // Establece la fotografía principal.
                val fotoPrincipal = fotosActualizadas.firstOrNull().orEmpty()

                // Prepara la solicitud con sus fotografías actualizadas.
                val solicitudActualizada = solicitud.copy(
                    fotoProblema = fotoPrincipal,
                    fotosProblema = fotosActualizadas
                )

                // Actualiza el documento existente en Firestore.
                val resultado = firestoreRepository.actualizarSolicitud(
                    solicitudActualizada,
                    uidCliente
                )

                withContext(Dispatchers.Main) {
                    _cargando.value = false
                    _resultadoEdicion.value = resultado
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    _cargando.value = false
                    _resultadoEdicion.value = Result.failure(e)
                }
            }
        }
    }


    // Subir imagen individual a Cloudinary
    private fun subirFotoACloudinary(contentResolver: ContentResolver, imageUri: Uri): String? {
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
                    val secureUrl = matchResult?.groups?.get(1)?.value?.replace("\\/", "/")
                    if (!secureUrl.isNullOrBlank()) {
                        return secureUrl
                    }
                }
            } catch (e: Exception) {
                // Siguiente preset
            }
        }
        return null
    }
}
