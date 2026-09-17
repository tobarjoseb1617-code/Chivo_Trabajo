package com.cherodevscode.chivo_trabajo.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.AuthRepository
import com.cherodevscode.chivo_trabajo.data.FirestoreRepository
import com.cherodevscode.chivo_trabajo.data.Registro
import kotlinx.coroutines.launch


class MainViewModel(private val context: Context) : ViewModel() {
    private val authRepository = AuthRepository(context)
    private val firestoreRepository = FirestoreRepository()

    private val _usuario = MutableLiveData(authRepository.usuarioActual())
    val usuario = _usuario as LiveData<com.google.firebase.auth.FirebaseUser?>

    private val _registros = MutableLiveData<List<Registro>>(emptyList())
    val registros: LiveData<List<Registro>> = _registros

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    fun iniciarSesion() {
        viewModelScope.launch {
            val resultado = authRepository.iniciarSesionConGoogle()
            resultado.onSuccess {
                _usuario.value = authRepository.usuarioActual()
                cargarRegistros()
            }.onFailure { error ->
                _mensaje.value = "No se pudo iniciar sesion: ${error.message}"
            }
        }
    }

    fun cargarRegistros() {
        val uid = authRepository.usuarioActual()?.uid
        if (uid == null) {
            _mensaje.value = "Inicie sesion para consultar datos"
            return
        }

        firestoreRepository.consultarRegistros(
            uid = uid,
            alExito = { lista -> _registros.value = lista },
            alError = { error -> _mensaje.value = error }
        )
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _usuario.value = null
        _registros.value = emptyList()
    }
}