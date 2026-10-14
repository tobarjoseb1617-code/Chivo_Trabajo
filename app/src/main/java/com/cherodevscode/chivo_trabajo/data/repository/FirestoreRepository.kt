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


    // Actualiza una solicitud existente en Firebase Firestore.
    suspend fun actualizarSolicitud(
        solicitud: Solicitud,
        uidCliente: String
    ): Result<Unit> {

        return try {

            // Verifica que se haya recibido un ID válido.
            require(solicitud.idSolicitud.isNotBlank()) {
                "El ID de la solicitud no puede estar vacío"
            }

            // Obtiene la referencia del documento existente.
            val referencia = db.collection("Solicitudes")
                .document(solicitud.idSolicitud)

            // Ejecuta una transacción para comprobar los datos
            // antes de guardar las modificaciones.
            db.runTransaction { transaccion ->

                // Consulta la información actual de la solicitud.
                val documento = transaccion.get(referencia)

                // Comprueba que el documento exista.
                if (!documento.exists()) {
                    throw IllegalStateException(
                        "La solicitud no existe"
                    )
                }

                // Obtiene el propietario de la solicitud.
                val propietarioId = documento.getString("clientId")

                // Verifica que el usuario sea el propietario.
                if (propietarioId != uidCliente) {
                    throw IllegalStateException(
                        "No tienes permiso para editar esta solicitud"
                    )
                }

                // Obtiene el estado actual.
                val estadoActual = documento.getString("estado")

                // Obtiene el profesional asignado.
                val profesionalId = documento.getString("profesionalId")

                // Solo permite editar solicitudes pendientes.
                if (
                    estadoActual != "PENDIENTE" ||
                    !profesionalId.isNullOrBlank()
                ) {
                    throw IllegalStateException(
                        "Esta solicitud ya no se puede editar"
                    )
                }

                // Prepara únicamente los campos editables.
                val cambios = mapOf(
                    "categoryId" to solicitud.categoryId,
                    "descripcion" to solicitud.descripcion,
                    "fotoProblema" to solicitud.fotoProblema,
                    "fotosProblema" to solicitud.fotosProblema,
                    "direccion" to solicitud.direccion,
                    "ciudad" to solicitud.ciudad,
                    "latitud" to solicitud.latitud,
                    "longitud" to solicitud.longitud,
                    "fechaServicio" to solicitud.fechaServicio,
                    "horaServicio" to solicitud.horaServicio,
                    "presupuesto" to solicitud.presupuesto,
                    "fechaActualizacion" to
                            com.google.firebase.firestore.FieldValue.serverTimestamp()
                )

                // Actualiza el mismo documento de Firestore.
                transaccion.update(referencia, cambios)

                null
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {

            // Devuelve el error si ocurre algún problema.
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
