package com.cherodevscode.chivo_trabajo.ui.cliente

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cherodevscode.chivo_trabajo.databinding.ActivitySeleccionarUbicacionBinding
import java.util.Locale

class SeleccionarUbicacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionarUbicacionBinding
    private var latitudSeleccionada: Double = 13.6894
    private var longitudSeleccionada: Double = -89.2361
    private var direccionSeleccionada: String = "San Salvador, El Salvador"
    private var ciudadSeleccionada: String = "San Salvador"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarUbicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón regresar
        binding.btnRegresarMapa.setOnClickListener {
            finish()
        }

        // Configurar WebView con Leaflet Map
        configurarMapa()

        // Botón Ubicación Actual (GPS)
        binding.btnUbicacionActual.setOnClickListener {
            obtenerUbicacionActual()
        }

        // Botón Buscar Dirección
        binding.btnBuscarUbicacion.setOnClickListener {
            val query = binding.etBuscarUbicacion.text.toString().trim()
            if (query.isNotEmpty()) {
                buscarDireccion(query)
            } else {
                Toast.makeText(this, "Escribe una dirección para buscar", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Confirmar Ubicación
        binding.btnConfirmarUbicacion.setOnClickListener {
            val intent = Intent().apply {
                putExtra("EXTRA_LAT", latitudSeleccionada)
                putExtra("EXTRA_LNG", longitudSeleccionada)
                putExtra("EXTRA_DIRECCION", direccionSeleccionada)
                putExtra("EXTRA_CIUDAD", ciudadSeleccionada)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    // Configurar WebView e interfaz JavaScript para recibir coordenadas
    private fun configurarMapa() {
        binding.mapWebView.settings.javaScriptEnabled = true
        binding.mapWebView.settings.domStorageEnabled = true
        binding.mapWebView.addJavascriptInterface(WebAppInterface(), "Android")
        binding.mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Centrar mapa por defecto en San Salvador
                view?.evaluateJavascript("if(typeof map !== 'undefined') { map.setView([13.6894, -89.2361], 14); }", null)
            }
        }
        binding.mapWebView.loadUrl("file:///android_asset/leaflet_map.html")
    }

    // Interfaz JavaScript para comunicar el WebView con Kotlin nativo
    inner class WebAppInterface {
        @JavascriptInterface
        fun setCoordinates(lat: Double, lng: Double) {
            latitudSeleccionada = lat
            longitudSeleccionada = lng
            resolverDireccion(lat, lng)
        }
    }

    // Resolver dirección legible a partir de lat/lng mediante Geocoder
    private fun resolverDireccion(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this, Locale("es", "SV"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                direccionSeleccionada = address.getAddressLine(0) ?: "Ubicación en mapa"
                ciudadSeleccionada = address.locality ?: address.subAdminArea ?: "San Salvador"
            } else {
                direccionSeleccionada = "Lat: $lat, Lng: $lng"
                ciudadSeleccionada = "San Salvador"
            }
        } catch (e: Exception) {
            direccionSeleccionada = "Lat: $lat, Lng: $lng"
            ciudadSeleccionada = "San Salvador"
        }
    }

    // Buscar dirección usando Geocoder
    private fun buscarDireccion(query: String) {
        try {
            val geocoder = Geocoder(this, Locale("es", "SV"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                latitudSeleccionada = address.latitude
                longitudSeleccionada = address.longitude
                direccionSeleccionada = address.getAddressLine(0) ?: query
                ciudadSeleccionada = address.locality ?: "San Salvador"

                // Mover marcador en el mapa WebView
                binding.mapWebView.evaluateJavascript(
                    "if(typeof map !== 'undefined' && typeof marker !== 'undefined') { " +
                            "map.setView([$latitudSeleccionada, $longitudSeleccionada], 16); " +
                            "marker.setLatLng([$latitudSeleccionada, $longitudSeleccionada]); }",
                    null
                )
                Toast.makeText(this, "Ubicación encontrada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al buscar ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    // Obtener ubicación GPS actual del dispositivo
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            latitudSeleccionada = location.latitude
            longitudSeleccionada = location.longitude
            resolverDireccion(latitudSeleccionada, longitudSeleccionada)

            binding.mapWebView.evaluateJavascript(
                "if(typeof map !== 'undefined' && typeof marker !== 'undefined') { " +
                        "map.setView([$latitudSeleccionada, $longitudSeleccionada], 16); " +
                        "marker.setLatLng([$latitudSeleccionada, $longitudSeleccionada]); }",
                null
            )
            Toast.makeText(this, "Ubicación actual obtenida", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se pudo obtener la ubicación actual por GPS", Toast.LENGTH_SHORT).show()
        }
    }
}
