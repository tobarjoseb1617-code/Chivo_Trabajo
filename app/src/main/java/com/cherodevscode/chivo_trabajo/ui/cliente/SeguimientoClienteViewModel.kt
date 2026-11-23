package com.cherodevscode.chivo_trabajo.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.model.DetalleServicioCliente
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class SeguimientoClienteViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _detalleServicio =
        MutableLiveData<DetalleServicioCliente?>()
    val detalleServicio: LiveData<DetalleServicioCliente?>
        get() = _detalleServicio

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> get() = _cargando

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> get() = _mensajeError

    fun cargarDatosSolicitud(
        idSolicitud: String,
        uidCliente: String
    ) {
        if (idSolicitud.isBlank() || uidCliente.isBlank()) {
            _mensajeError.value = "No se pudo identificar la solicitud."
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null
            _detalleServicio.value = null

            try {
                val resultadoSolicitud =
                    repository.obtenerSolicitudPorId(idSolicitud)

                val solicitud = resultadoSolicitud.getOrElse {
                    _mensajeError.value =
                        it.message ?: "No se pudo cargar la solicitud."
                    return@launch
                }

                if (solicitud == null) {
                    _mensajeError.value = "No se encontró la solicitud."
                    return@launch
                }

                if (solicitud.clientId != uidCliente) {
                    _mensajeError.value =
                        "No tienes permiso para consultar esta solicitud."
                    return@launch
                }

                val uidProfesional = solicitud.profesionalId.orEmpty()

                if (uidProfesional.isBlank()) {
                    _detalleServicio.value = DetalleServicioCliente(
                        solicitud = solicitud
                    )
                    return@launch
                }

                val resultadoPropuesta =
                    repository.obtenerPropuestaPorSolicitudYProfesional(
                        idSolicitud,
                        uidProfesional
                    )

                val resultadoUsuario =
                    repository.obtenerPerfilUsuario(uidProfesional)

                val resultadoProfesional =
                    repository.obtenerProfesional(uidProfesional)

                val detalle = DetalleServicioCliente(
                    solicitud = solicitud,
                    propuesta = resultadoPropuesta.getOrNull(),
                    usuarioProfesional = resultadoUsuario.getOrNull(),
                    profesional = resultadoProfesional.getOrNull()
                )

                _detalleServicio.value = detalle

                if (
                    resultadoPropuesta.isFailure ||
                    resultadoUsuario.isFailure ||
                    resultadoProfesional.isFailure
                ) {
                    _mensajeError.value =
                        "Algunos datos del profesional no pudieron cargarse."
                }

            } catch (e: Exception) {
                _mensajeError.value =
                    e.message ?: "Ocurrió un error al cargar el servicio."
            } finally {
                _cargando.value = false
            }
        }
    }
}
