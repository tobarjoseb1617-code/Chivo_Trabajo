package com.cherodevscode.chivo_trabajo.data.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val tipoUsuario: String = "CLIENTE", // CLIENTE o PROFESIONAL
    val estadoVerificacion: String = "PENDIENTE", // PENDIENTE, VERIFICADO
    val fotoPerfil: String = "",
    val direccion: String = "",
    val ciudad: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val activo: Boolean = true
)
