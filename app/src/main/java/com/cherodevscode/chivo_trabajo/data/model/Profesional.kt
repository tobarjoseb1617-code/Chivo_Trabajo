package com.cherodevscode.chivo_trabajo.data.model

data class Profesional(
    val id: String = "",
    val usuarioUid: String = "",
    val profesion: String = "",
    val descripcion: String = "",
    val tarifaHora: Double = 0.0,
    val verificadoDui: Boolean = false,
    val calificacionPromedio: Double = 0.0,
    val totalTrabajos: Int = 0,
    val disponible: Boolean = true
)
