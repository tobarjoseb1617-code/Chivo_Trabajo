package com.cherodevscode.chivo_trabajo.data.repository

import com.cherodevscode.chivo_trabajo.data.model.Conversacion
import com.cherodevscode.chivo_trabajo.data.model.Mensaje
import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class ChatRepository(context: Context) {
    private val authRepository = AuthRepository(context.applicationContext)
    fun usuarioActualUid(): String? = authRepository.usuarioActual()?.uid
    private val db = FirebaseFirestore.getInstance()
    private val chats = db.collection("conversaciones")
    private fun uid() = requireNotNull(usuarioActualUid()) { "Inicia sesión para conversar" }

    suspend fun abrirConversacion(receptor: String): String {
        val emisor = uid()
        require(receptor.isNotBlank() && receptor != emisor) { "Selecciona un contacto válido" }
        val participantes = listOf(emisor, receptor).sorted()
        val id = MessageDigest.getInstance("SHA-256")
            .digest(participantes.joinToString("|").toByteArray()).joinToString("") { "%02x".format(it) }
        val perfiles = participantes.associateWith { persona ->
            val perfil = db.collection("Usuario").document(persona).get().await()
            require(perfil.exists()) { "No se encontró el contacto" }
            listOfNotNull(perfil.getString("nombre"), perfil.getString("apellido")).joinToString(" ").ifBlank { "Usuario" }
        }
        val ref = chats.document(id)
        db.runTransaction { tx ->
            if (!tx.get(ref).exists()) tx.set(ref, mapOf(
                "participantes" to participantes, "nombres" to perfiles,
                "ultimoMensaje" to "", "actualizadoEn" to FieldValue.serverTimestamp()
            ))
        }.await()
        return id
    }

    suspend fun obtenerConversacion(id: String): Conversacion {
        val chat = chats.document(id).get().await().toObject(Conversacion::class.java)
        require(chat != null && uid() in chat.participantes) { "No tienes acceso a esta conversación" }
        return chat.copy(id = id)
    }

    fun escucharConversaciones(resultado: (List<Conversacion>?, Exception?) -> Unit): ListenerRegistration =
        chats.whereArrayContains("participantes", uid()).addSnapshotListener { datos, error ->
            resultado(datos?.documents?.mapNotNull { it.toObject(Conversacion::class.java)?.copy(id = it.id) }
                ?.sortedByDescending { it.actualizadoEn?.seconds ?: 0 }, error)
        }

    fun escucharMensajes(id: String, resultado: (List<Mensaje>?, Exception?) -> Unit): ListenerRegistration =
        chats.document(id).collection("mensajes").orderBy("creadoEn", Query.Direction.ASCENDING)
            .addSnapshotListener { datos, error ->
                resultado(datos?.documents?.mapNotNull { it.toObject(Mensaje::class.java)?.copy(id = it.id) }, error)
            }

    suspend fun enviar(id: String, tipo: String, texto: String = "", url: String = "", mime: String = "") {
        require(tipo in listOf("texto", "imagen", "video", "audio"))
        require(if (tipo == "texto") texto.isNotBlank() && texto.length <= 4000 else url.startsWith("https://res.cloudinary.com/"))
        val ref = chats.document(id)
        val mensaje = ref.collection("mensajes").document()
        db.runBatch { lote ->
            lote.set(mensaje, mapOf("remitenteUid" to uid(), "tipo" to tipo, "texto" to texto,
                "archivoUrl" to url, "mimeType" to mime, "creadoEn" to FieldValue.serverTimestamp()))
            lote.update(ref, mapOf("ultimoMensaje" to when(tipo) {
                "imagen" -> "📷 Foto"; "video" -> "🎬 Video"; "audio" -> "🎤 Audio"; else -> texto
            }, "actualizadoEn" to FieldValue.serverTimestamp()))
        }.await()
    }
}
