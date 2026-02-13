package com.ai.Echo

import android.os.Build
import android.graphics.Color as SysColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class Echo : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = SysColor.TRANSPARENT
        window.navigationBarColor = SysColor.TRANSPARENT

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // Ensure navigation bar contrast is disabled to show background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContent {
            EchoTheme {
                ConnectorsScreen()
            }
        }
    }
}

@Composable
fun ConnectorsScreen() {
    val context = LocalContext.current
    val backgroundColor = Color(0xFFFBF8F6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- FIXED LINEAR HEADER STYLE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF1C1C1E),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { (context as? ComponentActivity)?.finish() }
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = "Connectors",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
        }

        // --- CONTENT LIST ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        "Add more connectors",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Expand your AI workspace with integrations",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            val connectors = listOf(
                ConnectorData("Gmail", "Intelligent email indexing & summaries", R.drawable.drive),
                ConnectorData("Calendar", "Smart scheduling & event extraction", R.drawable.coupon_2),
                ConnectorData("Contacts", "Neural network contact management", R.drawable.wavy_check),
                ConnectorData("Drive", "Deep file search & cloud processing", R.drawable.folder)
            )

            items(connectors) { connector ->
                M3ModernCard(connector)
            }
        }
    }
}

@Composable
fun M3ModernCard(data: ConnectorData) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 0.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Action */ }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ICON CONTAINER
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF4EFED)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(data.icon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = data.desc,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

data class ConnectorData(val title: String, val desc: String, val icon: Int)

@Composable
fun EchoTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}