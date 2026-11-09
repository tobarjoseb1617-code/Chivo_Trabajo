package com.cherodevscode.chivo_trabajo.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.launch
import com.cherodevscode.chivo_trabajo.data.dto.DetallesSolicitudAdmin


class DetallesSolicitudAdminViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _detalles = MutableLiveData<DetallesSolicitudAdmin>()
    val detalles: LiveData<DetallesSolicitudAdmin> = _detalles

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun cargarDetalles(idSolicitud: String) {
        viewModelScope.launch {
            _cargando.value = true

            try {
                val resultadoSolicitud =
                    repository.obtenerSolicitudPorId(idSolicitud)

                val solicitud = resultadoSolicitud.getOrNull()

                if (solicitud == null) {
                    _error.value = "No se encontró la solicitud."
                    return@launch
                }

                val resultadoCliente =
                    repository.obtenerPerfilUsuario(solicitud.clientId)

                val cliente = resultadoCliente.getOrNull()

                val nombreCliente = if (cliente != null) {
                    "${cliente.nombre} ${cliente.apellido}".trim()
                } else {
                    "Cliente no disponible"
                }

                val nombreProfesional = if (!solicitud.profesionalId.isNullOrBlank()) {
                    val resultadoProfesional =
                        repository.obtenerPerfilUsuario(solicitud.profesionalId)

                    val profesional = resultadoProfesional.getOrNull()

                    if (profesional != null) {
                        "${profesional.nombre} ${profesional.apellido}".trim()
                    } else {
                        "Profesional no disponible"
                    }
                } else {
                    "Esta solicitud aún no tiene un profesional asignado."
                }

                _detalles.value = DetallesSolicitudAdmin(
                    solicitud = solicitud,
                    nombreCliente = nombreCliente,
                    nombreProfesional = nombreProfesional
                )

            } catch (e: Exception) {
                _error.value = "Error al cargar los detalles de la solicitud."
            } finally {
                _cargando.value = false
            }
        }
    }
}