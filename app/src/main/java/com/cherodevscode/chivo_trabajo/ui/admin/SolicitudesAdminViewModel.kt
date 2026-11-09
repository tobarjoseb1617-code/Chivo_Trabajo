package com.cherodevscode.chivo_trabajo.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class SolicitudesAdminViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _solicitudes = MutableLiveData<List<Solicitud>>()
    val solicitudes: LiveData<List<Solicitud>> = _solicitudes

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarSolicitudes() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            val resultado = repository.obtenerTodasLasSolicitudes()

            resultado.onSuccess { lista ->
                _solicitudes.value = lista.sortedByDescending {
                    it.fechaCreacion
                }
            }.onFailure { excepcion ->
                _error.value = excepcion.message
                    ?: "No se pudieron cargar las solicitudes"
            }

            _cargando.value = false
        }
    }
}