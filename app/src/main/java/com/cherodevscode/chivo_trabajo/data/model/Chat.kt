package com.cherodevscode.chivo_trabajo.data.model

data class Chat(
    val id: String = "",
    val solicitudId: String = "",
    val remitenteUid: String = "",
    val receptorUid: String = "",
    val mensaje: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
