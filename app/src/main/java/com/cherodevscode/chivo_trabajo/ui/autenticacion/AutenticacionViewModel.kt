package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.model.Usuario
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class AutenticacionViewModel(context: Context) : ViewModel() {
    private val authRepository = AuthRepository(context)
    private val firestoreRepository = FirestoreRepository()

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _tipoUsuarioDestino = MutableLiveData<String?>()
    val tipoUsuarioDestino: LiveData<String?> = _tipoUsuarioDestino

    fun iniciarSesion(correo: String, contrasena: String) {
        viewModelScope.launch {
            val res = authRepository.iniciarSesionConCorreoYContrasena(correo, contrasena)
            res.onSuccess { uid ->
                _mensaje.value = "¡Inicio de sesión exitoso!"
                verificarRolYEstado(uid)
            }.onFailure { e ->
                _mensaje.value = "Error: ${e.message}"
            }
        }
    }

    fun registrarse(nombre: String, apellido: String, correo: String, contrasena: String, telefono: String, tipoUsuario: String) {
        viewModelScope.launch {
            val res = authRepository.registrarConCorreoYContrasena(correo, contrasena)
            res.onSuccess { uid ->
                val nuevoUsuario = Usuario(
                    uid = uid,
                    nombre = nombre,
                    apellido = apellido,
                    correo = correo,
                    telefono = telefono,
                    tipoUsuario = tipoUsuario,
                    estadoVerificacion = "PENDIENTE"
                )
                val saveRes = firestoreRepository.guardarPerfilUsuario(nuevoUsuario)
                saveRes.onSuccess {
                    _mensaje.value = "¡Registro exitoso!"
                    _tipoUsuarioDestino.value = tipoUsuario
                }.onFailure { e ->
                    _mensaje.value = "Error al guardar perfil: ${e.message}"
                }
            }.onFailure { e ->
                _mensaje.value = "Error al registrar: ${e.message}"
            }
        }
    }

    fun iniciarSesionGoogle(activity: Activity) {
        viewModelScope.launch {
            val res = authRepository.iniciarSesionConGoogle(activity)
            res.onSuccess { uid ->
                _mensaje.value = "¡Google Login exitoso!"
                verificarRolYEstado(uid)
            }.onFailure { e ->
                _mensaje.value = "Error Google: ${e.message}"
            }
        }
    }

    private fun verificarRolYEstado(uid: String) {
        viewModelScope.launch {
            val res = firestoreRepository.obtenerPerfilUsuario(uid)
            res.onSuccess { usuario ->
                if (usuario != null) {
                    _tipoUsuarioDestino.value = usuario.tipoUsuario
                } else {
                    _tipoUsuarioDestino.value = "CLIENTE"
                }
            }.onFailure {
                _tipoUsuarioDestino.value = "CLIENTE"
            }
        }
    }

    fun recuperarPassword(correo: String) {
        viewModelScope.launch {
            val res = authRepository.enviarCorreoRecuperacionPassword(correo)
            res.onSuccess {
                _mensaje.value = "Correo de recuperación enviado."
            }.onFailure { e ->
                _mensaje.value = "Error: ${e.message}"
            }
        }
    }
}
