package com.cherodevscode.chivo_trabajo.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class HistorialServiciosClienteViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private var listenerSolicitudes: ListenerRegistration? = null

    private val _serviciosFiltrados =
        MutableLiveData<List<DocumentSnapshot>>()
    val serviciosFiltrados: LiveData<List<DocumentSnapshot>>
        get() = _serviciosFiltrados

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean>
        get() = _cargando

    private val _countEnCurso = MutableLiveData(0)
    val countEnCurso: LiveData<Int>
        get() = _countEnCurso

    private val _countPendientes = MutableLiveData(0)
    val countPendientes: LiveData<Int>
        get() = _countPendientes

    private val _countRealizados = MutableLiveData(0)
    val countRealizados: LiveData<Int>
        get() = _countRealizados

    private var todasLasSolicitudes = listOf<DocumentSnapshot>()
    private var filtroActual = "EN_CURSO"
    private var busquedaActual = ""

    fun escucharServiciosCliente(uid: String?) {

        listenerSolicitudes?.remove()
        listenerSolicitudes = null

        if (uid.isNullOrBlank()) {
            todasLasSolicitudes = emptyList()
            _cargando.value = false
            _serviciosFiltrados.value = emptyList()
            _countEnCurso.value = 0
            _countPendientes.value = 0
            _countRealizados.value = 0
            return
        }

        _cargando.value = true

        listenerSolicitudes = firestore
            .collection("Solicitudes")
            .whereEqualTo("clientId", uid)
            .addSnapshotListener { snapshot, error ->

                _cargando.value = false

                if (error != null) {
                    _serviciosFiltrados.value = emptyList()
                    return@addSnapshotListener
                }

                todasLasSolicitudes = snapshot?.documents
                    ?.sortedByDescending {
                        it.getTimestamp("fechaCreacion")?.seconds ?: 0L
                    }
                    ?: emptyList()

                actualizarContadoresYFiltrar()
            }
    }

    fun setFiltro(filtro: String) {
        filtroActual = filtro
        actualizarContadoresYFiltrar()
    }

    fun setBusqueda(texto: String) {
        busquedaActual = texto.trim()
        actualizarContadoresYFiltrar()
    }

    private fun actualizarContadoresYFiltrar() {

        var countEnCurso = 0
        var countAsignadas = 0
        var countRealizados = 0

        todasLasSolicitudes.forEach { doc ->

            val estado = doc.getString("estado")
                ?.trim()
                ?.uppercase(Locale.ROOT)
                .orEmpty()

            when (estado) {
                "EN_CURSO", "EN CURSO",
                "EN_PROCESO", "EN PROCESO" -> countEnCurso++

                "ACEPTADA", "ASIGNADA" -> countAsignadas++

                "FINALIZADA", "FINALIZADO",
                "COMPLETADA" -> countRealizados++
            }
        }

        _countEnCurso.value = countEnCurso
        _countPendientes.value = countAsignadas
        _countRealizados.value = countRealizados

        val filtradas = todasLasSolicitudes.filter { doc ->

            val estado = doc.getString("estado")
                ?.trim()
                ?.uppercase(Locale.ROOT)
                .orEmpty()

            val coincideFiltro = when (filtroActual) {

                "EN_CURSO" -> estado in listOf(
                    "EN_CURSO",
                    "EN CURSO",
                    "EN_PROCESO",
                    "EN PROCESO"
                )

                "ASIGNADAS" -> estado in listOf(
                    "ACEPTADA",
                    "ASIGNADA"
                )

                "REALIZADOS" -> estado in listOf(
                    "FINALIZADA",
                    "FINALIZADO",
                    "COMPLETADA"
                )

                else -> true
            }

            val textoBusqueda = listOf(
                doc.getString("descripcion").orEmpty(),
                doc.getString("categoryId").orEmpty(),
                doc.getString("idSolicitud").orEmpty(),
                doc.id
            ).joinToString(" ")

            val coincideBusqueda = textoBusqueda.contains(
                busquedaActual,
                ignoreCase = true
            )

            coincideFiltro && coincideBusqueda
        }

        _serviciosFiltrados.value = filtradas
    }

    override fun onCleared() {
        super.onCleared()
        listenerSolicitudes?.remove()
        listenerSolicitudes = null
    }
}
