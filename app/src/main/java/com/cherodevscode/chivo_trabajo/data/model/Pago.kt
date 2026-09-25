package com.cherodevscode.chivo_trabajo.data.model

data class Pago(
    val id: String = "",
    val solicitudId: String = "",
    val clienteUid: String = "",
    val profesionalUid: String = "",
    val monto: Double = 0.0,
    val metodoPago: String = "EFECTIVO",
    val estadoPago: String = "PENDIENTE",
    val fechaPago: Long = System.currentTimeMillis()
)
