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
import kotlinx.coroutines.launch
import android.graphics.Color as SysColor
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = SysColor.TRANSPARENT
        window.navigationBarColor = SysColor.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        prefs = getSharedPreferences("echo_prefs", MODE_PRIVATE)

        if (prefs.getBoolean("logged_in", false)) {
            startActivity(Intent(this, Ai::class.java))
            finish()
            return
        }

        googleAuthClient = GoogleAuthClient(this)

        setContent {
            MaterialTheme {
                BlackLoginUI(
                    googleAuthClient = googleAuthClient,
                    onLoginSuccess = {
                        prefs.edit().putBoolean("logged_in", true).apply()
                        startActivity(Intent(this, Ai::class.java))
                        finish()
                    }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleAuthClient.SIGN_IN_REQUEST_CODE) {
            ResultHolder.callback?.invoke(data)
            ResultHolder.callback = null
        }
    }
}

@Composable
fun BlackLoginUI(
    googleAuthClient: GoogleAuthClient,
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

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
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2A2A2A))
                )

                Spacer(modifier = Modifier.height(22.dp))

                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF1C1C1C))
                            .clickable {
                                loading = true
                                error = ""
                                scope.launch {
                                    val success = googleAuthClient.signIn()
                                    loading = false
                                    if (success) onLoginSuccess()
                                    else error = "Login cancelled"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = Color(0xFF393B39),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "You agree to our Terms & Privacy Policy",
                    fontSize = 12.sp,
                    color = Color(0xFF6E6E6E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
