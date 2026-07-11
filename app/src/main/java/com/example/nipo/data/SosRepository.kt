package com.example.nipo.data

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SosRepository(private val context: Context) {
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    suspend fun createSos(post: SosPost, imageUri: Uri?): Result<Unit> = try {
        val photoUrl = imageUri?.let { ImageUploadUtil.uploadImage(storage, context, it, "sos_images") }
        val docRef = db.collection("sos_posts").document()
        val data = post.copy(id = docRef.id, photoUrl = photoUrl, createdAt = Timestamp.now())
        docRef.set(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateSos(sosId: String, post: SosPost, newImageUri: Uri?): Result<Unit> = try {
        val photoUrl = newImageUri?.let { ImageUploadUtil.uploadImage(storage, context, it, "sos_images") } ?: post.photoUrl
        db.collection("sos_posts").document(sosId)
            .set(post.copy(id = sosId, photoUrl = photoUrl))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteSos(sosId: String): Result<Unit> = try {
        db.collection("sos_posts").document(sosId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSos(sosId: String): SosPost? = try {
        db.collection("sos_posts").document(sosId).get().await().toObject(SosPost::class.java)
    } catch (_: Exception) {
        null
    }

    fun observeOpenSos() =
        db.collection("sos_posts")
            .whereEqualTo("status", SosStatus.OPEN.name)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SosPost::class.java)
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            }
            .catch { emit(emptyList()) }

    fun observeMySos(uid: String) =
        db.collection("sos_posts")
            .whereEqualTo("authorUid", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SosPost::class.java)
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            }
            .catch { emit(emptyList()) }

    fun observeSosMessages(sosId: String) =
        db.collection("sos_posts").document(sosId).collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(SosMessage::class.java) }
            .catch { emit(emptyList()) }

    suspend fun sendSosMessage(sosId: String, message: SosMessage): Result<Unit> = try {
        val docRef = db.collection("sos_posts").document(sosId).collection("messages").document()
        docRef.set(message.copy(id = docRef.id, sentAt = Timestamp.now())).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun closeSos(sosId: String): Result<Unit> = try {
        db.collection("sos_posts").document(sosId)
            .update(mapOf("status" to SosStatus.CLOSED.name, "closedAt" to Timestamp.now()))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun reopenSos(sosId: String): Result<Unit> = try {
        db.collection("sos_posts").document(sosId)
            .update(mapOf("status" to SosStatus.OPEN.name, "closedAt" to null))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
