package com.cherodevscode.chivo_trabajo.data.repository

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.cherodevscode.chivo_trabajo.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

/**
 * Clase contenedora para la sesión local del usuario actual.
 */
data class LocalUser(
    val uid: String,
    val email: String?,
    val displayName: String? = null,
    val photoUrl: String? = null
)

/**
 * Resultado del flujo de autenticación con Google.
 */
sealed class ResultadoGoogleAuth {
    data class YaRegistrado(val uid: String, val email: String) : ResultadoGoogleAuth()
    data class RequiereRegistro(val email: String, val displayName: String?) : ResultadoGoogleAuth()
    object SolicitarCorreoManual : ResultadoGoogleAuth()
}

/**
 * Repositorio de Autenticación basado en la colección "credenciales" de Cloud Firestore,
 * encriptación de contraseñas con SHA-256 y almacenamiento local en SharedPreferences.
 */
class AuthRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("chivo_credenciales_prefs", Context.MODE_PRIVATE)
    private val credentialManager = CredentialManager.create(context)

    /**
     * Encripta la contraseña utilizando el algoritmo hash SHA-256.
     */
    fun encriptarContrasenaSHA256(contrasena: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(contrasena.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            contrasena
        }
    }

    // Obtener el usuario actual desde SharedPreferences locales
    fun usuarioActual(): LocalUser? {
        val uid = prefs.getString("USER_UID", null)
        val email = prefs.getString("USER_EMAIL", null)
        val name = prefs.getString("USER_NAME", "Usuario ChivoTrabajo")
        val photo = prefs.getString("USER_PHOTO", null)
        return if (uid != null) LocalUser(uid, email, name, photo) else null
    }

    // Registrar nuevo usuario guardando credenciales encriptadas en "credenciales"
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

            val hashContrasena = encriptarContrasenaSHA256(contrasena)
            val uid = UUID.randomUUID().toString()
            val credencialMap = mapOf(
                "uid" to uid,
                "correo" to correo,
                "contrasena" to hashContrasena
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

    // Iniciar sesión comparando contraseña encriptada en "credenciales"
    suspend fun iniciarSesionConCorreoYContrasena(correo: String, contrasena: String): Result<String> {
        return try {
            val hashContrasena = encriptarContrasenaSHA256(contrasena)

            // Consultar con el hash SHA-256
            var query = db.collection("credenciales")
                .whereEqualTo("correo", correo)
                .whereEqualTo("contrasena", hashContrasena)
                .get()
                .await()

            // Compatibilidad para registros antiguos en texto plano
            if (query.isEmpty) {
                query = db.collection("credenciales")
                    .whereEqualTo("correo", correo)
                    .whereEqualTo("contrasena", contrasena)
                    .get()
                    .await()

                if (!query.isEmpty) {
                    val docId = query.documents[0].id
                    db.collection("credenciales").document(docId)
                        .update("contrasena", hashContrasena)
                }
            }

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

    // Obtener cuentas de Google registradas en el dispositivo Android
    fun obtenerCuentasGoogleDispositivo(): List<String> {
        return try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            accounts.map { it.name }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Verificar un correo de Google arbitrario (para fallback o ingreso manual de Google)
    suspend fun verificarOIngresarCorreoGoogle(emailGoogle: String, displayName: String? = null): Result<ResultadoGoogleAuth> {
        return try {
            val query = db.collection("credenciales")
                .whereEqualTo("correo", emailGoogle)
                .get()
                .await()

            if (!query.isEmpty) {
                val doc = query.documents[0]
                val uid = doc.getString("uid") ?: doc.id

                prefs.edit()
                    .putString("USER_UID", uid)
                    .putString("USER_EMAIL", emailGoogle)
                    .putString("USER_NAME", displayName ?: emailGoogle.substringBefore("@"))
                    .apply()

                Result.success(ResultadoGoogleAuth.YaRegistrado(uid, emailGoogle))
            } else {
                Result.success(ResultadoGoogleAuth.RequiereRegistro(emailGoogle, displayName))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Selector de cuentas Google con CredentialManager y fallback para selección explícita
    suspend fun procesarGoogleAuth(activity: Activity): Result<ResultadoGoogleAuth> {
        val webClientId = activity.getString(R.string.default_web_client_id)

        // 1. Intentar con GetSignInWithGoogleOption
        try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val emailGoogle = googleCredential.id
                val nameGoogle = googleCredential.displayName ?: emailGoogle.substringBefore("@")
                return verificarOIngresarCorreoGoogle(emailGoogle, nameGoogle)
            }
        } catch (e1: Exception) {
            // 2. Intentar fallback con GetGoogleIdOption
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val emailGoogle = googleCredential.id
                    val nameGoogle = googleCredential.displayName ?: emailGoogle.substringBefore("@")
                    return verificarOIngresarCorreoGoogle(emailGoogle, nameGoogle)
                }
            } catch (e2: Exception) {
                // Si no hay token cacheado, solicitar al usuario seleccionar/confirmar explícitamente su correo Google
                return Result.success(ResultadoGoogleAuth.SolicitarCorreoManual)
            }
        }

        return Result.success(ResultadoGoogleAuth.SolicitarCorreoManual)
    }

    // Cerrar sesión limpiando credenciales locales
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
