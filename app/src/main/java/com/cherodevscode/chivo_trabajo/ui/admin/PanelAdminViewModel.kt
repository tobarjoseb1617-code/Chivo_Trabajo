package com.cherodevscode.chivo_trabajo.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cherodevscode.chivo_trabajo.data.dto.EstadisticasSolicitudesAdmin
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class PanelAdminViewModel(application: Application) :
    AndroidViewModel(application) {

    private val firestoreRepository = FirestoreRepository()
    private val authRepository = AuthRepository(application)

    private val _estadisticas =
        MutableLiveData<EstadisticasSolicitudesAdmin>()

    val estadisticas: LiveData<EstadisticasSolicitudesAdmin> =
        _estadisticas

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _sesionCerrada = MutableLiveData<Boolean>()
    val sesionCerrada: LiveData<Boolean> = _sesionCerrada

    fun cargarEstadisticas() {
        viewModelScope.launch {
            val resultado =
                firestoreRepository.obtenerTodasLasSolicitudes()

            val solicitudes = resultado.getOrNull()

            if (solicitudes == null) {
                _error.value =
                    resultado.exceptionOrNull()?.message
                        ?: "No se pudieron cargar las estadísticas."
                return@launch
            }

            val estadisticas = EstadisticasSolicitudesAdmin(
                total = solicitudes.size,

                pendientes = solicitudes.count {
                    it.estado.equals("PENDIENTE", ignoreCase = true)
                },

                asignadas = solicitudes.count {
                    it.estado.equals("ASIGNADA", ignoreCase = true)
                },

                enCurso = solicitudes.count {
                    it.estado.equals("EN_CURSO", ignoreCase = true)
                },

                completadas = solicitudes.count {
                    it.estado.equals("COMPLETADA", ignoreCase = true)
                },

                canceladas = solicitudes.count {
                    it.estado.equals("CANCELADA", ignoreCase = true)
                }
            )

            _estadisticas.value = estadisticas
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _sesionCerrada.value = true
    }
}
