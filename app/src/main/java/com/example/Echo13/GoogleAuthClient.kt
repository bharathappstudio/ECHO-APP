package com.ai.Echo

import android.app.Activity
import android.util.Log
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
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .requestIdToken(
                // ✅ MUST be WEB CLIENT ID from Firebase
                "29905288838-cot6m28nklmq9833s2vb1s15j3q2b40o.apps.googleusercontent.com"
            )
            .build()

        GoogleSignIn.getClient(activity, gso)
    }

    private val launcher =
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode != Activity.RESULT_OK) {
                Log.e("LOGIN_TEST", "Google Sign-In canceled")
                onResult(false)
                return@registerForActivityResult
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                val credential = GoogleAuthProvider.getCredential(
                    account.idToken,
                    null
                )

                firebaseAuth
                    .signInWithCredential(credential)
                    .addOnSuccessListener {

                        // 🔥 THIS LOG CONFIRMS FIREBASE LOGIN SUCCESS
                        Log.d("LOGIN_TEST", "Firebase login success")

                        val user = firebaseAuth.currentUser

                        CoroutineScope(Dispatchers.Main).launch {
                            MailSender.sendLoginMail(
                                userName = user?.displayName ?: "Unknown User",
                                userEmail = user?.email ?: "No Email",
                                userUid = user?.uid ?: "No UID",
                                provider = "Google",
                                packageName = activity.packageName
                            )
                        }

                        onResult(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("LOGIN_TEST", "Firebase login failed", e)
                        onResult(false)
                    }

            } catch (e: ApiException) {
                Log.e("LOGIN_TEST", "Google sign-in failed", e)
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
