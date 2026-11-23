package com.cherodevscode.chivo_trabajo.data.model

data class DetalleServicioCliente(
    val solicitud: Solicitud = Solicitud(),
    val propuesta: Propuesta? = null,
    val usuarioProfesional: Usuario? = null,
    val profesional: Profesional? = null
)
