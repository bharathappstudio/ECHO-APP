package com.ai.Echo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = Color(0xFFFFEA00),
                    surface = Color(0xFFFFC400),
                    primary = Color.Black
                )
            ) {
                SettingUI(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        prefs.edit().clear().apply()
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
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

        /* -------- TOP BAR -------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(28.dp))

        /* -------- PROFILE (GOOGLE DATA) -------- */
        Row(verticalAlignment = Alignment.CenterVertically) {

            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB3E5FC)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(email, fontSize = 14.sp, color = Color(0xFF6B6B6B))
            }
        }

        Spacer(Modifier.height(20.dp))

        /* -------- UPGRADE CARD -------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFC8E6C9),
                            Color(0xFFDDE6F8)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        "Get the best of Echo 🫐",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Higher limits, cloud storage, and Bharath apps with Echo built in Ai",
                        fontSize = 13.sp,
                        color = Color(0xFF4E4E4E)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {

                }
            }
        }

        Spacer(Modifier.height(24.dp))

        /* -------- SETTINGS LIST -------- */
        SettingRow("Echo App as your Assistant", true)
        SettingRow("Account")
        SettingRow("Connectors")
        SettingRow("Manage memory")
        SettingRow("User")
        SettingRow("Give feedback")
        SettingRow("Take the latest survey")
        SettingRow("About")

        Spacer(Modifier.height(20.dp))

        TextButton(onClick = onLogout) {
            Text("Sign out", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingRow(title: String, isNew: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )

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

        Icon(
            Icons.Default.Done,
            contentDescription = null,
            tint = Color(0xFF7A7A7A)
        )
    }
}
