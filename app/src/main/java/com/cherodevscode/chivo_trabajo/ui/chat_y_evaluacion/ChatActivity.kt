package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.databinding.ActivityChatBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private var grabador: MediaRecorder? = null
    private var audio: File? = null
    private val seleccionar = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: SecurityException) { }
            val mime = contentResolver.getType(uri).orEmpty()
            if (listOf("image/", "video/", "audio/").any { mime.startsWith(it) }) viewModel.enviar(uri = uri, mime = mime)
            else aviso("Selecciona una foto, video o audio válido")
        }
    }
    private val permisoAudio = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permitido ->
        if (permitido) iniciarGrabacion() else aviso("Se necesita permiso de micrófono para grabar")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getStringExtra("EXTRA_CHAT_ID").isNullOrBlank() && intent.getStringExtra("EXTRA_CONTACTO_UID").isNullOrBlank()) {
            startActivity(android.content.Intent(this, HistorialChatsActivity::class.java)
                .putExtra("EXTRA_IS_PRO", intent.getBooleanExtra("EXTRA_IS_PRO", false)))
            finish()
            return
        }
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter = MensajesAdapter(FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
        binding.listaMensajes.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.listaMensajes.adapter = adapter
        binding.btnRegresarChat.setOnClickListener { finish() }
        binding.btnEnviarMensaje.setOnClickListener {
            val texto = binding.etMensajeChat.text.toString()
            if (texto.isNotBlank()) viewModel.enviar(texto) else aviso("Escribe un mensaje")
        }
        binding.btnAdjuntar.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("Adjuntar archivo")
                .setItems(arrayOf("Foto", "Video", "Audio")) { _, opcion ->
                    seleccionar.launch(arrayOf(arrayOf("image/*"), arrayOf("video/*"), arrayOf("audio/*"))[opcion])
                }.show()
        }
        binding.btnGrabar.setOnClickListener {
            if (grabador != null) detenerGrabacion(true)
            else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) iniciarGrabacion()
            else permisoAudio.launch(Manifest.permission.RECORD_AUDIO)
        }
        binding.btnReintentarAdjunto.setOnClickListener { viewModel.reintentarAdjunto() }
        viewModel.adjuntoPendiente.observe(this) { binding.btnReintentarAdjunto.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.nombre.observe(this) { binding.tvNombreContacto.text = it }
        viewModel.mensajes.observe(this) { mensajes ->
            val layout = binding.listaMensajes.layoutManager as LinearLayoutManager
            val alFinal = layout.findLastVisibleItemPosition() >= adapter.itemCount - 2
            adapter.submitList(mensajes) {
                if (alFinal && mensajes.isNotEmpty()) binding.listaMensajes.scrollToPosition(mensajes.lastIndex)
            }
            binding.tvChatVacio.visibility = if (mensajes.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.tvChatVacio.setOnClickListener {
            if (viewModel.listo.value != true) viewModel.abrir(intent.getStringExtra("EXTRA_CHAT_ID"), intent.getStringExtra("EXTRA_CONTACTO_UID"))
        }
        viewModel.error.observe(this) {
            if (it != null) {
                aviso(it)
                if (viewModel.listo.value != true) binding.tvChatVacio.text = "No se pudo abrir la conversación. Toca para reintentar."
                viewModel.error.value = null
            }
        }
        viewModel.textoEnviado.observe(this) { texto ->
            if (texto != null) {
                if (binding.etMensajeChat.text.toString() == texto) binding.etMensajeChat.text.clear()
                viewModel.textoEnviado.value = null
            }
        }
        viewModel.listo.observe(this) {
            if (it) binding.tvChatVacio.text = "Envía un mensaje para iniciar la conversación"
            actualizarControles()
        }
        viewModel.enviando.observe(this) {
            binding.progresoEnvio.visibility = if (it) View.VISIBLE else View.GONE
            actualizarControles()
        }
        viewModel.abrir(intent.getStringExtra("EXTRA_CHAT_ID"), intent.getStringExtra("EXTRA_CONTACTO_UID"))
    }

    private fun actualizarControles() {
        val habilitado = viewModel.listo.value == true && viewModel.enviando.value != true
        binding.btnEnviarMensaje.isEnabled = habilitado && grabador == null
        binding.btnAdjuntar.isEnabled = habilitado && grabador == null
        binding.btnGrabar.isEnabled = habilitado
    }

    @Suppress("DEPRECATION")
    private fun iniciarGrabacion() {
        try {
            audio = File.createTempFile("nota_", ".m4a", cacheDir)
            grabador = MediaRecorder()
            grabador!!.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audio!!.absolutePath)
                setMaxDuration(120000)
                setOnInfoListener { _, que, _ -> if (que == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) detenerGrabacion(true) }
                prepare()
                start()
            }
            binding.btnGrabar.text = "Enviar voz"
            binding.tvEstadoGrabacion.visibility = View.VISIBLE
            actualizarControles()
        } catch (_: Exception) {
            detenerGrabacion(false)
            aviso("No se pudo iniciar la grabación")
        }
    }

    private fun detenerGrabacion(enviar: Boolean) {
        val actual = grabador ?: return
        var valido = true
        try { actual.stop() } catch (_: RuntimeException) { valido = false }
        actual.release()
        grabador = null
        binding.btnGrabar.text = "Voz"
        binding.tvEstadoGrabacion.visibility = View.GONE
        val archivo = audio
        audio = null
        if (enviar && valido && archivo != null) {
            viewModel.enviar(uri = Uri.fromFile(archivo), mime = "audio/mp4")
            // Se conserva hasta finalizar el envío para sobrevivir a recreaciones de la Activity.
        } else { archivo?.delete() }
        actualizarControles()
    }

    override fun onStop() { detenerGrabacion(false); super.onStop() }
    private fun aviso(mensaje: String) { Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show() }
}
