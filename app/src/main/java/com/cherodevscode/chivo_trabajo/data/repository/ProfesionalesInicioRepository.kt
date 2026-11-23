package com.cherodevscode.chivo_trabajo.data.repository

import android.location.Location
import com.cherodevscode.chivo_trabajo.data.model.Profesional
import com.cherodevscode.chivo_trabajo.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ProfesionalInicio(val usuario: Usuario, val profesional: Profesional, val distanciaKm: Float?)
class ProfesionalesInicioRepository {
    suspend fun obtener(): List<ProfesionalInicio> {
        val db = FirebaseFirestore.getInstance()
        val uid = requireNotNull(FirebaseAuth.getInstance().currentUser?.uid)
        val cliente = db.collection("Usuario").document(uid).get().await().toObject(Usuario::class.java)
        val profesionales = db.collection("profesionales").whereEqualTo("disponible", true).get().await()
        return profesionales.documents.mapNotNull { doc ->
            val pro = doc.toObject(Profesional::class.java) ?: return@mapNotNull null
            if (doc.id == uid) return@mapNotNull null
            val usuario = db.collection("Usuario").document(doc.id).get().await().toObject(Usuario::class.java)
                ?.copy(uid = doc.id) ?: return@mapNotNull null
            if (!usuario.activo) return@mapNotNull null
            val distancia = if (cliente != null && (cliente.latitud != 0.0 || cliente.longitud != 0.0) && (pro.latitud != 0.0 || pro.longitud != 0.0)) {
                val resultado = FloatArray(1)
                Location.distanceBetween(cliente.latitud, cliente.longitud, pro.latitud, pro.longitud, resultado)
                resultado[0] / 1000
            } else null
            ProfesionalInicio(usuario, pro.copy(uid = doc.id), distancia)
        }.sortedBy { it.distanciaKm ?: Float.MAX_VALUE }.take(10)
    }
}
