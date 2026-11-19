package com.cherodevscode.chivo_trabajo.ui.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class HistorialSolicitudesViewModel(application: Application) :
    AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val authRepository = AuthRepository(application)

    private var listenerSolicitudes: ListenerRegistration? = null

    private val _solicitudesFiltradas =
        MutableLiveData<List<DocumentSnapshot>>()
    val solicitudesFiltradas: LiveData<List<DocumentSnapshot>> =
        _solicitudesFiltradas

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private var todasLasSolicitudes = listOf<DocumentSnapshot>()
    private var filtroActual = "TODAS"
    private var busquedaActual = ""

    fun escucharSolicitudesCliente() {
        listenerSolicitudes?.remove()
        listenerSolicitudes = null

        val uid = authRepository.usuarioActual()?.uid

        if (uid.isNullOrEmpty()) {
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
                    ?.sortedByDescending {
                        it.getTimestamp("fechaCreacion")?.seconds ?: 0L
                    } ?: emptyList()

                aplicarFiltroYBusqueda()
            }
    }

    fun setFiltro(filtro: String) {
        filtroActual = filtro
        aplicarFiltroYBusqueda()
    }

    fun setBusqueda(texto: String) {
        busquedaActual = texto.trim()
        aplicarFiltroYBusqueda()
    }

    private fun aplicarFiltroYBusqueda() {
        val filtradas = todasLasSolicitudes.filter { documento ->

            val estado = documento.getString("estado")
                ?.uppercase(Locale.ROOT)
                .orEmpty()

            val coincideEstado = when (filtroActual) {
                "ABIERTAS" -> estado in listOf(
                    "PENDIENTE", "ABIERTA", "PUBLICADA"
                )

                "EN_PROCESO" -> estado in listOf(
                    "EN_PROCESO", "EN PROCESO", "ACEPTADA", "ASIGNADA"
                )

                "FINALIZADAS" -> estado in listOf(
                    "FINALIZADA", "FINALIZADO", "COMPLETADA"
                )

                else -> true
            }

            val textoBusqueda = listOf(
                documento.getString("descripcion").orEmpty(),
                documento.getString("categoryId").orEmpty(),
                documento.getString("idSolicitud").orEmpty(),
                documento.id
            ).joinToString(" ")

            val coincideBusqueda = textoBusqueda.contains(
                busquedaActual,
                ignoreCase = true
            )

            coincideEstado && coincideBusqueda
        }

        _solicitudesFiltradas.value = filtradas
    }

    override fun onCleared() {
        listenerSolicitudes?.remove()
        listenerSolicitudes = null
        super.onCleared()
    }
}
