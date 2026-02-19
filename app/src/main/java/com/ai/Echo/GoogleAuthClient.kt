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
import com.google.firebase.messaging.FirebaseMessaging
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
            .requestIdToken("29905288838-r78elj83hglalullips44dsuq9p8ton8.apps.googleusercontent.com")
            .build()
        GoogleSignIn.getClient(activity, gso)
    }

    private val launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
                    // 🔥 LOG THE ID AGAIN ON LOGIN SUCCESS
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        Log.d("FCM_ID_LOG", "REGISTRATION_TOKEN: $token")
                    }
                    onResult(true)
                }
            } catch (e: Exception) { onResult(false) }
        } else { onResult(false) }
    }

    fun signIn() {
        // 🔥 FORCE LOG TOKEN ON SIGN IN CLICK
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM_ID_LOG", "REGISTRATION_TOKEN: $token")
        }
        launcher.launch(googleSignInClient.signInIntent)
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }
}