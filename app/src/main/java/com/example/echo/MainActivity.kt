package com.ai.Echo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.Color as SysColor

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var googleAuthClient: GoogleAuthClient

    // State to handle loading/errors across the activity
    private var isLoading = mutableStateOf(false)
    private var errorMessage = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------- SYSTEM UI (UNCHANGED) ----------
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = SysColor.TRANSPARENT
        window.navigationBarColor = SysColor.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // ---------- PREFS ----------
        prefs = getSharedPreferences("echo_prefs", MODE_PRIVATE)

        // ✅ AUTO-LOGIN LOGIC
        if (prefs.getBoolean("logged_in", false)) {
            navigateToAi()
            return
        }

        // ---------- GOOGLE AUTH (LOGIC FIX) ----------
        googleAuthClient = GoogleAuthClient(this) { success ->
            if (success) {
                prefs.edit().putBoolean("logged_in", true).apply()
                navigateToAi()
            } else {
                // Reset UI if login fails so user can try again
                isLoading.value = false
                errorMessage.value = "Sign in failed. Please try again."
            }
        }

        // ---------- UI ----------
        setContent {
            MaterialTheme {
                // Passing the activity-level states to the UI
                BlackLoginUI(
                    googleAuthClient = googleAuthClient,
                    loading = isLoading.value,
                    error = errorMessage.value,
                    onLoginClick = {
                        isLoading.value = true
                        errorMessage.value = ""
                        googleAuthClient.signIn()
                    }
                )
            }
        }
    }

    private fun navigateToAi() {
        val intent = Intent(this, Ai::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BlackLoginUI(
    googleAuthClient: GoogleAuthClient,
    loading: Boolean,
    error: String,
    onLoginClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bb),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // White Bottom Sheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 26.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(100))
                        .background(Color(0xFF2A2A2A))
                )

                Spacer(modifier = Modifier.height(22.dp))

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else {
                    // Google Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF1C1C1C))
                            .clickable { onLoginClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Google",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = Color.Red, // Made red for visibility
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "You agree to Bharath-App-studio-App",
                    fontSize = 12.sp,
                    color = Color(0xFF6E6E6E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}