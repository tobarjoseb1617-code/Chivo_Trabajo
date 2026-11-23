package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.app.Activity
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.model.Usuario
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.data.repository.ResultadoGoogleAuth
import kotlinx.coroutines.launch

class AutenticacionViewModel(context: Context) : ViewModel() {
    private val authRepository = AuthRepository(context)
    private val firestoreRepository = FirestoreRepository()

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _tipoUsuarioDestino = MutableLiveData<String?>()
    val tipoUsuarioDestino: LiveData<String?> = _tipoUsuarioDestino

    private val _googleRequiereRegistro = MutableLiveData<Pair<String, String?>?>()
    val googleRequiereRegistro: LiveData<Pair<String, String?>?> = _googleRequiereRegistro

    private val _solicitarCorreoGoogleManual = MutableLiveData<Boolean>()
    val solicitarCorreoGoogleManual: LiveData<Boolean> = _solicitarCorreoGoogleManual

    /**
     * Valida si un correo electrónico tiene formato correcto o es la cuenta admin autorizada.
     */
    fun esCorreoValido(correo: String): Boolean {
        if (correo.equals("admin@gamil.com", ignoreCase = true) ||
            correo.equals("admin@gmail.com", ignoreCase = true)
        ) {
            return true
        }
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }

    /**
     * Valida que la contraseña tenga mínimo 8 caracteres, al menos una mayúscula y un carácter especial.
     */
    fun validarFormatoContrasena(contrasena: String): String? {
        if (contrasena.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres"
        }
        if (!contrasena.contains(Regex("[A-Z]"))) {
            return "La contraseña debe incluir al menos una letra mayúscula (ej. A-Z)"
        }
        if (!contrasena.contains(Regex("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]"))) {
            return "La contraseña debe incluir al menos un carácter especial (ej. !@#$%^&*)"
        }
        return null
    }

    /**
     * Verifica si la sesión local del usuario ya se encuentra activa para auto-login.
     */
    fun verificarSesionExistente() {
        val usuarioLocal = authRepository.usuarioActual()
        if (usuarioLocal != null) {
            verificarRolYEstado(usuarioLocal.uid)
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        if (!esCorreoValido(correo)) {
            _mensaje.value = "Ingrese un correo electrónico válido"
            return
        }

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
        if (!esCorreoValido(correo)) {
            _mensaje.value = "Ingrese un correo electrónico válido"
            return
        }

        val errorContrasena = validarFormatoContrasena(contrasena)
        if (errorContrasena != null) {
            _mensaje.value = errorContrasena
            return
        }

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
            val res = authRepository.procesarGoogleAuth(activity)
            res.onSuccess { resultado ->
                when (resultado) {
                    is ResultadoGoogleAuth.YaRegistrado -> {
                        _mensaje.value = "¡Inicio de sesión con Google exitoso!"
                        verificarRolYEstado(resultado.uid)
                    }
                    is ResultadoGoogleAuth.RequiereRegistro -> {
                        _mensaje.value = "Correo de Google no registrado. Por favor completa tu perfil."
                        _googleRequiereRegistro.value = Pair(resultado.email, resultado.displayName)
                    }
                    is ResultadoGoogleAuth.SolicitarCorreoManual -> {
                        _solicitarCorreoGoogleManual.value = true
                    }
                }
            }.onFailure { e ->
                _mensaje.value = "Error Google: ${e.message}"
            }
        }
    }

    fun procesarCorreoGoogleManual(emailGoogle: String) {
        if (!esCorreoValido(emailGoogle)) {
            _mensaje.value = "Ingrese un correo electrónico válido"
            return
        }

        viewModelScope.launch {
            val res = authRepository.verificarOIngresarCorreoGoogle(emailGoogle)
            res.onSuccess { resultado ->
                when (resultado) {
                    is ResultadoGoogleAuth.YaRegistrado -> {
                        _mensaje.value = "¡Inicio de sesión con Google exitoso!"
                        verificarRolYEstado(resultado.uid)
                    }
                    is ResultadoGoogleAuth.RequiereRegistro -> {
                        _mensaje.value = "Correo de Google no registrado. Por favor completa tu perfil."
                        _googleRequiereRegistro.value = Pair(resultado.email, resultado.displayName)
                    }
                    else -> {}
                }
            }.onFailure { e ->
                _mensaje.value = "Error: ${e.message}"
            }
        }
    }

    fun obtenerCuentasGoogleDispositivo(): List<String> {
        return authRepository.obtenerCuentasGoogleDispositivo()
    }

    fun limpiarGoogleRequiereRegistro() {
        _googleRequiereRegistro.value = null
    }

    fun limpiarSolicitarCorreoGoogleManual() {
        _solicitarCorreoGoogleManual.value = false
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
        if (!esCorreoValido(correo)) {
            _mensaje.value = "Ingrese un correo electrónico válido"
            return
        }
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
