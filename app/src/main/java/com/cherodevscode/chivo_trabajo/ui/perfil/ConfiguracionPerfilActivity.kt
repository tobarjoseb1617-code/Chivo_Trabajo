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
import com.cherodevscode.chivo_trabajo.databinding.ActivityConfiguracionPerfilBinding
import com.cherodevscode.chivo_trabajo.ui.autenticacion.IniciarSesionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ConfiguracionPerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfiguracionPerfilBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            subirImagenACloudinary(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Zonas seguras y Status Bar (Edge-to-edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityConfiguracionPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val left = maxOf(systemBars.left, cutout.left)
            val top = maxOf(systemBars.top, cutout.top)
            val right = maxOf(systemBars.right, cutout.right)
            val bottom = maxOf(systemBars.bottom, cutout.bottom)
            view.setPadding(left, top, right, bottom)
            insets
        }

        window.statusBarColor = Color.WHITE
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        cargarDatosUsuario()

        binding.btnRegresar.setOnClickListener {
            finish()
        }

        // Selección de foto de perfil
        binding.ivFotoPerfil.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.tvCambiarFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.tvEditarPerfil.setOnClickListener {
            Toast.makeText(this, "Función de edición en desarrollo", Toast.LENGTH_SHORT).show()
        }

        binding.tvSoporte.setOnClickListener {
            Toast.makeText(this, "Contacto de soporte: soporte@chivotrabajo.sv", Toast.LENGTH_LONG).show()
        }

        binding.tvTerminos.setOnClickListener {
            Toast.makeText(this, "Términos y condiciones de ChivoTrabajo El Salvador", Toast.LENGTH_SHORT).show()
        }

        binding.btnCerrarSesion.setOnClickListener {
            authRepository.cerrarSesion()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, IniciarSesionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = authRepository.usuarioActual()
        if (currentUser != null) {
            binding.tvCorreoUsuario.text = currentUser.email ?: "Sin correo"
            binding.tvNombreUsuario.text = currentUser.displayName ?: "Usuario ChivoTrabajo"

            if (currentUser.photoUrl != null) {
                Glide.with(this)
                    .load(currentUser.photoUrl)
                    .circleCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.ivFotoPerfil)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val resultado = firestoreRepository.obtenerPerfilUsuario(currentUser.uid)
                withContext(Dispatchers.Main) {
                    resultado.onSuccess { usuario ->
                        if (usuario != null) {
                            val nombreCompleto = "${usuario.nombre} ${usuario.apellido}".trim()
                            if (nombreCompleto.isNotBlank()) {
                                binding.tvNombreUsuario.text = nombreCompleto
                            }
                            binding.tvCorreoUsuario.text = usuario.correo.ifBlank { currentUser.email ?: "" }
                            binding.tvTelefonoUsuario.text = usuario.telefono.ifBlank { "No registrado" }
                            binding.tvTipoUsuario.text = usuario.tipoUsuario
                            binding.tvEstadoVerificacion.text = usuario.estadoVerificacion

                            if (!usuario.fotoPerfil.isBlank()) {
                                Glide.with(this@ConfiguracionPerfilActivity)
                                    .load(usuario.fotoPerfil)
                                    .circleCrop()
                                    .placeholder(R.mipmap.ic_launcher)
                                    .into(binding.ivFotoPerfil)
                            }
                        }
                    }
                }
            }
        } else {
            binding.tvNombreUsuario.text = "Invitado"
            binding.tvCorreoUsuario.text = "No autenticado"
        }
    }

    private fun subirImagenACloudinary(imageUri: Uri) {
        val currentUser = authRepository.usuarioActual()
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para cambiar la foto", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Subiendo foto a Cloudinary...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            var uploadSuccess = false
            var secureUrl: String? = null
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            val presetConfig = BuildConfig.CLOUDINARY_UPLOAD_PRESET
            // Try user preset from local.properties first, then common fallbacks
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
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"profile_${currentUser.uid}.jpg\"\r\n")
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
                        secureUrl = matchResult?.groups?.get(1)?.value?.replace("\\/", "/")
                        if (!secureUrl.isNullOrBlank()) {
                            uploadSuccess = true
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Try next preset
                }
            }

            withContext(Dispatchers.Main) {
                if (uploadSuccess && !secureUrl.isNullOrBlank()) {
                    guardarUrlEnFirestore(currentUser.uid, secureUrl)
                } else {
                    Toast.makeText(
                        this@ConfiguracionPerfilActivity,
                        "Error al subir imagen. Verifique que exista un Upload Preset 'unsigned' en Cloudinary.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun guardarUrlEnFirestore(uid: String, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = firestoreRepository.actualizarFotoPerfil(uid, url)
            withContext(Dispatchers.Main) {
                res.onSuccess {
                    Toast.makeText(this@ConfiguracionPerfilActivity, "¡Foto de perfil actualizada con éxito!", Toast.LENGTH_SHORT).show()
                    Glide.with(this@ConfiguracionPerfilActivity)
                        .load(url)
                        .circleCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .into(binding.ivFotoPerfil)
                }.onFailure { e ->
                    Toast.makeText(this@ConfiguracionPerfilActivity, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
