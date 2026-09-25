package com.cherodevscode.chivo_trabajo.data.model

data class Calificacion(
    val id: String = "",
    val solicitudId: String = "",
    val autorUid: String = "",
    val receptorUid: String = "",
    val puntuacion: Int = 5,
    val comentario: String = "",
    val fecha: Long = System.currentTimeMillis()
)
