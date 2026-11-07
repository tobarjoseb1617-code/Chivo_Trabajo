package com.cherodevscode.chivo_trabajo.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherodevscode.chivo_trabajo.R
import com.cherodevscode.chivo_trabajo.data.repository.AuthRepository
import com.cherodevscode.chivo_trabajo.databinding.ActivityPanelAdminBinding
import com.cherodevscode.chivo_trabajo.ui.autenticacion.IniciarSesionActivity
import com.google.firebase.firestore.FirebaseFirestore

class PanelAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelAdminBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPanelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load("https://res.cloudinary.com/djwvfjt7k/image/upload/v1790744838/o8ipleapa27fyd72nsll.png")
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.ivLogoAdmin)

        cargarEstadisticas()

        binding.btnCerrarSesionAdmin.setOnClickListener {
            AuthRepository(this).cerrarSesion()

            val intent = Intent(this, IniciarSesionActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    private fun cargarEstadisticas() {
        db.collection("Solicitudes")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val solicitudes = snapshot.documents

                    val total = solicitudes.size

                    val pendientes = solicitudes.count {
                        it.getString("estado") == "PENDIENTE"
                    }

                    val asignadas = solicitudes.count {
                        it.getString("estado") == "ASIGNADA"
                    }

                    val canceladas = solicitudes.count {
                        it.getString("estado").equals("CANCELADA", ignoreCase = true)
                    }

                    val completadas = 0

                    binding.tvTotalSolicitudesAdmin.text = total.toString()
                    binding.tvPendientesAdmin.text = pendientes.toString()
                    binding.tvAsignadasAdmin.text = asignadas.toString()
                    binding.tvCompletadasAdmin.text = completadas.toString()
                    binding.tvCanceladasAdmin.text = canceladas.toString()
                }
            }
    }
}
