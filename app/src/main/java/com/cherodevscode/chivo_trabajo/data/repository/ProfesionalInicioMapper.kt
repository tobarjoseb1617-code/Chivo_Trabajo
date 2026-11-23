package com.cherodevscode.chivo_trabajo.data.repository

import com.cherodevscode.chivo_trabajo.data.model.Profesional
import com.cherodevscode.chivo_trabajo.data.model.Usuario

data class ProfesionalInicio(val usuario: Usuario, val profesional: Profesional)

/** Los datos opcionales o incompletos no deben impedir mostrar el documento. */
internal object ProfesionalInicioMapper {
    fun desdeDocumento(id: String, datos: Map<String, Any?>): ProfesionalInicio {
        val uid = datos.texto("uid").ifBlank { id }
        val profesional = Profesional(
            uid = uid,
            especialidad = datos.texto("especialidad"),
            especialidades = (datos["especialidades"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
            descripcion = datos.texto("descripcion"),
            disponible = datos["disponible"] as? Boolean ?: false,
            calificacionPromedio = (datos["calificacionPromedio"] as? Number)?.toDouble() ?: 0.0,
            cantidadCalificaciones = (datos["cantidadCalificaciones"] as? Number)?.toLong() ?: 0L
        )
        return ProfesionalInicio(perfil(uid, datos), profesional)
    }

    fun completarPerfil(actual: ProfesionalInicio, datos: Map<String, Any?>): ProfesionalInicio {
        val perfil = perfil(actual.profesional.uid, datos)
        return actual.copy(usuario = actual.usuario.copy(
            nombre = perfil.nombre.ifBlank { actual.usuario.nombre },
            apellido = perfil.apellido.ifBlank { actual.usuario.apellido },
            fotoPerfil = perfil.fotoPerfil.ifBlank { actual.usuario.fotoPerfil }
        ))
    }

    private fun perfil(uid: String, datos: Map<String, Any?>) = Usuario(
        uid = uid,
        nombre = datos.texto("nombre"),
        apellido = datos.texto("apellido"),
        fotoPerfil = datos.texto("fotoPerfil")
    )

    private fun Map<String, Any?>.texto(campo: String) = (this[campo] as? String).orEmpty().trim()
}
