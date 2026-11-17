package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityChatBinding
import com.cherodevscode.chivo_trabajo.ui.cliente.InicioActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val firestoreRepository = FirestoreRepository()
    private var isPro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isPro = intent.getBooleanExtra("EXTRA_IS_PRO", false)

        verificarRolUsuario()

        // Botón regresar
        binding.btnRegresarChat.setOnClickListener {
            manejarRegreso()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                manejarRegreso()
            }
        })

        // Botón enviar mensaje
        binding.btnEnviarMensaje.setOnClickListener {
            val mensaje = binding.etMensajeChat.text.toString().trim()
            if (mensaje.isNotEmpty()) {
                Toast.makeText(this, "Mensaje enviado: $mensaje", Toast.LENGTH_SHORT).show()
                binding.etMensajeChat.text.clear()
            } else {
                Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarRolUsuario() {
        val usuarioActual = FirebaseAuth.getInstance().currentUser ?: return
        lifecycleScope.launch {
            val resultado = firestoreRepository.obtenerPerfilUsuario(usuarioActual.uid)
            resultado.onSuccess { usuario ->
                if (usuario != null && usuario.tipoUsuario.equals("PROFESIONAL", ignoreCase = true)) {
                    isPro = true
                }
            }
        }
    }

    private fun manejarRegreso() {
        if (isTaskRoot) {
            val targetIntent = if (isPro) {
                Intent(this, InicioProfesionalActivity::class.java)
            } else {
                Intent(this, InicioActivity::class.java)
            }.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(targetIntent)
        }
        finish()
    }
}
