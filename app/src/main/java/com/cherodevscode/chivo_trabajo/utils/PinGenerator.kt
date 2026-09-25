package com.cherodevscode.chivo_trabajo.utils

import kotlin.random.Random

object PinGenerator {
    /**
     * Genera un PIN de seguridad aleatorio de 4 dígitos (estilo Uber).
     */
    fun generarPin(): String {
        val pinInt = Random.nextInt(1000, 10000)
        return pinInt.toString()
    }
}
