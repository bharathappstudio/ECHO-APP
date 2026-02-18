// ======================= PermissionsActivity.kt =======================
package com.ai.Echo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class PermissionsActivity : ComponentActivity() {

    // ---- STATE ----
    private var locationGranted by mutableStateOf(false)
    private var notificationGranted by mutableStateOf(false)

    // ---- LOCATION PERMISSION ----
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            locationGranted = granted
        }

    // ---- NOTIFICATION PERMISSION (Android 13+) ----
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // EDGE TO EDGE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        // Ensure navigation bar contrast is disabled to show background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // INITIAL PERMISSION STATE
        locationGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        notificationGranted = if (Build.VERSION.SDK_INT >= 33) {
            isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true // auto granted below Android 13
        }

        setContent {
            PermissionsUI(
                onBack = { finish() },
                locationEnabled = locationGranted,
                notificationEnabled = notificationGranted,
                onLocationToggle = { enabled ->
                    if (enabled) {
                        locationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } else {
                        locationGranted = false
                    }
                },
                onNotificationToggle = { enabled ->
                    if (Build.VERSION.SDK_INT >= 33 && enabled) {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
            )
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun PermissionsUI(
    onBack: () -> Unit,
    locationEnabled: Boolean,
    notificationEnabled: Boolean,
    onLocationToggle: (Boolean) -> Unit,
    onNotificationToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp)
    ) {

        // ---- HEADER ----
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Text(
                text = "Permissions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(32.dp))

        // ---- NOTIFICATIONS ----
        PermissionItem(
            title = "Notifications",
            description = "Allow Echo to send notifications and messages",
            checked = notificationEnabled,
            onCheckedChange = onNotificationToggle
        )

        Spacer(Modifier.height(28.dp))

        // ---- LOCATION ----
        PermissionItem(
            title = "Location access",
            description = "Allow Echo to give better local answers based on your location",
            checked = locationEnabled,
            onCheckedChange = onLocationToggle
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xCC000000)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFFFFE0B2),
                checkedThumbColor = Color.White,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFC8E6C9)
            )
        )
    }
}
