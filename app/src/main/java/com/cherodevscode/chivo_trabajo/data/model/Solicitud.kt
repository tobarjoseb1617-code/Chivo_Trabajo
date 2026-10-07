package com.cherodevscode.chivo_trabajo.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de datos para la colección "solicitudes" en Cloud Firestore.
 */
data class Solicitud(
    val idSolicitud: String = "",
    val clientId: String = "",
    val categoryId: String = "",
    val descripcion: String = "",
    val fotoProblema: String = "",
    val direccion: String = "",
    val ciudad: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fechaServicio: String = "",
    val horaServicio: String = "",
    val presupuesto: String = "",
    val estado: String = "PENDIENTE",
    val profesionalId: String? = null,
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaActualizacion: Timestamp = Timestamp.now()
)
