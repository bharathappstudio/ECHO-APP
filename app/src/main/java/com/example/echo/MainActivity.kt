package com.ai.Echo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var googleAuthClient: GoogleAuthClient

    private var isLoading = mutableStateOf(false)
    private var errorMessage = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        prefs = getSharedPreferences("echo_prefs", MODE_PRIVATE)

        if (prefs.getBoolean("logged_in", false)) {
            navigateToAi()
            return
        }

        googleAuthClient = GoogleAuthClient(this) { success ->
            if (success) {
                prefs.edit().putBoolean("logged_in", true).apply()
                navigateToAi()
            } else {
                isLoading.value = false
                errorMessage.value = "Sign in failed. Please try again."
            }
        }

        setContent {
            MaterialTheme {
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
    // --- 5 COLOR GOOGLE ANIMATION LOGIC ---
    val googleColors = listOf(
        Color(0xFF4285F4), // Google Blue
        Color(0xFFEA4335), // Google Red
        Color(0xFFFBBC05), // Google Yellow
        Color(0xFF34A853), // Google Green
        Color(0xFF1976D2)  // Deep Blue (5th color to complete the loop)
    )

    var colorIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(loading) {
        if (loading) {
            while (true) {
                // Delay reduced to 500ms for a more energetic "Google" feel
                delay(500)
                colorIndex = (colorIndex + 1) % googleColors.size
            }
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = googleColors[colorIndex],
        // tween set to 500ms so it is constantly fading into the next brand color
        animationSpec = tween(durationMillis = 500),
        label = "GoogleColorAnimation"
    )
    // -------------------------------

    Box(modifier = Modifier.fillMaxSize()) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 26.dp)
            ) {
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
                    Box(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Fixed: Now showing 5 colors
                        LoadingIndicator(color = animatedColor)
                    }
                } else {
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
                        color = Color.Red,
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