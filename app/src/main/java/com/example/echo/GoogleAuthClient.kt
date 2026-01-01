package com.ai.Echo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient(
    private val activity: Activity
) {

    private val credentialManager = CredentialManager.create(activity)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Android 7–13
    private val firebaseWebClientId =
        "29905288838-cot6m28nklmq9833s2vb1s15j3q2b40o.apps.googleusercontent.com"

    // Android 14–16
    private val googleCloudWebClientId =
        "575958903864-ag8b7md1ohrfpmmbgfn2kj6spca0bk7r.apps.googleusercontent.com"

    private val firebaseProjectId = "ai-echo-12345"

    fun isSignedIn(): Boolean =
        firebaseAuth.currentUser != null

    // 🔥 MAIN ENTRY
    suspend fun signIn(): Boolean {
        if (isSignedIn()) return true

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startWebLogin()
            false
        } else {
            appLogin()
        }
    }

    // =============================
    // ANDROID 7–13 → APP LOGIN
    // =============================
    private suspend fun appLogin(): Boolean {
        return try {
            val result = requestCredential()
            handleCredential(result)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            false
        }
    }

    private suspend fun requestCredential(): GetCredentialResponse =
        withContext(Dispatchers.Main) {

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(firebaseWebClientId)
                .setNonce(UUID.randomUUID().toString())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            credentialManager.getCredential(activity, request)
        }

    private suspend fun handleCredential(
        result: GetCredentialResponse
    ): Boolean {

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return try {
                val googleCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val firebaseCredential =
                    GoogleAuthProvider.getCredential(
                        googleCredential.idToken,
                        null
                    )

                firebaseAuth.signInWithCredential(firebaseCredential).await()
                true
            } catch (e: GoogleIdTokenParsingException) {
                false
            }
        }
        return false
    }

    // =============================
    // ANDROID 14–16 → WEB LOGIN
    // =============================
    private fun startWebLogin() {

        val redirectUri =
            "https://$firebaseProjectId.firebaseapp.com/__/auth/handler"

        val authUrl =
            "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=$googleCloudWebClientId" +
                    "&redirect_uri=$redirectUri" +
                    "&response_type=code" +
                    "&scope=openid%20email%20profile" +
                    "&prompt=select_account"

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(authUrl)
        )

        activity.startActivity(intent)
    }

    // =============================
    // SIGN OUT
    // =============================
    suspend fun signOut() {
        withContext(Dispatchers.Main) {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            firebaseAuth.signOut()
        }
    }
}
