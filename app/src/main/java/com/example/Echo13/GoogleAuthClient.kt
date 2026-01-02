package com.ai.Echo

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GoogleAuthClient(private val activity: Activity) {

    private val googleSignInClient: GoogleSignInClient
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile() // ✅ IMPORTANT
            .requestIdToken(
                "29905288838-cot6m28nklmq9833s2vb1s15j3q2b40o.apps.googleusercontent.com"
            )
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    suspend fun signIn(): Boolean = suspendCancellableCoroutine { cont ->
        val intent: Intent = googleSignInClient.signInIntent
        activity.startActivityForResult(intent, SIGN_IN_REQUEST_CODE)

        ResultHolder.callback = { data ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount =
                    task.getResult(ApiException::class.java)

                val credential = GoogleAuthProvider.getCredential(
                    account.idToken,
                    null
                )

                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {

                            val user = firebaseAuth.currentUser

                            val userName = user?.displayName ?: "Unknown User"
                            val userEmail = user?.email ?: "No Email"
                            val userUid = user?.uid ?: "No UID"

                            // ✅ HIGH QUALITY GOOGLE PROFILE IMAGE (512x512)
                            account.photoUrl
                                ?.toString()
                                ?.replace("s96-c", "s512-c")

                            // OPTIONAL: use this URL in UI / save to Firestore
                            // highQualityPhoto

                            CoroutineScope(Dispatchers.Main).launch {
                                MailSender.sendLoginMail(
                                    userName = userName,
                                    userEmail = userEmail,
                                    userUid = userUid,
                                    provider = "Google",
                                    packageName = activity.packageName
                                )
                            }

                            cont.resume(true)
                        } else {
                            cont.resume(false)
                        }
                    }

            } catch (e: Exception) {
                cont.resume(false)
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }

    companion object {
        const val SIGN_IN_REQUEST_CODE = 9001
    }
}
