package com.ai.Echo

import android.graphics.Color as SysColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectorsScreen() {
    Scaffold(
        containerColor = Color(0xFFFFF8E1),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Connectors",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.mes),
                            contentDescription = "Back",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFF8E1)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            Text(
                text = "Add more connectors",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xCC1C1C1E),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            val connectors = listOf(
                ConnectorData(
                    "Gmail",
                    "Search, read, and analyze messages in your Gmail inbox.",
                    R.drawable.bg
                ),
                ConnectorData(
                    "Google Calendar",
                    "Search and read events in your Google Calendar.",
                    R.drawable.bg
                ),
                ConnectorData(
                    "Google Contacts",
                    "Access your Google contacts.",
                    R.drawable.bg
                ),
                ConnectorData(
                    "Google Drive",
                    "Search files in your Google Drive.",
                    R.drawable.bg
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(connectors) {
                    ConnectorItem(it)
                }
            }
        }
    }
}

@Composable
fun ConnectorItem(data: ConnectorData) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(data.icon),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = data.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0x80F3F3F3),
                    onClick = { }
                ) {
                    Text(
                        text = "Connect",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data.desc,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.weight(1f),
                    lineHeight = 20.sp
                )

            }
        }
    }
}

data class ConnectorData(
    val title: String,
    val desc: String,
    val icon: Int
)

@Composable
fun EchoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Color(0x80FBF8F5),
            surface = Color.White,
            onSurface = Color(0xFF1C1C1E)
        ),
        typography = Typography(
            displayLarge = TextStyle(fontFamily = FontFamily.SansSerif),
            displayMedium = TextStyle(fontFamily = FontFamily.SansSerif),
            displaySmall = TextStyle(fontFamily = FontFamily.SansSerif),
            headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif),
            headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif),
            headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif),
            titleLarge = TextStyle(fontFamily = FontFamily.SansSerif),
            titleMedium = TextStyle(fontFamily = FontFamily.SansSerif),
            titleSmall = TextStyle(fontFamily = FontFamily.SansSerif),
            bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif),
            bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif),
            bodySmall = TextStyle(fontFamily = FontFamily.SansSerif),
            labelLarge = TextStyle(fontFamily = FontFamily.SansSerif),
            labelMedium = TextStyle(fontFamily = FontFamily.SansSerif),
            labelSmall = TextStyle(fontFamily = FontFamily.SansSerif)
        ),
        content = content
    )
}
