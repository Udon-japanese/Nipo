package com.example.nipo.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.firestore

class AuthRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    val currentUser get() = auth.currentUser

    suspend fun signInWithGoogle(webClientId: String): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() = auth.signOut()

    suspend fun getDisplayName(): String {
        val uid = auth.currentUser?.uid ?: return "匿名"
        return try {
            val doc = Firebase.firestore.collection("users").document(uid).get().await()
            doc.getString("displayName") ?: "匿名"
        } catch (e: Exception) { "匿名" }
    }

    suspend fun updateDisplayName(name: String): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("未ログインです"))
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("displayName" to name)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}