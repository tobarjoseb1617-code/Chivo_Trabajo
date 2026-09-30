package com.cherodevscode.chivo_trabajo.data.model

data class Profesional(
    val uid: String = "",
    val aniosExperiencia: Long = 0,
    val calificacionPromedio: Double = 0.0,
    val cantidadCalificaciones: Long = 0,
    val descripcion: String = "",
    val disponible: Boolean = false,
    val especialidad: String = "", // Especialidad principal (para compatibilidad)
    val especialidades: List<String> = emptyList(), // Múltiples especialidades seleccionadas
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val serviciosOfrecidos: List<String> = emptyList(), // Sub-servicios específicos agrupados
    val verificado: Boolean = false,
    val zonaTrabajo: List<String> = emptyList()
)
