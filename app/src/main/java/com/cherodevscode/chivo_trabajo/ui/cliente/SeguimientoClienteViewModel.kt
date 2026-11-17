package com.cherodevscode.chivo_trabajo.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel para la pantalla de Seguimiento del Cliente (MVVM).
 * Encapsula el estado del servicio en tiempo real, notificaciones de llegada del técnico,
 * verificación del PIN, lista de repuestos registrados por el profesional y evidencias.
 */
class SeguimientoClienteViewModel : ViewModel() {

    private val _pinSeguridad = MutableLiveData<String>("3821")
    val pinSeguridad: LiveData<String> get() = _pinSeguridad

    private val _estadoFase = MutableLiveData<String>("EN_CAMINO")
    val estadoFase: LiveData<String> get() = _estadoFase

    private val _tecnicoLlego = MutableLiveData<Boolean>(true)
    val tecnicoLlego: LiveData<Boolean> get() = _tecnicoLlego

    private val _pinVerificado = MutableLiveData<Boolean>(true)
    val pinVerificado: LiveData<Boolean> get() = _pinVerificado

    private val _repuestosAgregados = MutableLiveData<List<Pair<String, Double>>>(
        listOf(
            "Cinta teflón industrial" to 3.50,
            "Empaque de alta presión de 1/2\"" to 4.00
        )
    )
    val repuestosAgregados: LiveData<List<Pair<String, Double>>> get() = _repuestosAgregados

    private val _fotoAntesUrl = MutableLiveData<String>("https://res.cloudinary.com/djwvfjt7k/image/upload/v1790744838/o8ipleapa27fyd72nsll.png")
    val fotoAntesUrl: LiveData<String> get() = _fotoAntesUrl

    private val _fotoDespuesUrl = MutableLiveData<String?>()
    val fotoDespuesUrl: LiveData<String?> get() = _fotoDespuesUrl

    fun cargarDatosSeguimiento(pin: String?) {
        if (!pin.isNullOrBlank()) {
            _pinSeguridad.value = pin
        }
    }
}
