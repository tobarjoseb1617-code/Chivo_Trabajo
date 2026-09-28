package com.cherodevscode.chivo_trabajo.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cherodevscode.chivo_trabajo.databinding.ActivityInicioBinding
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.ChatActivity
import com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion.HistorialChatsActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.ConfiguracionPerfilActivity
import com.cherodevscode.chivo_trabajo.ui.perfil.PerfilProfesionalActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.GestionarServicioEnCursoActivity
import com.cherodevscode.chivo_trabajo.ui.profesional.HistorialDeServiciosActivity

class InicioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ir a Seguimiento en Vivo al tocar la tarjeta de servicio en curso
        binding.cardServicioEnCurso.setOnClickListener {
            startActivity(Intent(this, SeguimientoActivity::class.java).putExtra("EXTRA_PIN", "3821"))
        }

        // 2. Ir a Gestionar Servicio en Curso al tocar el botón de "Ver mapa"
        binding.btnVerSeguimiento.setOnClickListener {
            startActivity(Intent(this, GestionarServicioEnCursoActivity::class.java))
        }

        // 3. Profesionales cercanos: Ver perfil 1 y 2 -> PerfilProfesionalActivity
        binding.btnVerPerfilPro1.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }
        binding.btnVerPerfilPro2.setOnClickListener {
            startActivity(Intent(this, PerfilProfesionalActivity::class.java))
        }

        // 4. Profesionales cercanos: Solicitar 1 y 2 -> ChatActivity (Abrir chat)
        binding.btnSolicitarPro1.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        binding.btnSolicitarPro2.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // 5. Categorías populares (Fontanería, Electricidad, Limpieza, Pintura) -> RadarProfesionalesActivity
        binding.cardCatFontaneria.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.cardCatElectricidad.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.cardCatLimpieza.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.cardCatPintura.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }
        binding.btnVerTodasCategorias.setOnClickListener {
            startActivity(Intent(this, RadarProfesionalesActivity::class.java))
        }

        // 6. Menú inferior: Solicitudes
        binding.navSolicitudes.setOnClickListener {
            startActivity(Intent(this, HistorialSolicitudesClienteActivity::class.java))
        }

        // 7. Menú inferior: Servicios
        binding.navServicios.setOnClickListener {
            startActivity(Intent(this, HistorialDeServiciosActivity::class.java))
        }

        // 8. Menú inferior: Mensajes
        binding.navMensajes.setOnClickListener {
            startActivity(Intent(this, HistorialChatsActivity::class.java))
        }

        // 9. Menú inferior y botón superior: Perfil / Configuración
        binding.navPerfil.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }
        binding.btnPerfilTop.setOnClickListener {
            startActivity(Intent(this, ConfiguracionPerfilActivity::class.java))
        }
    }
}
