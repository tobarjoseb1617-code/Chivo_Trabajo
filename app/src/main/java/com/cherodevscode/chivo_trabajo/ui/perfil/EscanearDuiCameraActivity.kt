package com.cherodevscode.chivo_trabajo.ui.perfil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cherodevscode.chivo_trabajo.databinding.ActivityEscanearDuiCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EscanearDuiCameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEscanearDuiCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var esFrente = true

    companion object {
        private const val REQUEST_CODE_CAMERA = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEscanearDuiCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        esFrente = intent.getBooleanExtra("EXTRA_ES_FRENTE", true)
        if (!esFrente) {
            binding.btnCapturar.text = "📸 Capturar Dorso de DUI"
        } else {
            binding.btnCapturar.text = "📸 Capturar Frente de DUI"
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_CAMERA
            )
        }

        binding.btnCapturar.setOnClickListener {
            takePhotoAndProcess()
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Error al iniciar cámara: ${exc.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndProcess() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            externalCacheDir,
            "dui_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "Error al capturar foto: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    if (esFrente) {
                        procesarOcr(savedUri)
                    } else {
                        // Dorso: no requiere OCR, solo retorna la imagen
                        val resultIntent = Intent().apply {
                            putExtra("EXTRA_IMAGE_URI", savedUri.toString())
                            putExtra("EXTRA_DUI_NUMERO", "")
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        )
    }

    private fun procesarOcr(imageUri: Uri) {
        Toast.makeText(this, "Escaneando DUI frontal con ML Kit...", Toast.LENGTH_SHORT).show()
        try {
            val image = InputImage.fromFilePath(this, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    // Regex para DUI salvadoreño: 8 dígitos guión 1 dígito verificador (ej: 01234567-8)
                    val regex = Regex("\\b\\d{8}-\\d\\b")
                    val match = regex.find(text)
                    val duiEncontrado = match?.value ?: ""

                    if (duiEncontrado.isNotBlank()) {
                        Toast.makeText(this, "¡DUI detectado: $duiEncontrado!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Foto frontal tomada. Ingrese el número de DUI si no se autodetectó.", Toast.LENGTH_LONG).show()
                    }

                    val intent = Intent().apply {
                        putExtra("EXTRA_IMAGE_URI", imageUri.toString())
                        putExtra("EXTRA_DUI_NUMERO", duiEncontrado)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Foto tomada con éxito", Toast.LENGTH_SHORT).show()
                    val intent = Intent().apply {
                        putExtra("EXTRA_IMAGE_URI", imageUri.toString())
                        putExtra("EXTRA_DUI_NUMERO", "")
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error procesando imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
