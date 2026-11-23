package com.cherodevscode.chivo_trabajo.data.repository

import org.junit.Assert.*
import org.junit.Test

class ProfesionalInicioMapperTest {
    @Test fun conservaLosDosDocumentosAunqueNoTenganPerfilNiUbicacion() {
        val documentos = mapOf(
            "profesional-1" to mapOf<String, Any?>("disponible" to false, "activo" to false),
            "profesional-2" to mapOf<String, Any?>("ciudad" to "Otra ciudad", "especialidad" to "Electricidad")
        )
        val resultado = documentos.map { (id, datos) -> ProfesionalInicioMapper.desdeDocumento(id, datos) }
        assertEquals(listOf("profesional-1", "profesional-2"), resultado.map { it.usuario.uid })
        assertFalse(resultado.first().profesional.disponible)
        assertEquals("Electricidad", resultado.last().profesional.especialidad)
    }

    @Test fun admiteCamposOpcionalesYTiposNumericosDeFirestore() {
        val resultado = ProfesionalInicioMapper.desdeDocumento("documento", mapOf(
            "uid" to "uid-real", "nombre" to "Ana", "especialidades" to listOf("Pintura", 123),
            "calificacionPromedio" to 5L, "cantidadCalificaciones" to 2.0,
            "aniosExperiencia" to "dato antiguo"
        ))
        assertEquals("uid-real", resultado.usuario.uid)
        assertEquals("Ana", resultado.usuario.nombre)
        assertEquals(listOf("Pintura"), resultado.profesional.especialidades)
        assertEquals(5.0, resultado.profesional.calificacionPromedio, 0.0)
        assertEquals(2L, resultado.profesional.cantidadCalificaciones)
    }

    @Test fun unPerfilIncompletoNoBorraLosDatosDelProfesional() {
        val profesional = ProfesionalInicioMapper.desdeDocumento("uid", mapOf("nombre" to "Ana", "fotoPerfil" to "foto"))
        val resultado = ProfesionalInicioMapper.completarPerfil(profesional, mapOf("apellido" to "López"))
        assertEquals("Ana", resultado.usuario.nombre)
        assertEquals("López", resultado.usuario.apellido)
        assertEquals("foto", resultado.usuario.fotoPerfil)
    }
}
