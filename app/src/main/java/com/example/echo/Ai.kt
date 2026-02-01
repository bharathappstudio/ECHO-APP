// ===================== PACKAGE NAME =====================
package com.ai.Echo

// ===================== ANDROID IMPORTS =====================
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.graphics.Color as SysColor

// ===================== ACTIVITY + COMPOSE =====================
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

// ===================== COMPOSE UI =====================
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// ===================== 🔧 SAFE EXPLICIT TEXT IMPORTS (FIX) =====================
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

// ===================== IMAGE LOADING =====================
import coil.compose.AsyncImage

// ===================== AI (GEMINI) =====================
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

// ===================== FIREBASE =====================
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.IgnoreExtraProperties

// ===================== COROUTINES =====================
import kotlinx.coroutines.launch

// ===================== CORE =====================
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// ===================== LAYOUT =====================
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn

// ===================== FOUNDATION =====================
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// ===================== ALIGNMENT =====================
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// ===================== MATERIAL 3 =====================
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults

// ===================== UNITS =====================
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
// ===================== ADDED FOR iOS ANIMATION =====================
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.FastOutSlowInEasing
// ===================== ======================== =====================

// ======================================================
// INTERNET CHECK FUNCTION
// ======================================================
fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        @Suppress("DEPRECATION")
        cm.activeNetworkInfo?.isConnected == true
    }
}

// ======================================================
// CHAT MESSAGE DATA MODEL (FIREBASE)
// ======================================================
@IgnoreExtraProperties
data class ChatMessage(
    var id: String = "",
    var text: String = "",
    var isUser: Boolean = false,
    var imageUri: String? = null,
    var timestamp: Long = 0L
)

// ======================================================
// MARKDOWN PARSER (**bold**)
// ======================================================
@Composable
fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}


// ======================================================
// MAIN ACTIVITY
// ======================================================
class Ai : ComponentActivity() {

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
                ChatApp()
            }
        }
    }
}

// ======================================================
// BACKGROUND IMAGE
// ======================================================
@Composable
fun Background() {
    Image(
        painter = painterResource(id = R.drawable.bb),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

// ======================================================
// EMPTY CHAT IMAGE
// ======================================================
@Composable
fun EmptyChatImage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.size(300.dp),
            alpha = 0.9f
        )
    }
}

// ======================================================
// USER AVATAR
// ======================================================
@Composable
fun UserAvatar(size: Dp = 40.dp, onClick: () -> Unit) {

    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName ?: "U"
    val photoUrl = user?.photoUrl?.toString()?.replace("s96-c", "s4096-c")

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                text = name.first().toString(),
                fontSize = (size.value / 2.3).sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ======================================================
// TOP BAR
// ======================================================
@Composable
fun EchoTopBar() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    )
                )
                .blur(15.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.12f)
                        )
                    ),
                    RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    context.startActivity(
                        Intent(context, DataControlsActivity::class.java)
                    )
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 1.0f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mes),
                    contentDescription = "Data Controls",
                    tint = Color.Black.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Echo AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.92f),
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "Bharath App Studio",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.weight(1f))

            UserAvatar(size = 40.dp) {
                context.startActivity(
                    Intent(context, Setting::class.java)
                )
            }
        }
    }
}


// ======================================================
// CHAT APP ROOT
// ======================================================
@Composable
fun ChatApp() {

    val model = remember {
        GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = "AIzaSyDqeGpmEaLZB--PGrV65xlaUk8NikLvVVc"
        )
    }

    Box(Modifier.fillMaxSize()) {
        Background()
        Column {
            EchoTopBar()
            ChatScreen(model)
        }
    }
}

// ======================================================
// THINKING ANIMATION COMPONENT (iOS STYLE)
// ======================================================
@Composable
fun ThinkingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    // Function to create sequential bounce for each dot
    @Composable
    fun animateDot(delay: Int): Float {
        val anim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -8f, // Bounce height
            animationSpec = infiniteRepeatable(
                animation = weightlessEasingTween(delay),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        return anim
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp)) // iOS style more rounded
                .background(Color(0xFFFFECB3).copy(alpha = 0.45f))
                .border(
                    0.5.dp,
                    Color.White.copy(alpha = 0.40f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Three iOS-style dots
                Dot(animateDot(0))
                Dot(animateDot(150))
                Dot(animateDot(300))
            }
        }
    }
}

@Composable
fun Dot(offsetY: Float) {
    Box(
        modifier = Modifier
            .graphicsLayer(translationY = offsetY)
            .size(7.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
    )
}

// Helper for iOS-like physics
fun weightlessEasingTween(delay: Int) = tween<Float>(
    durationMillis = 400,
    delayMillis = delay,
    easing = FastOutSlowInEasing
)


// ======================================================
// MAIN CHAT SCREEN (LOGIC + UI)
// ======================================================
@Composable
fun ChatScreen(model: GenerativeModel) {

    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val dbRef = FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .child(uid)
        .child("chats")

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
        }

    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                messages.clear()
                s.children
                    .mapNotNull { it.getValue(ChatMessage::class.java) }
                    .forEach { messages.add(it) }
                messages.sortBy { it.timestamp }
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    LaunchedEffect(messages.size, loading) {
        if (messages.isNotEmpty() || loading) {
            listState.animateScrollToItem(if (loading) messages.size else messages.lastIndex)
        }
    }

    fun send() {
        if ((input.isBlank() && selectedImageUri == null) || loading) return
        if (!isInternetAvailable(context)) return

        val question = input.trim()
        val uri = selectedImageUri

        input = ""
        selectedImageUri = null
        keyboard?.hide()

        val key = dbRef.push().key ?: return
        dbRef.child(key).setValue(
            ChatMessage(
                id = key,
                text = question,
                isUser = true,
                imageUri = uri?.toString(),
                timestamp = System.currentTimeMillis()
            )
        )

        loading = true

        scope.launch {
            try {
                val response = if (uri != null) {
                    val bitmap = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
                    model.generateContent(content {
                        image(bitmap!!)
                        text(question.ifBlank { "Analyze image" })
                    }).text
                } else {
                    model.generateContent(question).text
                }

                val rKey = dbRef.push().key ?: ""
                dbRef.child(rKey).setValue(
                    ChatMessage(
                        id = rKey,
                        text = response ?: "No response",
                        isUser = false,
                        imageUri = null,
                        timestamp = System.currentTimeMillis()
                    )
                )

            } catch (e: Exception) {
                val rKey = dbRef.push().key ?: ""
                dbRef.child(rKey).setValue(
                    ChatMessage(
                        id = rKey,
                        text = "Error: ${e.message}",
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } finally {
                loading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        if (messages.isEmpty() && !loading) {
            EmptyChatImage()
        }

        Column {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    AnimatedVisibility(visible = true, enter = fadeIn()) {
                        ChatBubble(msg = msg, onLongPress = { deleteTargetId = msg.id })
                    }
                }

                if (loading) {
                    item {
                        ThinkingAnimation()
                    }
                }
            }

            if (selectedImageUri != null) {
                Box(modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.04f)).border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            InputBar(
                text = input,
                onChange = { input = it },
                onSend = { send() },
                onImageClick = { imagePicker.launch("image/*") }
            )
        }
    }

    if (deleteTargetId != null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val messageToCopy = messages.firstOrNull { it.id == deleteTargetId }?.text ?: ""

        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Message options") },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setPrimaryClip(ClipData.newPlainText("chat", messageToCopy))
                    deleteTargetId = null
                }) { Text("Copy") }
                TextButton(onClick = {
                    dbRef.child(deleteTargetId!!).removeValue()
                    deleteTargetId = null
                }) { Text("Delete", color = Color.Red) }
            }
        )
    }
}

// ======================================================
// MODERN CHAT BUBBLE
// ======================================================
@Composable
fun ChatBubble(msg: ChatMessage, onLongPress: () -> Unit) {
    val isUser = msg.isUser
    val bubbleShape = RoundedCornerShape(
        topStart = 22.dp,
        topEnd = 22.dp,
        bottomStart = if (isUser) 22.dp else 8.dp,
        bottomEnd = if (isUser) 8.dp else 22.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }
                .widthIn(max = 320.dp)
                .clip(bubbleShape)
                .background(if (isUser) Color(0xB3C8E6C9) else Color(0xFFFFECB3).copy(alpha = 0.45f))
                .border(1.dp, Color.White.copy(alpha = 0.30f), bubbleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (msg.imageUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    AsyncImage(
                        model = msg.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.6f),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Text(
                text = parseMarkdown(msg.text),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = Color.Black
            )
        }
    }
}

// ======================================================
// INPUT BAR
// ======================================================
@Composable
fun InputBar(text: String, onChange: (String) -> Unit, onSend: () -> Unit, onImageClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(30.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onImageClick,
            modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)).border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(painter = painterResource(id = R.drawable.img), contentDescription = null, tint = Color.Black.copy(alpha = 0.85f), modifier = Modifier.size(24.dp))
        }

        Spacer(Modifier.width(10.dp))

        TextField(
            value = text,
            onValueChange = onChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask Echo…", color = Color.Black.copy(alpha = 0.45f)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black
            )
        )

        Spacer(Modifier.width(10.dp))

        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank(),
            modifier = Modifier.size(46.dp).clip(CircleShape).background(if (text.isNotBlank()) Color.Black else Color.Black.copy(alpha = 0.25f))
        ) {
            Icon(painter = painterResource(id = R.drawable.send), contentDescription = null, tint = Color.White)
        }
    }
}