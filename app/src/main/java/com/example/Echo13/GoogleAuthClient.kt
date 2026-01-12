package com.ai.Echo

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleAuthClient(
    private val activity: ComponentActivity,
    private val onResult: (Boolean) -> Unit
) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(
                // ✅ WEB CLIENT ID
                "29905288838-cot6m28nklmq9833s2vb1s15j3q2b40o.apps.googleusercontent.com"
            )
            .build()

        GoogleSignIn.getClient(activity, gso)
    }

    private val launcher =
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                val credential =
                    GoogleAuthProvider.getCredential(account.idToken, null)

                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {

                            val user = firebaseAuth.currentUser
                            val userName = user?.displayName ?: "Unknown User"
                            val userEmail = user?.email ?: "No Email"
                            val userUid = user?.uid ?: "No UID"

                            CoroutineScope(Dispatchers.Main).launch {
                                MailSender.sendLoginMail(
                                    userName = userName,
                                    userEmail = userEmail,
                                    userUid = userUid,
                                    provider = "Google",
                                    packageName = activity.packageName
                                )
                            }

                            onResult(true)
                        } else {
                            onResult(false)
                        }
                    }

            } catch (e: Exception) {
                onResult(false)
            }
        }

    fun signIn() {
        launcher.launch(googleSignInClient.signInIntent)
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }
}
