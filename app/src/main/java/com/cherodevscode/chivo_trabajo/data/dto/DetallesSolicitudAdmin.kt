package com.cherodevscode.chivo_trabajo.data.dto

import com.cherodevscode.chivo_trabajo.data.model.Solicitud

data class DetallesSolicitudAdmin(
    val solicitud: Solicitud,
    val nombreCliente: String,
    val nombreProfesional: String
)