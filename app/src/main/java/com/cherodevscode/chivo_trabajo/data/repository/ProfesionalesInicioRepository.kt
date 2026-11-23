package com.cherodevscode.chivo_trabajo.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class ProfesionalesInicioRepository {
    private val db = FirebaseFirestore.getInstance()

    fun observar(): Flow<List<ProfesionalInicio>> = callbackFlow<QuerySnapshot> {
        val listener = db.collection("profesionales").addSnapshotListener { documentos, error ->
            if (error != null) close(error)
            else if (documentos != null) trySend(documentos)
        }
        awaitClose { listener.remove() }
    }.transform { documentos ->
        // Mostrar todos los documentos, sin depender de ubicación, estado ni de otra colección.
        val profesionales = documentos.documents.map { doc ->
            ProfesionalInicioMapper.desdeDocumento(doc.id, doc.data.orEmpty())
        }
        emit(profesionales)

        // Nombre y foto de Usuario son un complemento: si no se pueden leer, la tarjeta permanece.
        val completos = coroutineScope {
            profesionales.map { profesional ->
                async {
                    try {
                        val datos = withTimeoutOrNull(3000) {
                            db.collection("Usuario").document(profesional.usuario.uid).get().await().data
                        }
                        if (datos != null) ProfesionalInicioMapper.completarPerfil(profesional, datos) else profesional
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Log.w("ProfesionalesInicio", "No se pudo completar el perfil del profesional", e)
                        profesional
                    }
                }
            }.awaitAll()
        }
        if (completos != profesionales) emit(completos)
    }
}
