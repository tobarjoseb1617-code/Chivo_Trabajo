package com.cherodevscode.chivo_trabajo.data.model

import com.google.firebase.Timestamp

data class Propuesta(
    val idPropuesta: String = "",
    val solicitudId: String = "",
    val profesionalId: String = "",
    val clienteId: String = "",
    val tipoPropuesta: String = "",
    val precioPropuesto: Double = 0.0,
    val mensaje: String = "",
    val disponibilidad: String = "",
    val estado: String = "PENDIENTE",
    val fechaCreacion: Timestamp? = null
)
