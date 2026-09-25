package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón regresar
        binding.btnRegresarChat.setOnClickListener {
            finish()
        }

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
}
