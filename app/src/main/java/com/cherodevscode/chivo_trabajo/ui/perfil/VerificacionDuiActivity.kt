package com.cherodevscode.chivo_trabajo.ui.perfil

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.BuildConfig
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityVerificacionDuiBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class VerificacionDuiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerificacionDuiBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    private var frenteUri: Uri? = null
    private var dorsoUri: Uri? = null

    private val escanearFrenteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uriStr = result.data?.getStringExtra("EXTRA_IMAGE_URI")
            val duiNum = result.data?.getStringExtra("EXTRA_DUI_NUMERO")
            if (uriStr != null) {
                frenteUri = Uri.parse(uriStr)
                Glide.with(this).load(frenteUri).into(binding.ivDuiFrente)
            }
            if (!duiNum.isNullOrBlank()) {
                binding.etDuiNumero.setText(duiNum)
            } else {
                Toast.makeText(this, "No se pudo extraer automáticamente el DUI. Escanee nuevamente o verifique la nitidez.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val escanearDorsoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uriStr = result.data?.getStringExtra("EXTRA_IMAGE_URI")
            if (uriStr != null) {
                dorsoUri = Uri.parse(uriStr)
                Glide.with(this).load(dorsoUri).into(binding.ivDuiDorso)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityVerificacionDuiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            view.setPadding(
                maxOf(systemBars.left, cutout.left),
                maxOf(systemBars.top, cutout.top),
                maxOf(systemBars.right, cutout.right),
                maxOf(systemBars.bottom, cutout.bottom)
            )
            insets
        }

        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Hacer el input de DUI de solo lectura (no editable manualmente)
        binding.etDuiNumero.isFocusable = false
        binding.etDuiNumero.isClickable = false
        binding.etDuiNumero.isCursorVisible = false
        binding.etDuiNumero.isFocusableInTouchMode = false

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        cargarEstadoActual()

        binding.btnRegresar.setOnClickListener {
            finish()
        }

        binding.btnSeleccionarFrente.setOnClickListener {
            val intent = Intent(this, EscanearDuiCameraActivity::class.java).apply {
                putExtra("EXTRA_ES_FRENTE", true)
            }
            escanearFrenteLauncher.launch(intent)
        }

        binding.btnSeleccionarDorso.setOnClickListener {
            val intent = Intent(this, EscanearDuiCameraActivity::class.java).apply {
                putExtra("EXTRA_ES_FRENTE", false)
            }
            escanearDorsoLauncher.launch(intent)
        }

        binding.btnEnviarDui.setOnClickListener {
            val duiStr = binding.etDuiNumero.text.toString().trim()
            if (duiStr.isEmpty()) {
                Toast.makeText(this, "Debe escanear la parte frontal de su DUI para extraer el número automáticamente.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (frenteUri == null || dorsoUri == null) {
                Toast.makeText(this, "Debe capturar la fotografía frontal y del dorso del DUI", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            subirDuiYGuardar(duiStr)
        }
    }

    private fun cargarEstadoActual() {
        val currentUser = authRepository.usuarioActual()
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val resultado = firestoreRepository.obtenerPerfilUsuario(currentUser.uid)
                withContext(Dispatchers.Main) {
                    resultado.onSuccess { usuario ->
                        if (usuario != null) {
                            binding.tvEstadoVerificacionDui.text = usuario.estadoVerificacion
                            if (usuario.dui.isNotBlank()) {
                                binding.etDuiNumero.setText(usuario.dui)
                            }
                            if (usuario.duiFrenteUrl.isNotBlank()) {
                                Glide.with(this@VerificacionDuiActivity).load(usuario.duiFrenteUrl).into(binding.ivDuiFrente)
                            }
                            if (usuario.duiDorsoUrl.isNotBlank()) {
                                Glide.with(this@VerificacionDuiActivity).load(usuario.duiDorsoUrl).into(binding.ivDuiDorso)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun subirDuiYGuardar(duiNumero: String) {
        val currentUser = authRepository.usuarioActual()
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar estado de carga y bloquear botones
        binding.btnSeleccionarFrente.isEnabled = false
        binding.btnSeleccionarDorso.isEnabled = false
        binding.btnEnviarDui.isEnabled = false
        binding.btnEnviarDui.text = "Subiendo y verificando..."
        Toast.makeText(this, "Subiendo imágenes a Cloudinary...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            val frenteUrl = subirImagenACloudinary(frenteUri!!, "dui_frente_${currentUser.uid}")
            val dorsoUrl = subirImagenACloudinary(dorsoUri!!, "dui_dorso_${currentUser.uid}")

            withContext(Dispatchers.Main) {
                if (frenteUrl != null && dorsoUrl != null) {
                    guardarEnFirestore(currentUser.uid, duiNumero, frenteUrl, dorsoUrl)
                } else {
                    binding.btnSeleccionarFrente.isEnabled = true
                    binding.btnSeleccionarDorso.isEnabled = true
                    binding.btnEnviarDui.isEnabled = true
                    binding.btnEnviarDui.text = "Enviar para Verificación"
                    Toast.makeText(this@VerificacionDuiActivity, "Error al subir imágenes a Cloudinary", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun subirImagenACloudinary(imageUri: Uri, publicIdHint: String): String? {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val presetConfig = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        val presets = listOf(presetConfig, "chivo_trabajo", "chivo_trabajo_preset", "ml_default", "preset_chivo")

        for (preset in presets) {
            try {
                val urlString = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.doInput = true

                val boundary = "Boundary-${System.currentTimeMillis()}"
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                val outputStream = DataOutputStream(connection.outputStream)

                // Write upload_preset
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                outputStream.writeBytes("$preset\r\n")

                // Write file
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$publicIdHint.jpg\"\r\n")
                outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")

                contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }

                outputStream.writeBytes("\r\n--$boundary--\r\n")
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    val regex = "\"secure_url\"\\s*:\\s*\"(.*?)\"".toRegex()
                    val matchResult = regex.find(responseString)
                    val secureUrl = matchResult?.groups?.get(1)?.value?.replace("\\/", "/")
                    if (!secureUrl.isNullOrBlank()) {
                        return secureUrl
                    }
                }
            } catch (e: Exception) {
                // Try next preset
            }
        }
        return null
    }

    private fun guardarEnFirestore(uid: String, dui: String, frenteUrl: String, dorsoUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = firestoreRepository.actualizarVerificacionDuiCompleta(uid, dui, frenteUrl, dorsoUrl, "VERIFICADO")
            withContext(Dispatchers.Main) {
                res.onSuccess {
                    Toast.makeText(this@VerificacionDuiActivity, "¡Identidad verificada exitosamente!", Toast.LENGTH_LONG).show()
                    finish() // Regresa a la página de perfil
                }.onFailure { e ->
                    binding.btnSeleccionarFrente.isEnabled = true
                    binding.btnSeleccionarDorso.isEnabled = true
                    binding.btnEnviarDui.isEnabled = true
                    binding.btnEnviarDui.text = "Enviar para Verificación"
                    Toast.makeText(this@VerificacionDuiActivity, "Error al actualizar Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
