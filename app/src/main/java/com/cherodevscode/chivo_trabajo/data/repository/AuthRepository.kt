package com.cherodevscode.chivo_trabajo.data.repository

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.cherodevscode.chivo_trabajo.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Clase contenedora para simular la sesión local del usuario actual con los campos esperados por la UI.
 */
data class LocalUser(
    val uid: String,
    val email: String?,
    val displayName: String? = null,
    val photoUrl: String? = null
)

/**
 * Repositorio de Autenticación basado en la colección "credenciales" de Cloud Firestore
 * y almacenamiento local en SharedPreferences (Sin utilizar Firebase Auth).
 */
class AuthRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("chivo_credenciales_prefs", Context.MODE_PRIVATE)
    private val credentialManager = CredentialManager.create(context)

    // Obtener el usuario actual desde SharedPreferences locales
    fun usuarioActual(): LocalUser? {
        val uid = prefs.getString("USER_UID", null)
        val email = prefs.getString("USER_EMAIL", null)
        val name = prefs.getString("USER_NAME", "Usuario ChivoTrabajo")
        val photo = prefs.getString("USER_PHOTO", null)
        return if (uid != null) LocalUser(uid, email, name, photo) else null
    }

    // Registrar nuevo usuario guardando credenciales en la colección "credenciales"
    suspend fun registrarConCorreoYContrasena(correo: String, contrasena: String): Result<String> {
        return try {
            // Verificar si el correo ya existe en "credenciales"
            val query = db.collection("credenciales")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!query.isEmpty) {
                return Result.failure(Exception("El correo ya se encuentra registrado"))
            }

            val uid = UUID.randomUUID().toString()
            val credencialMap = mapOf(
                "uid" to uid,
                "correo" to correo,
                "contrasena" to contrasena
            )

            // Guardar en Firestore colección "credenciales"
            db.collection("credenciales").document(uid).set(credencialMap).await()

            // Guardar sesión local
            prefs.edit()
                .putString("USER_UID", uid)
                .putString("USER_EMAIL", correo)
                .putString("USER_NAME", correo.substringBefore("@"))
                .apply()

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Iniciar sesión validando contra la colección "credenciales" de Firestore
    suspend fun iniciarSesionConCorreoYContrasena(correo: String, contrasena: String): Result<String> {
        return try {
            val query = db.collection("credenciales")
                .whereEqualTo("correo", correo)
                .whereEqualTo("contrasena", contrasena)
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("Correo o contraseña incorrectos"))
            }

            val documento = query.documents[0]
            val uid = documento.getString("uid") ?: documento.id

            // Guardar sesión local
            prefs.edit()
                .putString("USER_UID", uid)
                .putString("USER_EMAIL", correo)
                .putString("USER_NAME", correo.substringBefore("@"))
                .apply()

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Simular envío de correo de recuperación
    suspend fun enviarCorreoRecuperacionPassword(correo: String): Result<Unit> {
        return try {
            val query = db.collection("credenciales")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("El correo no está registrado"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Iniciar sesión con Google (Valida o registra el email en "credenciales")
    suspend fun iniciarSesionConGoogle(activity: Activity): Result<String> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val emailGoogle = googleCredential.id
                val nameGoogle = googleCredential.displayName ?: emailGoogle.substringBefore("@")
                val photoGoogle = googleCredential.profilePictureUri?.toString()

                // Buscar si el correo de Google ya existe en "credenciales"
                val query = db.collection("credenciales")
                    .whereEqualTo("correo", emailGoogle)
                    .get()
                    .await()

                val uid = if (!query.isEmpty) {
                    val doc = query.documents[0]
                    doc.getString("uid") ?: doc.id
                } else {
                    // Si no existe, crear credencial automática para Google
                    val newUid = UUID.randomUUID().toString()
                    val credencialMap = mapOf(
                        "uid" to newUid,
                        "correo" to emailGoogle,
                        "contrasena" to "GOOGLE_AUTH_SESSION"
                    )
                    db.collection("credenciales").document(newUid).set(credencialMap).await()
                    newUid
                }

                // Guardar sesión local
                prefs.edit()
                    .putString("USER_UID", uid)
                    .putString("USER_EMAIL", emailGoogle)
                    .putString("USER_NAME", nameGoogle)
                    .putString("USER_PHOTO", photoGoogle)
                    .apply()

                Result.success(uid)
            } else {
                Result.failure(Exception("No se pudo autenticar con Google"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión limpiando credenciales locales
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
