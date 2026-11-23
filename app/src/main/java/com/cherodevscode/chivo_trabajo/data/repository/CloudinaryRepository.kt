package com.cherodevscode.chivo_trabajo.data.repository

import android.content.Context
import android.net.Uri
import com.cherodevscode.chivo_trabajo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class CloudinaryRepository(private val context: Context) {
    suspend fun subir(uri: Uri, mime: String): String = withContext(Dispatchers.IO) {
        require(mime.startsWith("image/") || mime.startsWith("video/") || mime.startsWith("audio/")) { "Archivo no compatible" }
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            require(descriptor.length <= 50L * 1024 * 1024) { "El archivo supera los 50 MB" }
        }
        val cloud = BuildConfig.CLOUDINARY_CLOUD_NAME
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        require(cloud.isNotBlank() && preset.isNotBlank()) { "Configura Cloudinary en local.properties" }
        val recurso = if (mime.startsWith("image/")) "image" else "video"
        val conexion = URL("https://api.cloudinary.com/v1_1/$cloud/$recurso/upload").openConnection() as HttpURLConnection
        try {
            val boundary = UUID.randomUUID().toString()
            conexion.requestMethod = "POST"
            conexion.doOutput = true
            conexion.connectTimeout = 30000
            conexion.readTimeout = 120000
            conexion.setChunkedStreamingMode(65536)
            conexion.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            DataOutputStream(conexion.outputStream).use { salida ->
                salida.writeBytes("--$boundary\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n$preset\r\n")
                val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "bin"
                salida.writeBytes("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"chat_${UUID.randomUUID()}.$extension\"\r\nContent-Type: $mime\r\n\r\n")
                requireNotNull(context.contentResolver.openInputStream(uri)) { "No se pudo leer el archivo" }.use { entrada ->
                    val buffer = ByteArray(65536)
                    var total = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val n = entrada.read(buffer)
                        if (n == -1) break
                        total += n
                        require(total <= 50L * 1024 * 1024) { "El archivo supera los 50 MB" }
                        salida.write(buffer, 0, n)
                    }
                }
                salida.writeBytes("\r\n--$boundary--\r\n")
            }
            check(conexion.responseCode in 200..299) { "No se pudo subir el archivo. Revisa el preset de Cloudinary (${conexion.responseCode})" }
            val url = JSONObject(conexion.inputStream.bufferedReader().use { it.readText() }).getString("secure_url")
            check(url.startsWith("https://res.cloudinary.com/")) { "Respuesta de Cloudinary no válida" }
            url
        } finally { conexion.disconnect() }
    }
}
