package com.cherodevscode.chivo_trabajo.data.model

data class Profesional(
    val uid: String = "",
    val aniosExperiencia: Long = 0,
    val calificacionPromedio: Double = 0.0,
    val cantidadCalificaciones: Long = 0,
    val descripcion: String = "",
    val disponible: Boolean = false,
    val especialidad: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val serviciosOfrecidos: List<String> = emptyList(),
    val verificado: Boolean = false,
    val zonaTrabajo: List<String> = emptyList()

)
