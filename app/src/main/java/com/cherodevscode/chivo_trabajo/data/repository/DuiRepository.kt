package com.cherodevscode.chivo_trabajo.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DuiRepository {
    private val storageRef = FirebaseStorage.getInstance().reference

    suspend fun subirImagenDui(uid: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "dui_${uid}_${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child("dui_images/$fileName")
            fileRef.putFile(imageUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
