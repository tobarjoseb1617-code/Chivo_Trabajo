package com.cherodevscode.chivo_trabajo.data.dto

data class EstadisticasSolicitudesAdmin(
    val total: Int = 0,
    val pendientes: Int = 0,
    val asignadas: Int = 0,
    val enCurso: Int = 0,
    val completadas: Int = 0,
    val canceladas: Int = 0
)
