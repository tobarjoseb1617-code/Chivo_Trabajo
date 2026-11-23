package com.cherodevscode.chivo_trabajo.data.model

import com.google.firebase.Timestamp

data class Conversacion(
    val id: String = "",
    val participantes: List<String> = emptyList(),
    val nombres: Map<String, String> = emptyMap(),
    val ultimoMensaje: String = "",
    val actualizadoEn: Timestamp? = null
)

data class Mensaje(
    val id: String = "",
    val remitenteUid: String = "",
    val tipo: String = "texto",
    val texto: String = "",
    val archivoUrl: String = "",
    val mimeType: String = "",
    val creadoEn: Timestamp? = null
)
