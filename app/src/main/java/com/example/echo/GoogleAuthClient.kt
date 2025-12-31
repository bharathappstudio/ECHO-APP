package com.ai.Echo

import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient(
    private val activity: ComponentActivity
) {

    private val credentialManager = CredentialManager.create(activity)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isSignedIn(): Boolean =
        firebaseAuth.currentUser != null

    suspend fun signIn(): Boolean {
        if (isSignedIn()) return true

        return try {
            val result = buildCredentialRequest()
            handleSignIn(result)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            false
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return try {
                val tokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val authCredential = GoogleAuthProvider.getCredential(
                    tokenCredential.idToken,
                    null
                )

                firebaseAuth.signInWithCredential(authCredential).await()
                true
            } catch (e: GoogleIdTokenParsingException) {
                false
            }
        }
        return false
    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse =
        withContext(Dispatchers.Main) {

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .setServerClientId(
                            "29905288838-cot6m28nklmq9833s2vb1s15j3q2b40o.apps.googleusercontent.com"
                        )
                        .build()
                )
                .build()

            // ✅ CORRECT API CALL
            credentialManager.getCredential(
                context = activity,
                request = request
            )
        }

    suspend fun signOut() {
        withContext(Dispatchers.Main) {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            firebaseAuth.signOut()
        }
    }
}
