package com.cherodevscode.chivo_trabajo.ui.chat_y_evaluacion

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class VisorMultimediaActivity : AppCompatActivity() {
    private var reproductor: VideoView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("URL").orEmpty()
        if (!url.startsWith("https://res.cloudinary.com/")) { finish(); return }
        if (intent.getStringExtra("TIPO") == "imagen") {
            val imagen = ImageView(this).apply { adjustViewBounds = true; contentDescription = "Foto del chat" }
            setContentView(imagen)
            Glide.with(this).load(url).into(imagen)
        } else {
            val video = VideoView(this)
            reproductor = video
            setContentView(video)
            val controles = MediaController(this).apply { setAnchorView(video) }
            video.setMediaController(controles)
            video.setOnErrorListener { _, _, _ -> Toast.makeText(this, "No se pudo reproducir el archivo", Toast.LENGTH_LONG).show(); true }
            video.setVideoURI(Uri.parse(url))
            video.setOnPreparedListener { video.start(); controles.show(0) }
        }
    }
    override fun onStop() { reproductor?.pause(); super.onStop() }
    override fun onDestroy() { reproductor?.stopPlayback(); super.onDestroy() }
}
