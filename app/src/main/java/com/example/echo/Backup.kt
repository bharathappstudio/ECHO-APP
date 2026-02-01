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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// --- STYLE PALETTE ---
val ScreenBg = Color(0xFFFFFBFA)
val CardPeach = Color(0xFFFDF2ED)
val TextDark = Color(0xFF423935)
val TextGray = Color(0xFF756B67)
val SectionTitle = Color(0xFF8D5B41)

class DataBackupScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ YOUR REQUESTED CONFIGURATION
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
                // Calling the UI Composable
                DataBackupUI(onBack = { finish() })
            }
        }
    }
}

@Composable
fun DataBackupUI(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            // navigationBarsPadding used here to prevent content hiding behind nav bar
            // since you are using FLAG_LAYOUT_NO_LIMITS
            .padding(horizontal = 16.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.mes),
                    contentDescription = null,
                    tint = TextDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Data & Backup",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // --- CLOUD BACKUP SECTION ---
        Text(
            text = "Cloud Backup",
            color = SectionTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        BackupRow(
            iconId = R.drawable.drive,
            title = "Google Drive Backup",
            subtitle = "Backup and restore your data to Google Drive",
            isTop = true,
            isBottom = true,
            isLocked = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- NEW BACKUP SECTION ---
        Text(
            text = "New Backup",
            color = SectionTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            BackupRow(
                iconId = R.drawable.coupon_2,
                title = "Create Local Backup",
                subtitle = "Save all your data to a backup file",
                isTop = true
            )
            BackupRow(
                iconId = R.drawable.wavy_check,
                title = "Restore Local Backup",
                subtitle = "Restore your data from a backup file"
            )
            BackupRow(
                iconId = R.drawable.folder,
                title = "Export to CSV",
                subtitle = "Export your data to a CSV file, Not intended for backup purposes",
                isLocked = true
            )
            BackupRow(
                iconId = R.drawable.trash,
                title = "Clear all data",
                subtitle = "Permanently delete all your data from the app. This action cannot be undone."
            )
            BackupRow(
                iconId = R.drawable.coupon_2,
                title = "History",
                subtitle = "",
                isBottom = true
            )
        }

        Spacer(modifier = Modifier.height(60.dp)) // Extra space for Navigation Bar
    }
}

@Composable
fun BackupRow(
    iconId: Int,
    title: String,
    subtitle: String,
    isTop: Boolean = false,
    isBottom: Boolean = false,
    isLocked: Boolean = false
) {
    val shape = when {
        isTop && isBottom -> RoundedCornerShape(16.dp)
        isTop -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        isBottom -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Action */ },
        color = CardPeach,
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.k2),
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = TextGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}