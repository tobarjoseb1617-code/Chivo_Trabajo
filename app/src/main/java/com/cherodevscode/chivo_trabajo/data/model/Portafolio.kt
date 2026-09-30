package com.cherodevscode.chivo_trabajo.data.model

import com.google.firebase.Timestamp

data class Portafolio(
    val idFoto: String = "",
    val url: String = "",
    val titulo: String = "", // Título del trabajo o acreditación
    val descripcion: String = "",
    val fecha: Timestamp = Timestamp.now()
)
