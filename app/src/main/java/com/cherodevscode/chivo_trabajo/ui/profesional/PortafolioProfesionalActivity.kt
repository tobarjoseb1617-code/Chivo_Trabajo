package com.cherodevscode.chivo_trabajo.ui.profesional

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.cherodevscode.chivo_trabajo.BuildConfig
import com.cherodevscode.chivo_trabajo.data.model.Portafolio
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityPortafolioProfesionalBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PortafolioProfesionalActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    private lateinit var binding: ActivityPortafolioProfesionalBinding
    private lateinit var portafolioAdapter: PortafolioAdapter

    private val listaPortafolio = mutableListOf<Portafolio>()

    private var imagenSeleccionada: Uri? = null


    // Seleccionar imagen de la galería
    private val seleccionarImagenLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {
                imagenSeleccionada = uri
                mostrarDialogoDescripcion()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPortafolioProfesionalBinding.inflate(layoutInflater)

        setContentView(binding.root)


        // -----------------------------------------
        // EVITAR QUE EL HEADER QUEDE DEBAJO
        // DE LA BARRA DE NOTIFICACIONES
        // -----------------------------------------

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            binding.layoutHeaderPortafolio.setPadding(
                16.dpToPx(),
                systemBars.top,
                16.dpToPx(),
                0
            )

            insets
        }


        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        configurarRecyclerView()
        configurarEventos()

        cargarPortafolio()
    }


    // ------------------------------------------------
    // CONVERTIR DP A PIXELES
    // ------------------------------------------------

    private fun Int.dpToPx(): Int {
        return (
                this * resources.displayMetrics.density
                ).toInt()
    }


    // ------------------------------------------------
    // RECYCLERVIEW
    // ------------------------------------------------

    private fun configurarRecyclerView() {

        portafolioAdapter =
            PortafolioAdapter(listaPortafolio) { trabajo ->

                mostrarOpcionesTrabajo(trabajo)
            }

        binding.rvPortafolio.apply {

            layoutManager =
                GridLayoutManager(
                    this@PortafolioProfesionalActivity,
                    2
                )

            adapter = portafolioAdapter

            setHasFixedSize(false)
        }
    }


    // ------------------------------------------------
    // BOTONES
    // ------------------------------------------------

    private fun configurarEventos() {

        binding.btnRegresarPortafolio.setOnClickListener {
            finish()
        }

        binding.btnAgregarTrabajo.setOnClickListener {
            abrirAgregarTrabajo()
        }

        binding.btnAgregarPrimerTrabajo.setOnClickListener {
            abrirAgregarTrabajo()
        }
    }


    // ------------------------------------------------
    // CARGAR PORTAFOLIO DESDE FIRESTORE
    // ------------------------------------------------

    private fun cargarPortafolio() {

        val currentUser =
            authRepository.usuarioActual()

        if (currentUser == null) {

            Toast.makeText(
                this,
                "No se encontró el usuario",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        CoroutineScope(Dispatchers.IO).launch {

            val resultado =
                firestoreRepository.obtenerPortafolio(
                    currentUser.uid
                )

            withContext(Dispatchers.Main) {

                resultado.onSuccess { trabajos ->

                    listaPortafolio.clear()
                    listaPortafolio.addAll(trabajos)

                    portafolioAdapter.actualizarLista(
                        listaPortafolio
                    )

                    actualizarEstadoPortafolio()

                }.onFailure { e ->

                    Toast.makeText(
                        this@PortafolioProfesionalActivity,
                        "Error al cargar portafolio: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    actualizarEstadoPortafolio()
                }
            }
        }
    }


    // ------------------------------------------------
    // ESTADO VACÍO / CANTIDAD DE TRABAJOS
    // ------------------------------------------------

    private fun actualizarEstadoPortafolio() {

        val cantidad =
            listaPortafolio.size

        binding.tvCantidadTrabajos.text =
            cantidad.toString()

        if (listaPortafolio.isEmpty()) {

            binding.layoutPortafolioVacio.visibility =
                View.VISIBLE

            binding.rvPortafolio.visibility =
                View.GONE

        } else {

            binding.layoutPortafolioVacio.visibility =
                View.GONE

            binding.rvPortafolio.visibility =
                View.VISIBLE
        }
    }


    // ------------------------------------------------
    // AGREGAR TRABAJO
    // ------------------------------------------------

    private fun abrirAgregarTrabajo() {

        seleccionarImagenLauncher.launch(
            "image/*"
        )
    }


    // ------------------------------------------------
    // DESCRIPCIÓN DEL NUEVO TRABAJO
    // ------------------------------------------------

    private fun mostrarDialogoDescripcion() {

        val inputDescripcion =
            EditText(this).apply {

                hint =
                    "Ejemplo: Instalación de tomacorrientes y reparación del sistema eléctrico"

                minLines = 3
                maxLines = 5

                gravity =
                    android.view.Gravity.TOP
            }


        val contenedor =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                val margen =
                    (20 * resources.displayMetrics.density)
                        .toInt()

                setPadding(
                    margen,
                    margen / 2,
                    margen,
                    0
                )

                addView(
                    inputDescripcion,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }


        val dialogo =
            AlertDialog.Builder(this)
                .setTitle("Agregar trabajo")
                .setMessage(
                    "Describe brevemente el trabajo realizado."
                )
                .setView(contenedor)
                .setPositiveButton(
                    "Publicar",
                    null
                )
                .setNegativeButton(
                    "Cancelar"
                ) { dialog, _ ->

                    imagenSeleccionada = null
                    dialog.dismiss()
                }
                .create()


        dialogo.setOnShowListener {

            dialogo
                .getButton(
                    AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {

                    val descripcion =
                        inputDescripcion
                            .text
                            .toString()
                            .trim()


                    if (descripcion.isEmpty()) {

                        inputDescripcion.error =
                            "Escribe una descripción del trabajo"

                        return@setOnClickListener
                    }


                    val imagen =
                        imagenSeleccionada


                    if (imagen == null) {

                        Toast.makeText(
                            this,
                            "No se seleccionó ninguna imagen",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }


                    dialogo.dismiss()

                    subirTrabajoACloudinary(
                        imagen,
                        descripcion
                    )
                }
        }


        dialogo.show()
    }


    // ------------------------------------------------
    // SUBIR IMAGEN A CLOUDINARY
    // ------------------------------------------------

    private fun subirTrabajoACloudinary(
        imageUri: Uri,
        descripcion: String
    ) {

        val currentUser =
            authRepository.usuarioActual()


        if (currentUser == null) {

            Toast.makeText(
                this,
                "Debe iniciar sesión para publicar un trabajo",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        Toast.makeText(
            this,
            "Publicando trabajo...",
            Toast.LENGTH_SHORT
        ).show()


        CoroutineScope(Dispatchers.IO).launch {

            var uploadSuccess = false

            var secureUrl: String? = null


            val cloudName =
                BuildConfig.CLOUDINARY_CLOUD_NAME


            val presetConfig =
                BuildConfig.CLOUDINARY_UPLOAD_PRESET


            val presets =
                listOf(
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


                    val url =
                        URL(urlString)


                    val connection =
                        url.openConnection()
                                as HttpURLConnection


                    connection.requestMethod =
                        "POST"

                    connection.doOutput =
                        true

                    connection.doInput =
                        true


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


                    // Upload preset
                    outputStream.writeBytes(
                        "--$boundary\r\n"
                    )

                    outputStream.writeBytes(
                        "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n"
                    )

                    outputStream.writeBytes(
                        "$preset\r\n"
                    )


                    // Imagen
                    outputStream.writeBytes(
                        "--$boundary\r\n"
                    )

                    outputStream.writeBytes(
                        "Content-Disposition: form-data; name=\"file\"; filename=\"trabajo_${System.currentTimeMillis()}.jpg\"\r\n"
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
                                inputStream
                                    .read(buffer)
                                    .also {
                                        bytesRead = it
                                    } != -1
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
                            regex.find(
                                responseString
                            )


                        secureUrl =
                            matchResult
                                ?.groups
                                ?.get(1)
                                ?.value
                                ?.replace(
                                    "\\/",
                                    "/"
                                )


                        if (
                            !secureUrl.isNullOrBlank()
                        ) {

                            uploadSuccess =
                                true

                            break
                        }
                    }

                } catch (e: Exception) {

                    // Si falla un preset,
                    // intenta con el siguiente
                }
            }


            withContext(
                Dispatchers.Main
            ) {

                if (
                    uploadSuccess &&
                    !secureUrl.isNullOrBlank()
                ) {

                    guardarTrabajoEnFirestore(
                        currentUser.uid,
                        secureUrl,
                        descripcion
                    )

                } else {

                    Toast.makeText(
                        this@PortafolioProfesionalActivity,
                        "Error al subir la imagen a Cloudinary",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // ------------------------------------------------
    // GUARDAR TRABAJO EN FIRESTORE
    // ------------------------------------------------

    private fun guardarTrabajoEnFirestore(
        uid: String,
        url: String,
        descripcion: String
    ) {

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            val resultado =
                firestoreRepository
                    .guardarTrabajoPortafolio(
                        uid,
                        url,
                        descripcion
                    )


            withContext(
                Dispatchers.Main
            ) {

                resultado.onSuccess {

                    Toast.makeText(
                        this@PortafolioProfesionalActivity,
                        "Trabajo publicado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()


                    imagenSeleccionada = null


                    // Actualizar lista
                    cargarPortafolio()


                }.onFailure { e ->

                    Toast.makeText(
                        this@PortafolioProfesionalActivity,
                        "Error al guardar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // ------------------------------------------------
    // MENÚ DE LOS TRES PUNTOS
    // ------------------------------------------------

    private fun mostrarOpcionesTrabajo(
        trabajo: Portafolio
    ) {

        val opciones =
            arrayOf(
                "Editar descripción",
                "Eliminar trabajo"
            )


        AlertDialog.Builder(this)
            .setTitle(
                "Opciones del trabajo"
            )
            .setItems(
                opciones
            ) { _, opcion ->

                when (opcion) {

                    0 -> {
                        editarDescripcionTrabajo(
                            trabajo
                        )
                    }

                    1 -> {
                        confirmarEliminarTrabajo(
                            trabajo
                        )
                    }
                }
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }


    // ------------------------------------------------
    // EDITAR DESCRIPCIÓN
    // ------------------------------------------------

    private fun editarDescripcionTrabajo(
        trabajo: Portafolio
    ) {

        val inputDescripcion =
            EditText(this).apply {

                setText(
                    trabajo.descripcion
                )

                setSelection(
                    text.length
                )

                minLines = 3
                maxLines = 5

                gravity =
                    android.view.Gravity.TOP
            }


        val contenedor =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL


                val margen =
                    (20 * resources.displayMetrics.density)
                        .toInt()


                setPadding(
                    margen,
                    margen / 2,
                    margen,
                    0
                )


                addView(
                    inputDescripcion,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }


        val dialogo =
            AlertDialog.Builder(this)
                .setTitle(
                    "Editar descripción"
                )
                .setView(
                    contenedor
                )
                .setPositiveButton(
                    "Guardar",
                    null
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .create()


        dialogo.setOnShowListener {

            dialogo
                .getButton(
                    AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {


                    val nuevaDescripcion =
                        inputDescripcion
                            .text
                            .toString()
                            .trim()


                    if (
                        nuevaDescripcion.isEmpty()
                    ) {

                        inputDescripcion.error =
                            "La descripción no puede estar vacía"

                        return@setOnClickListener
                    }


                    val currentUser =
                        authRepository.usuarioActual()


                    if (
                        currentUser == null
                    ) {

                        return@setOnClickListener
                    }


                    CoroutineScope(
                        Dispatchers.IO
                    ).launch {


                        val resultado =
                            firestoreRepository
                                .actualizarDescripcionPortafolio(
                                    currentUser.uid,
                                    trabajo.idFoto,
                                    nuevaDescripcion
                                )


                        withContext(
                            Dispatchers.Main
                        ) {


                            resultado.onSuccess {


                                dialogo.dismiss()


                                Toast.makeText(
                                    this@PortafolioProfesionalActivity,
                                    "Descripción actualizada",
                                    Toast.LENGTH_SHORT
                                ).show()


                                cargarPortafolio()


                            }.onFailure { e ->


                                Toast.makeText(
                                    this@PortafolioProfesionalActivity,
                                    "Error al actualizar: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }


        dialogo.show()
    }


    // ------------------------------------------------
    // CONFIRMAR ELIMINACIÓN
    // ------------------------------------------------

    private fun confirmarEliminarTrabajo(
        trabajo: Portafolio
    ) {

        AlertDialog.Builder(this)
            .setTitle(
                "Eliminar trabajo"
            )
            .setMessage(
                "¿Estás seguro de que deseas eliminar este trabajo de tu portafolio?"
            )
            .setPositiveButton(
                "Eliminar"
            ) { _, _ ->

                eliminarTrabajo(
                    trabajo
                )
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }


    // ------------------------------------------------
    // ELIMINAR TRABAJO
    // ------------------------------------------------

    private fun eliminarTrabajo(
        trabajo: Portafolio
    ) {

        val currentUser =
            authRepository.usuarioActual()


        if (
            currentUser == null
        ) {

            Toast.makeText(
                this,
                "No se encontró el usuario",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        CoroutineScope(
            Dispatchers.IO
        ).launch {


            val resultado =
                firestoreRepository
                    .eliminarTrabajoPortafolio(
                        currentUser.uid,
                        trabajo.idFoto
                    )


            withContext(
                Dispatchers.Main
            ) {


                resultado.onSuccess {


                    Toast.makeText(
                        this@PortafolioProfesionalActivity,
                        "Trabajo eliminado",
                        Toast.LENGTH_SHORT
                    ).show()


                    cargarPortafolio()


                }.onFailure { e ->


                    Toast.makeText(
                        this@PortafolioProfesionalActivity,
                        "Error al eliminar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}