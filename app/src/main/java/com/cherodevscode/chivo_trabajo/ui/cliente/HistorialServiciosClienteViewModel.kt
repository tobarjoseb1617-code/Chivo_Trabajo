package com.cherodevscode.chivo_trabajo.ui.cliente

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

/**
 * ViewModel para la pantalla de Mis Servicios del Cliente (MVVM).
 * Consulta en Firestore las solicitudes del cliente autenticado y las clasifica
 * en tres categorías: En Curso, Pendientes/No Aceptados y Realizados.
 */
class HistorialServiciosClienteViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var listenerSolicitudes: ListenerRegistration? = null

    private val _serviciosFiltrados = MutableLiveData<List<DocumentSnapshot>>()
    val serviciosFiltrados: LiveData<List<DocumentSnapshot>> get() = _serviciosFiltrados

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    private val _countEnCurso = MutableLiveData<Int>(0)
    val countEnCurso: LiveData<Int> get() = _countEnCurso

    private val _countPendientes = MutableLiveData<Int>(0)
    val countPendientes: LiveData<Int> get() = _countPendientes

    private val _countRealizados = MutableLiveData<Int>(0)
    val countRealizados: LiveData<Int> get() = _countRealizados

    private var todasLasSolicitudes = listOf<DocumentSnapshot>()
    private var filtroActual = "EN_CURSO"
    private var busquedaActual = ""

    fun escucharServiciosCliente() {
        listenerSolicitudes?.remove()
        listenerSolicitudes = null

        val uid = auth.currentUser?.uid
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

        listenerSolicitudes = firestore.collection("Solicitudes")
            .whereEqualTo("clientId", uid)
            .addSnapshotListener { snapshot, error ->
                _cargando.value = false

                if (error != null) {
                    _serviciosFiltrados.value = emptyList()
                    return@addSnapshotListener
                }

                todasLasSolicitudes = snapshot?.documents
                    ?.sortedByDescending { it.getTimestamp("fechaCreacion")?.seconds ?: 0L }
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
        var countPendientes = 0
        var countRealizados = 0

        todasLasSolicitudes.forEach { doc ->
            val estado = doc.getString("estado")?.uppercase(Locale.ROOT).orEmpty()
            when (estado) {
                "EN_PROCESO", "EN PROCESO", "ACEPTADA", "ASIGNADA" -> countEnCurso++
                "PENDIENTE", "ABIERTA", "PUBLICADA" -> countPendientes++
                "FINALIZADA", "FINALIZADO", "COMPLETADA" -> countRealizados++
                else -> countPendientes++
            }
        }

        _countEnCurso.value = countEnCurso
        _countPendientes.value = countPendientes
        _countRealizados.value = countRealizados

        val filtradas = todasLasSolicitudes.filter { doc ->
            val estado = doc.getString("estado")?.uppercase(Locale.ROOT).orEmpty()

            val coincideFiltro = when (filtroActual) {
                "EN_CURSO" -> estado in listOf("EN_PROCESO", "EN PROCESO", "ACEPTADA", "ASIGNADA")
                "PENDIENTES" -> estado in listOf("PENDIENTE", "ABIERTA", "PUBLICADA")
                "REALIZADOS" -> estado in listOf("FINALIZADA", "FINALIZADO", "COMPLETADA")
                else -> true
            }

            val textoBusqueda = listOf(
                doc.getString("descripcion").orEmpty(),
                doc.getString("categoryId").orEmpty(),
                doc.getString("idSolicitud").orEmpty(),
                doc.id
            ).joinToString(" ")

            val coincideBusqueda = textoBusqueda.contains(busquedaActual, ignoreCase = true)

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
