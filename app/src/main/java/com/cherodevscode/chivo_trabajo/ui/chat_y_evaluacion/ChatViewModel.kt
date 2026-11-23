package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.model.Mensaje
import com.cherodevscode.chivo_trabajo.data.repository.ChatRepository
import com.cherodevscode.chivo_trabajo.data.repository.CloudinaryRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ChatRepository(app)
    private val archivos = CloudinaryRepository(app)
    val mensajes = MutableLiveData<List<Mensaje>>(emptyList())
    val nombre = MutableLiveData("Conversación")
    val error = MutableLiveData<String?>()
    val listo = MutableLiveData(false)
    val enviando = MutableLiveData(false)
    val textoEnviado = MutableLiveData<String?>()
    val adjuntoPendiente = MutableLiveData(false)
    private var pendiente: Pair<Uri, String>? = null
    private var urlPendiente: String? = null
    private var chatId: String? = null
    private var listener: ListenerRegistration? = null
    private var iniciado = false

    fun abrir(id: String?, contacto: String?) {
        if (iniciado) return
        iniciado = true
        viewModelScope.launch {
            try {
                val destino = id?.takeIf { it.isNotBlank() } ?: repository.abrirConversacion(contacto.orEmpty())
                val chat = repository.obtenerConversacion(destino)
                chatId = destino
                nombre.value = chat.nombres[chat.participantes.first { it != repository.usuarioActualUid() }] ?: "Contacto"
                listener = repository.escucharMensajes(destino) { datos, fallo ->
                    if (fallo != null) error.value = "No se pudieron cargar los mensajes: ${fallo.localizedMessage}"
                    else mensajes.value = datos.orEmpty()
                }
                listo.value = true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                iniciado = false
                android.util.Log.e("Chat", "No se pudo abrir la conversación", e)
                error.value = mensajeError(e, "No se pudo abrir el chat")
            }
        }
    }

    fun enviar(texto: String = "", uri: Uri? = null, mime: String = "") {
        val id = chatId ?: return
        if (enviando.value == true) return
        if (uri != null) {
            if (pendiente?.first != uri) {
                pendiente?.first?.let { limpiarTemporal(it) }
                urlPendiente = null
            }
            pendiente = uri to mime
            adjuntoPendiente.value = false
        }
        enviando.value = true
        viewModelScope.launch {
            try {
                if (uri == null) {
                    repository.enviar(id, "texto", texto.trim())
                    textoEnviado.value = texto
                } else {
                    val url = urlPendiente ?: archivos.subir(uri, mime).also { urlPendiente = it }
                    val tipo = when { mime.startsWith("image/") -> "imagen"; mime.startsWith("video/") -> "video"; else -> "audio" }
                    repository.enviar(id, tipo, url = url, mime = mime)
                    limpiarTemporal(uri)
                    pendiente = null
                    urlPendiente = null
                    adjuntoPendiente.value = false
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e("Chat", "No se pudo enviar el mensaje", e)
                error.value = mensajeError(e, "No se pudo enviar. Inténtalo de nuevo")
            } finally {
                adjuntoPendiente.value = pendiente != null
                enviando.value = false
            }
        }
    }

    private fun mensajeError(e: Exception, defecto: String): String =
        when ((e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code) {
            com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Firebase rechazó el acceso al chat. Revisa las reglas de conversaciones."
            com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE ->
                "No se pudo conectar con Firebase. Revisa la conexión e inténtalo de nuevo."
            else -> e.localizedMessage ?: defecto
        }

    fun reintentarAdjunto() { pendiente?.let { enviar(uri = it.first, mime = it.second) } }

    private fun limpiarTemporal(uri: Uri) {
        if (uri.scheme != "file") return
        val archivo = java.io.File(uri.path.orEmpty())
        if (archivo.parentFile == getApplication<Application>().cacheDir && archivo.name.startsWith("nota_")) archivo.delete()
    }

    override fun onCleared() { listener?.remove(); pendiente?.first?.let { limpiarTemporal(it) } }
}
