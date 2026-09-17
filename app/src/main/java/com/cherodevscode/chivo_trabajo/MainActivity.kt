package com.cherodevscode.chivo_trabajo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cherodevscode.chivo_trabajo.databinding.ActivityMainBinding
import com.cherodevscode.chivo_trabajo.ui.MainViewModel
import com.cherodevscode.chivo_trabajo.ui.MainViewModelFactory
import com.cherodevscode.chivo_trabajo.ui.RegistroAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(applicationContext)
    }
    private val adapter = RegistroAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvRegistros.layoutManager = LinearLayoutManager(this)
        binding.rvRegistros.adapter = adapter

        binding.btnGoogle.setOnClickListener {
            viewModel.iniciarSesion()
        }

        binding.btnCargar.setOnClickListener {
            viewModel.cargarRegistros()
        }

        viewModel.usuario.observe(this) { usuario ->
            binding.tvUsuario.text = if (usuario == null) {
                "No hay sesion iniciada"
            } else {
                "Usuario: ${usuario.email}"
            }
        }

        viewModel.registros.observe(this) { lista ->
            adapter.actualizar(lista)
            binding.tvEstado.text = "Registros encontrados: ${lista.size}"
        }

        viewModel.mensaje.observe(this) { mensaje ->
            binding.tvEstado.text = mensaje
        }
    }
}