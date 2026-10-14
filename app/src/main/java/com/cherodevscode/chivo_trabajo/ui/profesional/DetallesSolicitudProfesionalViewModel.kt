package com.cherodevscode.chivo_trabajo.ui.profesional

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.model.Propuesta
import com.cherodevscode.chivo_trabajo.data.model.Solicitud
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetallesSolicitudProfesionalViewModel : ViewModel() {

    private val firestoreRepository = FirestoreRepository()

    private val _solicitud = MutableLiveData<Solicitud?>()
    val solicitud: LiveData<Solicitud?> get() = _solicitud

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    private val _enviandoPropuesta = MutableLiveData<Boolean>(false)
    val enviandoPropuesta: LiveData<Boolean>
        get() = _enviandoPropuesta

    private val _resultadoPropuesta =
        MutableLiveData<Result<String>>()
    val resultadoPropuesta: LiveData<Result<String>>
        get() = _resultadoPropuesta

    // Carga los detalles de la solicitud.
    fun cargarDetallesSolicitud(idSolicitud: String) {
        if (idSolicitud.isBlank()) return

        _cargando.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val resultado =
                firestoreRepository.obtenerSolicitudPorId(idSolicitud)

            withContext(Dispatchers.Main) {
                _cargando.value = false

                resultado.onSuccess { solicitud ->
                    _solicitud.value = solicitud
                }.onFailure {
                    _solicitud.value = null
                }
            }
        }
    }

    // Envía la propuesta del profesional a Firestore.
    fun enviarPropuesta(propuesta: Propuesta) {
        if (_enviandoPropuesta.value == true) return

        _enviandoPropuesta.value = true

        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                firestoreRepository.enviarPropuesta(propuesta)
            }

            _enviandoPropuesta.value = false
            _resultadoPropuesta.value = resultado
        }
    }
}
