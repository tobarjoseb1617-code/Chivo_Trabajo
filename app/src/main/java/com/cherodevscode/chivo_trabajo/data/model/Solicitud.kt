package com.cherodevscode.chivo_trabajo.data.model

data class Solicitud(
    val id: String = "",
    val clienteUid: String = "",
    val profesionalUid: String = "",
    val servicioId: String = "",
    val estado: String = "PENDIENTE", // PENDIENTE, ACEPTADA, RECHAZADA, EN_PROCESO, COMPLETADA
    val pinSeguridad: String = "", // PIN de 4 dígitos estilo Uber
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val detalles: String = ""
)
