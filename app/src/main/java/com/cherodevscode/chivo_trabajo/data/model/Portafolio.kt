package com.cherodevscode.chivo_trabajo.data.model

data class Portafolio(
    val id: String = "",
    val profesionalUid: String = "",
    val tituloTrabajo: String = "",
    val descripcion: String = "",
    val imagenUrl: String = "",
    val fechaSubida: Long = System.currentTimeMillis()
)
