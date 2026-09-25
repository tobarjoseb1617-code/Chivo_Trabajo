package com.cherodevscode.chivo_trabajo.data.model

data class Servicio(
    val id: String = "",
    val profesionalUid: String = "",
    val categoriaId: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val activo: Boolean = true
)
