package com.cherodevscode.chivo_trabajo.data.model

import com.google.firebase.Timestamp

data class Portafolio(
    val idFoto: String = "",
    val url: String = "",
    val descripcion: String = "",
    val fecha: Timestamp = Timestamp.now()
)