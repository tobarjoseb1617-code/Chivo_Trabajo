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
import kotlin.random.Random

class CrearSolicitudViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    private val _resultadoCreacion = MutableLiveData<Result<String>>()
    val resultadoCreacion: LiveData<Result<String>> get() = _resultadoCreacion

    private val _solicitudParaEditar = MutableLiveData<Result<Solicitud?>>()
    val solicitudParaEditar: LiveData<Result<Solicitud?>>
        get() = _solicitudParaEditar

    private val _resultadoEdicion = MutableLiveData<Result<Unit>>()
    val resultadoEdicion: LiveData<Result<Unit>>
        get() = _resultadoEdicion

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    fun publicarSolicitud(
        contentResolver: ContentResolver,
        solicitud: Solicitud,
        fotosUris: List<Uri>
    ) {
        _cargando.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val urlsSubidas = mutableListOf<String>()

                for (uri in fotosUris) {
                    val url = subirFotoACloudinary(contentResolver, uri)

                    if (!url.isNullOrBlank()) {
                        urlsSubidas.add(url)
                    }
                }

                val fotoPrincipal = urlsSubidas.firstOrNull().orEmpty()

                // Generar un PIN de cuatro dígitos para la solicitud.
                val pinGenerado = solicitud.pinSeguridad.ifBlank {
                    Random.nextInt(1000, 10000).toString()
                }

                val solicitudFinal = solicitud.copy(
                    fotoProblema = fotoPrincipal,
                    fotosProblema = urlsSubidas,
                    pinSeguridad = pinGenerado
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

    fun cargarSolicitudParaEditar(idSolicitud: String) {

        _cargando.value = true

        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                firestoreRepository.obtenerSolicitudPorId(idSolicitud)
            }

            _cargando.value = false
            _solicitudParaEditar.value = resultado
        }
    }

    fun editarSolicitud(
        contentResolver: ContentResolver,
        solicitud: Solicitud,
        fotosNuevas: List<Uri>,
        uidCliente: String
    ) {

        if (_cargando.value == true) return

        _cargando.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val fotosActualizadas = solicitud.fotosProblema.toMutableList()

                for (uri in fotosNuevas) {

                    val url = subirFotoACloudinary(contentResolver, uri)

                    if (url.isNullOrBlank()) {
                        throw IllegalStateException(
                            "No se pudo subir una de las fotografías"
                        )
                    }

                    fotosActualizadas.add(url)
                }

                if (fotosActualizadas.size > 5) {
                    throw IllegalStateException(
                        "Solo se permiten cinco fotografías por solicitud"
                    )
                }

                val fotoPrincipal = fotosActualizadas.firstOrNull().orEmpty()

                val solicitudActualizada = solicitud.copy(
                    fotoProblema = fotoPrincipal,
                    fotosProblema = fotosActualizadas
                )

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

    private fun subirFotoACloudinary(
        contentResolver: ContentResolver,
        imageUri: Uri
    ): String? {

        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val presetConfig = BuildConfig.CLOUDINARY_UPLOAD_PRESET

        val presets = listOf(
            presetConfig,
            "chivo_trabajo",
            "chivo_trabajo_preset",
            "ml_default",
            "preset_chivo"
        )

        for (preset in presets) {
            var connection: HttpURLConnection? = null

            try {
                val urlString =
                    "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.doInput = true

                val boundary = "Boundary-${System.currentTimeMillis()}"

                connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=$boundary"
                )

                val outputStream = DataOutputStream(connection.outputStream)

                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes(
                    "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n"
                )
                outputStream.writeBytes("$preset\r\n")

                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"solicitud_${System.currentTimeMillis()}.jpg\"\r\n"
                )
                outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")

                contentResolver.openInputStream(imageUri)?.use { inputStream ->

                    val buffer = ByteArray(4096)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also {
                            bytesRead = it
                        } != -1
                    ) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                } ?: throw IllegalStateException(
                    "No se pudo leer la fotografía seleccionada"
                )

                outputStream.writeBytes("\r\n--$boundary--\r\n")
                outputStream.flush()
                outputStream.close()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {

                    val responseString =
                        connection.inputStream.bufferedReader().use {
                            it.readText()
                        }

                    val regex =
                        """"secure_url"\s*:\s*"(.*?)"""".toRegex()

                    val matchResult = regex.find(responseString)

                    val secureUrl = matchResult
                        ?.groups
                        ?.get(1)
                        ?.value
                        ?.replace("\\/", "/")

                    if (!secureUrl.isNullOrBlank()) {
                        return secureUrl
                    }
                }

            } catch (e: Exception) {
                // Intentar con el siguiente preset.
            } finally {
                connection?.disconnect()
            }
        }

        return null
    }
}
