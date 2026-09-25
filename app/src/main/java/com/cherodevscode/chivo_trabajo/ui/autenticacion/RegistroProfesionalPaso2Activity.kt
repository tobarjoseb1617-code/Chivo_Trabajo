package com.cherodevscode.chivo_trabajo.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.data.repository.FirestoreRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityRegistroProfesionalPaso2Binding
import com.cherodevscode.chivo_trabajo.ui.profesional.InicioProfesionalActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroProfesionalPaso2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroProfesionalPaso2Binding
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroProfesionalPaso2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        firestoreRepository = FirestoreRepository()

        binding.btnCompletarRegistroPro.setOnClickListener {
            val especialidad = binding.etEspecialidad.text.toString().trim()
            if (especialidad.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese su especialidad principal", Toast.LENGTH_SHORT).show()
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
                            tipoUsuario = "PROFESIONAL",
                            ciudad = especialidad
                        )
                        val saveRes = withContext(Dispatchers.IO) {
                            firestoreRepository.guardarPerfilUsuario(usuarioActualizado)
                        }
                        saveRes.onSuccess {
                            Toast.makeText(this@RegistroProfesionalPaso2Activity, "¡Perfil profesional completado con éxito!", Toast.LENGTH_LONG).show()
                            irAInicioPro()
                        }.onFailure { e ->
                            Toast.makeText(this@RegistroProfesionalPaso2Activity, "Error al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@RegistroProfesionalPaso2Activity, "¡Registro completado!", Toast.LENGTH_LONG).show()
                        irAInicioPro()
                    }
                }.onFailure {
                    Toast.makeText(this@RegistroProfesionalPaso2Activity, "¡Registro completado!", Toast.LENGTH_LONG).show()
                    irAInicioPro()
                }
            }
        }
    }

    private fun irAInicioPro() {
        val intent = Intent(this, InicioProfesionalActivity::class.java)
        startActivity(intent)
        finish()
    }
}
