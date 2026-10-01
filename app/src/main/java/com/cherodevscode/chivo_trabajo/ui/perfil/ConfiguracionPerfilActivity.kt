package com.cherodevscode.chivo_trabajo.ui.perfil

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.BuildConfig
import com.cherodevscode.chivo_trabajo.utils.ThemePreferences
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityConfiguracionPerfilBinding
import com.cherodevscode.chivo_trabajo.ui.autenticacion.IniciarSesionActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.CrearPortafolioActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.PortafolioProfesionalActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

// Apariencia: los colores creados desde Kotlin respetan el modo elegido.
class ConfiguracionPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracionPerfilBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val cutout =
                insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val left = maxOf(systemBars.left, cutout.left)
            val top = maxOf(systemBars.top, cutout.top)
            val right = maxOf(systemBars.right, cutout.right)
            val bottom = maxOf(systemBars.bottom, cutout.bottom)

            view.setPadding(
                left,
                top,
                right,
                bottom
            )

            insets
        }

        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, com.cherodevscode.chivo_trabajo.R.color.ui_background)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = resources.getBoolean(R.bool.light_system_bars)


        // Apariencia: inicializar antes del listener evita un cambio al cargar el interruptor.
        binding.switchModoOscuro.isChecked = ThemePreferences.isDark(this)
        binding.switchModoOscuro.setOnCheckedChangeListener { _, activado ->
            // AppCompat recrea las pantallas para aplicar también layouts, controles y diálogos.
            ThemePreferences.setDark(this, activado)
        }

        // Inicializar repositorios
        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()


        // ---------------------------------------------------------
        // PORTAFOLIO OCULTO POR DEFECTO
        // ---------------------------------------------------------
        // Solo se mostrará después si Firestore confirma
        // que el usuario es PROFESIONAL.
        binding.btnMiPortafolio.visibility = View.GONE


        // Cargar información del usuario
        cargarDatosUsuario()


        // ---------------------------------------------------------
        // BOTÓN REGRESAR
        // ---------------------------------------------------------

        binding.btnRegresar.setOnClickListener {
            finish()
        }


        // ---------------------------------------------------------
        // CAMBIAR FOTO DE PERFIL
        // ---------------------------------------------------------

        binding.ivFotoPerfil.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.tvCambiarFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        // ---------------------------------------------------------
        // EDITAR PERFIL
        // ---------------------------------------------------------

        binding.tvEditarPerfil.setOnClickListener {

            Toast.makeText(
                this,
                "Función de edición en desarrollo",
                Toast.LENGTH_SHORT
            ).show()
        }


        // ---------------------------------------------------------
        // VERIFICACIÓN DUI
        // ---------------------------------------------------------

        binding.tvVerificarDui.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    VerificacionDuiActivity::class.java
                )
            )
        }


        // ---------------------------------------------------------
        // MI PORTAFOLIO
        // ---------------------------------------------------------
        // Aunque el listener existe, el botón solamente será
        // visible para usuarios PROFESIONAL.

        binding.btnMiPortafolio.setOnClickListener {
            mostrarOpcionesPortafolio()
        }


        // ---------------------------------------------------------
        // SOPORTE
        // ---------------------------------------------------------

        binding.tvSoporte.setOnClickListener {

            Toast.makeText(
                this,
                "Contacto de soporte: soporte@chivotrabajo.sv",
                Toast.LENGTH_LONG
            ).show()
        }


        // ---------------------------------------------------------
        // TÉRMINOS
        // ---------------------------------------------------------

        binding.tvTerminos.setOnClickListener {

            Toast.makeText(
                this,
                "Términos y condiciones de ChivoTrabajo El Salvador",
                Toast.LENGTH_SHORT
            ).show()
        }


        // ---------------------------------------------------------
        // CERRAR SESIÓN
        // ---------------------------------------------------------

        binding.btnCerrarSesion.setOnClickListener {

            authRepository.cerrarSesion()

            Toast.makeText(
                this,
                "Sesión cerrada correctamente",
                Toast.LENGTH_SHORT
            ).show()

            val intent =
                Intent(
                    this,
                    IniciarSesionActivity::class.java
                ).apply {

                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

            startActivity(intent)

            finish()
        }
    }


    // =============================================================
    // OPCIONES DEL PORTAFOLIO
    // =============================================================

    private fun mostrarOpcionesPortafolio() {

        val opciones = arrayOf(
            "Ver portafolio",
            "Agregar trabajo al portafolio"
        )

        // Material 3: el diálogo hereda colores y formas del tema activo.
        MaterialAlertDialogBuilder(this)
            .setTitle("Gestión de Portafolio")
            .setItems(opciones) { _, which ->

                when (which) {

                    0 -> {

                        // Ver Portafolio existente
                        startActivity(
                            Intent(
                                this,
                                PortafolioProfesionalActivity::class.java
                            )
                        )
                    }

                    1 -> {

                        // Crear o agregar trabajo al portafolio
                        startActivity(
                            Intent(
                                this,
                                CrearPortafolioActivity::class.java
                            )
                        )
                    }
                }
            }
            .show()
    }


    // =============================================================
    // CARGAR DATOS DEL USUARIO
    // =============================================================

    private fun cargarDatosUsuario() {

        val currentUser = authRepository.usuarioActual()

        if (currentUser != null) {

            // Información inicial desde Firebase Authentication
            binding.tvCorreoUsuario.text =
                currentUser.email ?: "Sin correo"

            binding.tvNombreUsuario.text =
                currentUser.displayName ?: "Usuario ChivoTrabajo"


            // Foto proveniente de Firebase Authentication
            if (currentUser.photoUrl != null) {

                Glide.with(this)
                    .load(currentUser.photoUrl)
                    .circleCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.ivFotoPerfil)
            }


            // -----------------------------------------------------
            // OBTENER INFORMACIÓN COMPLETA DESDE FIRESTORE
            // -----------------------------------------------------

            CoroutineScope(Dispatchers.IO).launch {

                val resultado =
                    firestoreRepository.obtenerPerfilUsuario(
                        currentUser.uid
                    )

                withContext(Dispatchers.Main) {

                    resultado.onSuccess { usuario ->

                        if (usuario != null) {

                            // -------------------------------------
                            // NOMBRE COMPLETO
                            // -------------------------------------

                            val nombreCompleto =
                                "${usuario.nombre} ${usuario.apellido}".trim()

                            if (nombreCompleto.isNotBlank()) {

                                binding.tvNombreUsuario.text =
                                    nombreCompleto
                            }


                            // -------------------------------------
                            // CORREO
                            // -------------------------------------

                            binding.tvCorreoUsuario.text =
                                usuario.correo.ifBlank {
                                    currentUser.email ?: ""
                                }


                            // -------------------------------------
                            // TELÉFONO
                            // -------------------------------------

                            binding.tvTelefonoUsuario.text =
                                usuario.telefono.ifBlank {
                                    "No registrado"
                                }


                            // -------------------------------------
                            // TIPO DE USUARIO
                            // -------------------------------------

                            binding.tvTipoUsuario.text =
                                usuario.tipoUsuario


                            // -------------------------------------
                            // ESTADO DE VERIFICACIÓN
                            // -------------------------------------

                            binding.tvEstadoVerificacion.text =
                                usuario.estadoVerificacion


                            // =====================================
                            // CONTROL DEL PORTAFOLIO
                            // =====================================
                            //
                            // PROFESIONAL -> Mostrar
                            // CLIENTE     -> Ocultar
                            //

                            if (
                                usuario.tipoUsuario.equals(
                                    "PROFESIONAL",
                                    ignoreCase = true
                                )
                            ) {

                                binding.btnMiPortafolio.visibility =
                                    View.VISIBLE

                            } else {

                                binding.btnMiPortafolio.visibility =
                                    View.GONE
                            }


                            // -------------------------------------
                            // FOTO DE PERFIL
                            // -------------------------------------

                            if (usuario.fotoPerfil.isNotBlank()) {

                                Glide.with(
                                    this@ConfiguracionPerfilActivity
                                )
                                    .load(usuario.fotoPerfil)
                                    .circleCrop()
                                    .placeholder(R.mipmap.ic_launcher)
                                    .into(binding.ivFotoPerfil)
                            }
                        }
                    }

                    resultado.onFailure { error ->

                        // Si no se pueden obtener los datos,
                        // mantenemos Portafolio oculto por seguridad.
                        binding.btnMiPortafolio.visibility =
                            View.GONE

                        Toast.makeText(
                            this@ConfiguracionPerfilActivity,
                            "Error al cargar perfil: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        } else {

            // No hay usuario autenticado
            binding.tvNombreUsuario.text = "Invitado"
            binding.tvCorreoUsuario.text = "No autenticado"

            // Un usuario no autenticado tampoco debe ver Portafolio
            binding.btnMiPortafolio.visibility = View.GONE
        }
    }


    // =============================================================
    // SUBIR IMAGEN A CLOUDINARY
    // =============================================================

    private fun subirImagenACloudinary(imageUri: Uri) {

        val currentUser = authRepository.usuarioActual()

        if (currentUser == null) {

            Toast.makeText(
                this,
                "Debe iniciar sesión para cambiar la foto",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        Toast.makeText(
            this,
            "Subiendo foto a Cloudinary...",
            Toast.LENGTH_SHORT
        ).show()


        CoroutineScope(Dispatchers.IO).launch {

            var uploadSuccess = false
            var secureUrl: String? = null

            val cloudName =
                BuildConfig.CLOUDINARY_CLOUD_NAME

            val presetConfig =
                BuildConfig.CLOUDINARY_UPLOAD_PRESET

            val presets = listOf(
                presetConfig,
                "chivo_trabajo",
                "chivo_trabajo_preset",
                "ml_default",
                "preset_chivo"
            )


            for (preset in presets) {

                try {

                    val urlString =
                        "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

                    val url = URL(urlString)

                    val connection =
                        url.openConnection() as HttpURLConnection

                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.doInput = true


                    val boundary =
                        "Boundary-${System.currentTimeMillis()}"

                    connection.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=$boundary"
                    )


                    val outputStream =
                        DataOutputStream(
                            connection.outputStream
                        )


                    // -------------------------------------------------
                    // UPLOAD PRESET
                    // -------------------------------------------------

                    outputStream.writeBytes(
                        "--$boundary\r\n"
                    )

                    outputStream.writeBytes(
                        "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n"
                    )

                    outputStream.writeBytes(
                        "$preset\r\n"
                    )


                    // -------------------------------------------------
                    // ARCHIVO
                    // -------------------------------------------------

                    outputStream.writeBytes(
                        "--$boundary\r\n"
                    )

                    outputStream.writeBytes(
                        "Content-Disposition: form-data; name=\"file\"; filename=\"profile_${currentUser.uid}.jpg\"\r\n"
                    )

                    outputStream.writeBytes(
                        "Content-Type: image/jpeg\r\n\r\n"
                    )


                    contentResolver
                        .openInputStream(imageUri)
                        ?.use { inputStream ->

                            val buffer =
                                ByteArray(4096)

                            var bytesRead: Int

                            while (
                                inputStream.read(buffer)
                                    .also { bytesRead = it } != -1
                            ) {

                                outputStream.write(
                                    buffer,
                                    0,
                                    bytesRead
                                )
                            }
                        }


                    outputStream.writeBytes(
                        "\r\n--$boundary--\r\n"
                    )

                    outputStream.flush()
                    outputStream.close()


                    val responseCode =
                        connection.responseCode


                    if (
                        responseCode ==
                        HttpURLConnection.HTTP_OK
                    ) {

                        val responseString =
                            connection
                                .inputStream
                                .bufferedReader()
                                .use {
                                    it.readText()
                                }


                        val regex =
                            "\"secure_url\"\\s*:\\s*\"(.*?)\""
                                .toRegex()


                        val matchResult =
                            regex.find(responseString)


                        secureUrl =
                            matchResult
                                ?.groups
                                ?.get(1)
                                ?.value
                                ?.replace("\\/", "/")


                        if (!secureUrl.isNullOrBlank()) {

                            uploadSuccess = true
                            break
                        }
                    }

                } catch (e: Exception) {

                    // Si falla un preset,
                    // intenta con el siguiente.
                }
            }


            // ---------------------------------------------------------
            // RESULTADO
            // ---------------------------------------------------------

            withContext(Dispatchers.Main) {

                if (
                    uploadSuccess &&
                    !secureUrl.isNullOrBlank()
                ) {

                    guardarUrlEnFirestore(
                        currentUser.uid,
                        secureUrl
                    )

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


    // =============================================================
    // GUARDAR URL DE FOTO EN FIRESTORE
    // =============================================================

    private fun guardarUrlEnFirestore(
        uid: String,
        url: String
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            val res =
                firestoreRepository.actualizarFotoPerfil(
                    uid,
                    url
                )


            withContext(Dispatchers.Main) {

                res.onSuccess {

                    Toast.makeText(
                        this@ConfiguracionPerfilActivity,
                        "¡Foto de perfil actualizada con éxito!",
                        Toast.LENGTH_SHORT
                    ).show()


                    Glide.with(
                        this@ConfiguracionPerfilActivity
                    )
                        .load(url)
                        .circleCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .into(binding.ivFotoPerfil)
                }


                res.onFailure { e ->

                    Toast.makeText(
                        this@ConfiguracionPerfilActivity,
                        "Error al guardar en Firestore: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}