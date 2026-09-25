package com.cherodevscode.chivo_trabajo.data.repository

import com.cherodevscode.chivo_trabajo.data.model.Registro
import com.cherodevscode.chivo_trabajo.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun guardarPerfilUsuario(usuario: Usuario): Result<Unit> {
        return try {
            db.collection("Usuario").document(usuario.uid).set(usuario).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPerfilUsuario(uid: String): Result<Usuario?> {
        return try {
            val doc = db.collection("Usuario").document(uid).get().await()
            val usuario = doc.toObject(Usuario::class.java)
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarEstadoVerificacionDui(uid: String, estado: String): Result<Unit> {
        return try {
            db.collection("Usuario").document(uid).update("estadoVerificacion", estado).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
