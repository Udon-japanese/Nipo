package com.example.nipo.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object ImageUploadUtil {

    suspend fun uploadImage(storage: FirebaseStorage, context: Context, uri: Uri, folder: String): String {
        val compressed = compressImage(context, uri)
        val ref = storage.reference.child("$folder/${UUID.randomUUID()}.jpg")
        ref.putBytes(compressed).await()
        return ref.downloadUrl.await().toString()
    }

    fun compressImage(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // 長辺を1280pxに縮小（表示用途としては十分な解像度）
        val maxDimension = 1280
        val ratio = minOf(
            maxDimension.toFloat() / original.width,
            maxDimension.toFloat() / original.height,
            1f // 元が小さい場合は拡大しない
        )
        val resized = if (ratio < 1f) {
            original.scale((original.width * ratio).toInt(), (original.height * ratio).toInt())
        } else original

        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // 品質80%
        return outputStream.toByteArray()
    }
}
