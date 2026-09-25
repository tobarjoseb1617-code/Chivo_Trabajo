package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityRegistroUsuarioPaso2Binding
import com.cherodevscode.chivo_trabajo.ui.cliente.InicioActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroClientePaso2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroUsuarioPaso2Binding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    private var latitudSeleccionada: Double = 13.6894
    private var longitudSeleccionada: Double = -89.2361

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroUsuarioPaso2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        // Configurar WebView con Leaflet
        binding.webViewMapa.settings.javaScriptEnabled = true
        binding.webViewMapa.webViewClient = WebViewClient()
        binding.webViewMapa.addJavascriptInterface(WebAppInterface { lat, lng ->
            latitudSeleccionada = lat
            longitudSeleccionada = lng
            runOnUiThread {
                binding.tvCoordenadas.text = "🟢 Lat: %.4f, Lng: %.4f".format(lat, lng)
            }
        }, "Android")

        binding.webViewMapa.loadUrl("file:///android_asset/leaflet_map.html")

        // Botón Regresar
        binding.btnRegresarRegistroPaso2.setOnClickListener {
            finish()
        }

        // 1. Completar Registro y Guardar Dirección
        binding.btnCompletarRegistro.setOnClickListener {
            val municipio = binding.etMunicipio.text.toString().trim()
            val direccion = binding.etDireccionExacta.text.toString().trim()
            val referencia = binding.etPuntoReferencia.text.toString().trim()

            if (municipio.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(this, "Por favor complete el municipio y la dirección exacta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = authRepository.usuarioActual()?.uid
            if (uid == null) {
                Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                val perfilRes = withContext(Dispatchers.IO) {
                    firestoreRepository.obtenerPerfilUsuario(uid)
                }

                perfilRes.onSuccess { usuarioActual ->
                    if (usuarioActual != null) {
                        val usuarioActualizado = usuarioActual.copy(
                            ciudad = municipio,
                            direccion = "$direccion (Ref: $referencia)",
                            latitud = latitudSeleccionada,
                            longitud = longitudSeleccionada
                        )
                        val saveRes = withContext(Dispatchers.IO) {
                            firestoreRepository.guardarPerfilUsuario(usuarioActualizado)
                        }
                        saveRes.onSuccess {
                            Toast.makeText(this@RegistroClientePaso2Activity, "¡Registro completado con éxito!", Toast.LENGTH_LONG).show()
                            irAInicio()
                        }.onFailure { e ->
                            Toast.makeText(this@RegistroClientePaso2Activity, "Error al guardar dirección: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@RegistroClientePaso2Activity, "¡Registro completado!", Toast.LENGTH_LONG).show()
                        irAInicio()
                    }
                }.onFailure {
                    Toast.makeText(this@RegistroClientePaso2Activity, "¡Registro completado!", Toast.LENGTH_LONG).show()
                    irAInicio()
                }
            }
        }

        // 2. Omitir / Configurar más tarde
        binding.btnOmitirDireccion.setOnClickListener {
            Toast.makeText(this, "Dirección omitida por ahora", Toast.LENGTH_SHORT).show()
            irAInicio()
        }
    }

    private fun irAInicio() {
        val intent = Intent(this, InicioActivity::class.java)
        startActivity(intent)
        finish()
    }

    inner class WebAppInterface(private val onCoordinatesChanged: (Double, Double) -> Unit) {
        @JavascriptInterface
        fun setCoordinates(lat: Double, lng: Double) {
            onCoordinatesChanged(lat, lng)
        }
    }
}
