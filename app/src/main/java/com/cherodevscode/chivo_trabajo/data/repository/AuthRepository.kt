package com.cherodevscode.chivo_trabajo.data.repository

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.cherodevscode.chivo_trabajo.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    fun usuarioActual() = auth.currentUser

    suspend fun registrarConCorreoYContrasena(correo: String, contrasena: String): Result<String> {
        return try {
            val res = auth.createUserWithEmailAndPassword(correo, contrasena).await()
            val uid = res.user?.uid
            if (uid != null) Result.success(uid) else Result.failure(Exception("Error al registrar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesionConCorreoYContrasena(correo: String, contrasena: String): Result<String> {
        return try {
            val res = auth.signInWithEmailAndPassword(correo, contrasena).await()
            val uid = res.user?.uid
            if (uid != null) Result.success(uid) else Result.failure(Exception("Error al iniciar sesión"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enviarCorreoRecuperacionPassword(correo: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                Result.success(auth.currentUser?.uid.orEmpty())
            } else {
                Result.failure(Exception("Credencial no reconocida"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
