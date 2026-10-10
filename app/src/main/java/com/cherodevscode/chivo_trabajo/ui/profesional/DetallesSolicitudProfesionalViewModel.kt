package com.cherodevscode.chivo_trabajo.ui.profesional

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para gestionar la obtención de los detalles de una solicitud para el profesional (Patrón MVVM).
 */
class DetallesSolicitudProfesionalViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    private val _solicitud = MutableLiveData<Solicitud?>()
    val solicitud: LiveData<Solicitud?> get() = _solicitud

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    // Cargar solicitud por su ID desde Firestore
    fun cargarDetallesSolicitud(idSolicitud: String) {
        if (idSolicitud.isBlank()) return

        _cargando.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val resultado = firestoreRepository.obtenerSolicitudPorId(idSolicitud)
            withContext(Dispatchers.Main) {
                _cargando.value = false
                resultado.onSuccess { sol ->
                    _solicitud.value = sol
                }.onFailure {
                    _solicitud.value = null
                }
            }
        }
    }
}
