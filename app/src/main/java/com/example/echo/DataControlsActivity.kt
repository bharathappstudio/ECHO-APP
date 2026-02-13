// ======================= DataControlsActivity.kt =======================
package com.ai.Echo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.firebase.database.FirebaseDatabase

class DataControlsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        setContent {
            MaterialTheme {
                DataControlsUI()
            }
        }
    }
}

@Composable
fun DataControlsUI() {

    val context = LocalContext.current
    var improveModel by remember { mutableStateOf(true) }

    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName ?: "Unknown User"
    val email = user?.email ?: ""

    val photoUrl = user?.photoUrl
        ?.toString()
        ?.replace("s96-c", "s4096-c")
        ?.replace("s400-c", "s4096-c")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
    ) {

        /* ───────── TOP BAR ───────── */

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { (context as ComponentActivity).finish() }
            )
            Spacer(Modifier.width(16.dp))
            Text("Data controls", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(28.dp))

        /* ───────── PROFILE ───────── */

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
                        .background(Color(0xFFF9FBE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.first().toString(),
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

        /* ───────── ECHO PROMO CARD ───────── */

        val transition = rememberInfiniteTransition(label = "bubbles")

        val up1 by transition.animateFloat(
            -120f, 120f,
            infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = ""
        )
        val up2 by transition.animateFloat(
            120f, -120f,
            infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = ""
        )
        val side1 by transition.animateFloat(
            -50f, 50f,
            infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = ""
        )
        val side2 by transition.animateFloat(
            50f, -50f,
            infiniteRepeatable(tween(10000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = ""
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                .background(Color(0xFFFFECB3))
                .clickable {
                    context.startActivity(Intent(context, EchoWeb::class.java))
                }
        ) {

            val bubbleColor = Color(0xFFFAFAF7).copy(alpha = 0.75f)

            Box(Modifier.size(22.dp).offset(30.dp + side1.dp, up1.dp).background(bubbleColor, CircleShape))
            Box(Modifier.size(18.dp).offset(70.dp, up2.dp).background(bubbleColor, CircleShape))
            Box(Modifier.size(14.dp).offset(120.dp + side2.dp, up1.dp + 30.dp).background(bubbleColor, CircleShape))
            Box(Modifier.size(26.dp).offset(160.dp, up2.dp + 50.dp).background(bubbleColor, CircleShape))

            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Get the best of Echo 🫐", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Higher limits, secure cloud storage, real-time database, and built-in AI.",
                    fontSize = 13.sp,
                    color = Color(0xCC4E4E4E)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ───────── DATA CONTROL ROWS ───────── */

        DataRow("Improve the model") {
            improveModel = !improveModel
        }

        DataRow("Shared links") {
            // TODO: open shared links screen
        }

        DataRow("Clear chat history", danger = true) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@DataRow
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(uid)
                .child("chats")
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "All chats deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

@Composable
fun DataRow(
    title: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = if (danger) Color(0xFFDC2626) else Color.Black
        )
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0x80171616)
        )
    }
}
