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
 * ViewModel que encapsula la lógica de negocio, escucha en tiempo real de Firestore
 * y filtrado de solicitudes para HistorialSolicitudesClienteActivity (Patrón MVVM).
 */
class HistorialSolicitudesViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var listenerSolicitudes: ListenerRegistration? = null

    private val _solicitudesFiltradas = MutableLiveData<List<DocumentSnapshot>>()
    val solicitudesFiltradas: LiveData<List<DocumentSnapshot>> get() = _solicitudesFiltradas

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    private var todasLasSolicitudes = listOf<DocumentSnapshot>()
    private var filtroActual = "TODAS"
    private var busquedaActual = ""

    // Iniciar escucha en tiempo real de las solicitudes del cliente autenticado
    fun escucharSolicitudesCliente() {
        listenerSolicitudes?.remove()
        listenerSolicitudes = null

        val uid = auth.currentUser?.uid
        if (uid == null) {
            todasLasSolicitudes = emptyList()
            _cargando.value = false
            _solicitudesFiltradas.value = emptyList()
            return
        }

        _cargando.value = true

        listenerSolicitudes = firestore.collection("Solicitudes")
            .whereEqualTo("clientId", uid)
            .addSnapshotListener { snapshot, error ->
                _cargando.value = false

                if (error != null) {
                    _solicitudesFiltradas.value = emptyList()
                    return@addSnapshotListener
                }

                todasLasSolicitudes = snapshot?.documents
                    ?.sortedByDescending { it.getTimestamp("fechaCreacion")?.seconds ?: 0L }
                    ?: emptyList()

                aplicarFiltroYBusqueda()
            }
    }

    // Cambiar filtro (TODAS, ABIERTAS, EN_PROCESO, FINALIZADAS)
    fun setFiltro(filtro: String) {
        filtroActual = filtro
        aplicarFiltroYBusqueda()
    }

    // Actualizar texto de búsqueda
    fun setBusqueda(texto: String) {
        busquedaActual = texto.trim()
        aplicarFiltroYBusqueda()
    }

    // Aplicar lógica de filtrado por estado y texto de búsqueda
    private fun aplicarFiltroYBusqueda() {
        val filtradas = todasLasSolicitudes.filter { documento ->
            val estado = documento.getString("estado")?.uppercase(Locale.ROOT).orEmpty()

            val coincideEstado = when (filtroActual) {
                "ABIERTAS" -> estado in listOf("PENDIENTE", "ABIERTA", "PUBLICADA")
                "EN_PROCESO" -> estado in listOf("EN_PROCESO", "EN PROCESO", "ACEPTADA", "ASIGNADA")
                "FINALIZADAS" -> estado in listOf("FINALIZADA", "FINALIZADO", "COMPLETADA")
                else -> true
            }

            val textoBusqueda = listOf(
                documento.getString("descripcion").orEmpty(),
                documento.getString("categoryId").orEmpty(),
                documento.getString("idSolicitud").orEmpty(),
                documento.id
            ).joinToString(" ")

            val coincideBusqueda = textoBusqueda.contains(busquedaActual, ignoreCase = true)

            coincideEstado && coincideBusqueda
        }

        _solicitudesFiltradas.value = filtradas
    }

    override fun onCleared() {
        super.onCleared()
        // Remover listener de Firestore para evitar fugas de memoria (Memory Leaks)
        listenerSolicitudes?.remove()
        listenerSolicitudes = null
    }
}
