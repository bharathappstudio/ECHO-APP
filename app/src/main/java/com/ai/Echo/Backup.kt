package com.ai.Echo

import android.os.Bundle
import android.view.WindowManager
import android.graphics.Color as SysColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// --- MODERN M3 REFINED PALETTE ---
val ScreenBg = Color(0xFFFFF8E1)
val CardPeach = Color(0x80FFECB3)
val AccentBrown = Color(0xFF8D5B41)
val TextDark = Color(0xCC2D2724)
val TextSub = Color(0xFF756B67)

class DataBackupScreen : ComponentActivity() {
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
            MaterialTheme {
                DataBackupUI()
            }
        }
    }
}

@Composable
fun DataBackupUI() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- YOUR REQUESTED LINEAR HEADER STYLE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { (context as? ComponentActivity)?.finish() }
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = "Data & Backup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
        }

        // --- SECTION: CLOUD ---
        SectionHeader("Cloud Backup")
        ModernBackupRow(
            iconId = R.drawable.drive,
            title = "Google Drive Backup",
            subtitle = "Secure cloud sync for your data",
            isTop = true,
            isBottom = true,
            isLocked = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION: MANAGEMENT ---
        SectionHeader("Management")
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ModernBackupRow(
                iconId = R.drawable.coupon_2,
                title = "Create Local Backup",
                subtitle = "Save snapshots to your device storage",
                isTop = true
            )
            ModernBackupRow(
                iconId = R.drawable.restore,
                title = "Restore Data",
                subtitle = "Recover from a local file"
            )
            ModernBackupRow(
                iconId = R.drawable.folder,
                title = "Export to CSV",
                subtitle = "View your data in Excel or Sheets",
                isLocked = true
            )
            ModernBackupRow(
                iconId = R.drawable.trash,
                title = "Clear All Data",
                subtitle = "Erase app database permanently",
                isDanger = true
            )
            ModernBackupRow(
                iconId = R.drawable.cloud,
                title = "Backup History",
                subtitle = "View recent logs",
                isBottom = true
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = AccentBrown,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
fun ModernBackupRow(
    iconId: Int,
    title: String,
    subtitle: String,
    isTop: Boolean = false,
    isBottom: Boolean = false,
    isLocked: Boolean = false,
    isDanger: Boolean = false
) {
    val shape = when {
        isTop && isBottom -> RoundedCornerShape(24.dp)
        isTop -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        isBottom -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        else -> RoundedCornerShape(4.dp)
    }

    val contentColor = if (isDanger) Color(0xFF2D2724) else TextDark

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardPeach,
        shape = shape,
        onClick = { /* Action */ }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDanger) Color.White.copy(alpha = 0.5f) else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.wavy_check),
                            contentDescription = null,
                            tint = AccentBrown,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = TextSub,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}