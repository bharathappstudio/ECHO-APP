package com.ai.Echo

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

class Setting : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        prefs = getSharedPreferences("echo_prefs", MODE_PRIVATE)

        setContent {
            MaterialTheme {
                SettingUI(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        prefs.edit().clear().apply()
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingUI(onLogout: () -> Unit) {

    val context = LocalContext.current
    fun openLink(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName ?: "Unknown User"
    val email = user?.email ?: ""
    val photoUrl = user?.photoUrl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB3E5FC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(name.first().toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(email, fontSize = 14.sp, color = Color(0xFF6B6B6B))
            }
        }

        Spacer(Modifier.height(20.dp))

        /* -------- CAD LIGHT GREEN + ORANGE BUBBLES -------- */

        val transition = rememberInfiniteTransition(label = "bubbles")

        val up1 by transition.animateFloat(
            initialValue = -120f,
            targetValue = 120f,
            animationSpec = infiniteRepeatable(
                tween(7000, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = ""
        )

        val up2 by transition.animateFloat(
            initialValue = 120f,
            targetValue = -120f,
            animationSpec = infiniteRepeatable(
                tween(9000, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = ""
        )

        val side1 by transition.animateFloat(
            initialValue = -50f,
            targetValue = 50f,
            animationSpec = infiniteRepeatable(
                tween(8000, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = ""
        )

        val side2 by transition.animateFloat(
            initialValue = 50f,
            targetValue = -50f,
            animationSpec = infiniteRepeatable(
                tween(10000, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = ""
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFC8E6C9)) // light green CAD
        ) {

            val bubbleColor = Color(0xFF81C784).copy(alpha = 0.8f) // ORANGE bubbles

            Box(
                Modifier
                    .size(22.dp)
                    .offset(x = 30.dp + side1.dp, y = up1.dp)
                    .background(bubbleColor, CircleShape)
            )
            Box(
                Modifier
                    .size(18.dp)
                    .offset(x = 70.dp, y = up2.dp)
                    .background(bubbleColor, CircleShape)
            )
            Box(
                Modifier
                    .size(14.dp)
                    .offset(x = 120.dp + side2.dp, y = up1.dp + 30.dp)
                    .background(bubbleColor, CircleShape)
            )
            Box(
                Modifier
                    .size(26.dp)
                    .offset(x = 160.dp, y = up2.dp + 50.dp)
                    .background(bubbleColor, CircleShape)
            )
            Box(
                Modifier
                    .size(12.dp)
                    .offset(x = 200.dp + side1.dp, y = up1.dp + 70.dp)
                    .background(bubbleColor, CircleShape)
            )
            Box(
                Modifier
                    .size(20.dp)
                    .offset(x = 240.dp, y = up2.dp + 20.dp)
                    .background(bubbleColor, CircleShape)
            )
            Box(
                Modifier
                    .size(16.dp)
                    .offset(x = 280.dp + side2.dp, y = up1.dp + 40.dp)
                    .background(bubbleColor, CircleShape)
            )
            Box(
                Modifier
                    .size(10.dp)
                    .offset(x = 320.dp, y = up2.dp + 80.dp)
                    .background(bubbleColor, CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Get the best of Echo 🫐", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Higher limits, cloud storage, and Bharath apps with Echo built in Ai",
                    fontSize = 13.sp,
                    color = Color(0xFF4E4E4E)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        SettingRow("Echo App as your Assistant", true) { openLink("https://example.com/assistant") }
        SettingRow("Account") { openLink("https://example.com/account") }
        SettingRow("Connectors") { openLink("https://example.com/connectors") }
        SettingRow("Manage memory") { openLink("https://example.com/memory") }
        SettingRow("User") { openLink("https://example.com/user") }
        SettingRow("Give feedback") { openLink("mailto:jarvisvbharath11@gmail.com") }
        SettingRow("Call to Developer") { openLink("tel:+917094589909") }
        SettingRow("About") { openLink("https://gitlab.com/jarvisvbharath11") }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onLogout) { Text("Sign out") }
        }
    }
}

@Composable
fun SettingRow(title: String, isNew: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        if (isNew) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3D7BD))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("NEW", fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.Default.Done, contentDescription = null, tint = Color(0xFFFFCCBC))
    }
}
