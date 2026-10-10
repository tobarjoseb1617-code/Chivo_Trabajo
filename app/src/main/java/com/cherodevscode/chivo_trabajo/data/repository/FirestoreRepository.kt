package com.cherodevscode.chivo_trabajo.data.repository

import com.cherodevscode.chivo_trabajo.data.model.Registro
import com.cherodevscode.chivo_trabajo.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.cherodevscode.chivo_trabajo.data.model.Profesional
import com.cherodevscode.chivo_trabajo.data.model.Portafolio
import com.cherodevscode.chivo_trabajo.data.model.Solicitud

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

    suspend fun actualizarVerificacionDuiCompleta(
        uid: String,
        dui: String,
        frenteUrl: String,
        dorsoUrl: String,
        estado: String
    ): Result<Unit> {
        return try {
            val datos = mapOf(
                "dui" to dui,
                "duiFrenteUrl" to frenteUrl,
                "duiDorsoUrl" to dorsoUrl,
                "estadoVerificacion" to estado
            )
            db.collection("Usuario").document(uid).update(datos).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun actualizarFotoPerfil(uid: String, url: String): Result<Unit> {
        return try {
            db.collection("Usuario").document(uid).update("fotoPerfil", url).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarProfesional(profesional: Profesional): Result<Unit> {
        return try {
            db.collection("profesionales")
                .document(profesional.uid)
                .set(profesional)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarTrabajoPortafolio(
        uidProfesional: String,
        url: String,
        titulo: String,
        descripcion: String
    ): Result<Unit> {
        return try {

            // Crear un documento nuevo con ID automático
            val documento = db.collection("profesionales")
                .document(uidProfesional)
                .collection("Portafolio")
                .document()

            val trabajo = Portafolio(
                idFoto = documento.id,
                url = url,
                titulo = titulo,
                descripcion = descripcion
            )

            documento.set(trabajo).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun obtenerPortafolio(
        uidProfesional: String
    ): Result<List<Portafolio>> {
        return try {

            val documentos = db.collection("profesionales")
                .document(uidProfesional)
                .collection("Portafolio")
                .get()
                .await()

            val trabajos = documentos.documents.mapNotNull { documento ->

                documento.toObject(Portafolio::class.java)?.copy(
                    idFoto = documento.id
                )
            }

            Result.success(trabajos)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarDescripcionPortafolio(
        uidProfesional: String,
        idFoto: String,
        nuevaDescripcion: String
    ): Result<Unit> {
        return try {

            db.collection("profesionales")
                .document(uidProfesional)
                .collection("Portafolio")
                .document(idFoto)
                .update("descripcion", nuevaDescripcion)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun eliminarTrabajoPortafolio(
        uidProfesional: String,
        idFoto: String
    ): Result<Unit> {
        return try {

            db.collection("profesionales")
                .document(uidProfesional)
                .collection("Portafolio")
                .document(idFoto)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Guardar nueva solicitud de servicio en la colección "Solicitudes"
    suspend fun guardarSolicitud(solicitud: Solicitud): Result<String> {
        return try {
            val docRef = db.collection("Solicitudes").document()
            val solicitudConId = solicitud.copy(idSolicitud = docRef.id)
            docRef.set(solicitudConId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener una solicitud específica por su ID desde la colección "Solicitudes"
    suspend fun obtenerSolicitudPorId(idSolicitud: String): Result<Solicitud?> {
        return try {
            val doc = db.collection("Solicitudes").document(idSolicitud).get().await()
            val solicitud = doc.toObject(Solicitud::class.java)?.copy(idSolicitud = doc.id)
            Result.success(solicitud)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener datos del perfil profesional (incluyendo descripción)
    suspend fun obtenerProfesional(uid: String): Result<Profesional?> {
        return try {
            val doc = db.collection("profesionales").document(uid).get().await()
            val profesional = doc.toObject(Profesional::class.java)
            Result.success(profesional)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar descripción profesional
    suspend fun actualizarDescripcionProfesional(uid: String, nuevaDescripcion: String): Result<Unit> {
        return try {
            db.collection("profesionales").document(uid).update("descripcion", nuevaDescripcion).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
