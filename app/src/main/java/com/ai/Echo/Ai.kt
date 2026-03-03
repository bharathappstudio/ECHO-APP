package com.ai.Echo

// ===================== ANDROID IMPORTS =====================
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.WindowManager
import android.graphics.Color as SysColor

// ===================== ACTIVITY + COMPOSE =====================
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// ===================== IMAGE LOADING & NETWORKING =====================
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

// ===================== FIREBASE & COROUTINES =====================
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ======================================================
// INTERNET CHECK & DATA MODEL
// ======================================================
fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        @Suppress("DEPRECATION") cm.activeNetworkInfo?.isConnected == true
    }
}

@IgnoreExtraProperties
data class ChatMessage(
    var id: String = "",
    var text: String = "",
    var isUser: Boolean = false,
    var imageUri: String? = null,
    var timestamp: Long = 0L
)

@Composable
fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContent {
            MaterialTheme { ChatApp() }
        }
    }
}

// ======================================================
// UI COMPONENTS (YOUR EXACT STYLE)
// ======================================================
@Composable fun Background() { Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3E0))) }

@Composable fun EmptyChatImage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.bg), contentDescription = null, modifier = Modifier.size(300.dp), alpha = 0.9f)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserAvatar(size: Dp = 40.dp, onClick: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    var isInitialLoading by remember { mutableStateOf(true) }
    val googleColors = listOf(Color(0xFF4285F4), Color(0xFFEA4335), Color(0xFFFBBC05), Color(0xFF34A853))
    var colorIndex1 by remember { mutableIntStateOf(0) }
    var colorIndex2 by remember { mutableIntStateOf(1) }

    LaunchedEffect(isInitialLoading) {
        if (isInitialLoading) {
            launch { while (true) { delay(500); colorIndex1 = (colorIndex1 + 1) % googleColors.size; colorIndex2 = (colorIndex2 + 1) % googleColors.size } }
            delay(2500); isInitialLoading = false
        }
    }

    val animatedColor1 by animateColorAsState(targetValue = googleColors[colorIndex1], animationSpec = tween(500))
    val animatedColor2 by animateColorAsState(targetValue = googleColors[colorIndex2], animationSpec = tween(500))

    Box(modifier = Modifier.size(size).clip(CircleShape).background(Color.White.copy(0.22f)).clickable { if (!isInitialLoading) onClick() }, contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = isInitialLoading, transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() }) { loading ->
            if (loading) {
                LoadingIndicator(
                    modifier = Modifier.size(50.dp).graphicsLayer(alpha = 0.99f).drawWithContent {
                        drawContent()
                        drawRect(brush = Brush.linearGradient(listOf(animatedColor1, animatedColor2)), blendMode = BlendMode.SrcAtop)
                    }, color = animatedColor1
                )
            } else {
                AsyncImage(model = user?.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            }
        }
    }
}

@Composable
fun EchoTopBar() {
    val context = LocalContext.current

    val labels = listOf("Bharath App Studio", "GPT-4 Model")
    var index by remember { mutableIntStateOf(0) }
    var displayedText by remember { mutableStateOf("") }

    // Label rotation loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Stay on completed text for 4 seconds
            index = (index + 1) % labels.size
        }
    }

    // Typing effect logic
    LaunchedEffect(index) {
        val fullText = labels[index]
        displayedText = "" // Reset text for new label
        fullText.forEachIndexed { charIndex, _ ->
            displayedText = fullText.substring(0, charIndex + 1)
            delay(80) // Normal human typing speed (80ms per letter)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp)) {
        Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(30.dp)).background(Color.White.copy(0.30f)).blur(15.dp))
        Row(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(30.dp)).border(1.dp, Color.White.copy(0.45f), RoundedCornerShape(30.dp)).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { context.startActivity(Intent(context, DataControlsActivity::class.java)) }, modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White)) {
                Icon(painter = painterResource(id = R.drawable.mes), contentDescription = null, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ECHO Ai", fontSize = 15.sp, fontWeight = FontWeight.Light, color = Color.Black.copy(0.8f))

                // Normal Speed Typing Text
                Text(
                    text = displayedText,
                    fontSize = 11.sp,
                    color = Color.Black.copy(0.55f),
                    maxLines = 1
                )
            }
            Spacer(Modifier.weight(1f))
            UserAvatar { context.startActivity(Intent(context, Setting::class.java)) }
        }
    }
}

// ======================================================
// GITHUB API CLIENT - FIXED FOR GPT-5 (404 & 400 FIX)
// ======================================================
class GitHubModelClient(private val apiKey: String) {
    private val client = OkHttpClient()
    suspend fun generate(prompt: String, bitmap: Bitmap?): String? = withContext(Dispatchers.IO) {
        try {
            val contentArray = JSONArray().apply {
                put(JSONObject().apply { put("type", "text"); put("text", prompt.ifBlank { "Explain this image" }) })
                bitmap?.let {
                    val stream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    put(JSONObject().apply { put("type", "image_url"); put("image_url", JSONObject().apply { put("url", "data:image/jpeg;base64,$base64") }) })
                }
            }
            val body = JSONObject().apply {
                put("messages", JSONArray().put(JSONObject().apply { put("role", "user"); put("content", contentArray) }))
                put("model", "gpt-4o") // Or your preferred GitHub model
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://models.inference.ai.azure.com/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body).build()

            client.newCall(request).execute().use { response ->
                val json = JSONObject(response.body?.string() ?: "")
                json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            }
        } catch (e: Exception) { "Error: ${e.localizedMessage}" }
    }
}

// ======================================================
// CHAT APP ROOT
// ======================================================
@Composable
fun ChatApp() {
    val apiKey = "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"
    val aiClient = remember { GitHubModelClient(apiKey) }

    Box(Modifier.fillMaxSize()) {
        Background()
        Column(Modifier.fillMaxSize()) {
            EchoTopBar()
            Box(Modifier.weight(1f)) { ChatScreen(aiClient) }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContainedLoadingIndicator() {
    val colors = listOf(Color(0xFFFFC107), Color(0xFFF44336), Color(0xFF4CAF50))
    var idx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(true) { delay(800); idx = (idx + 1) % colors.size } }
    val animColor by animateColorAsState(colors[idx], tween(600))
    Row(Modifier.fillMaxWidth().padding(16.dp, 2.dp)) {
        Box(Modifier.clip(RoundedCornerShape(200.dp)).background(Color(0xFFF0F4C3).copy(0.45f)).padding(16.dp, 12.dp)) {
            LoadingIndicator(Modifier.size(30.dp), color = animColor)
        }
    }
}

// ======================================================
// MAIN CHAT SCREEN
// ======================================================
@Composable
fun ChatScreen(aiClient: GitHubModelClient) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(uid).child("chats")

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedImageUri = it }

    LaunchedEffect(Unit) {
        dbRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                messages.clear()
                messages.addAll(s.children.mapNotNull { it.getValue(ChatMessage::class.java) })
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    LaunchedEffect(messages.size) { if(messages.isNotEmpty()) listState.animateScrollToItem(messages.size-1) }

    fun send() {
        if ((input.isBlank() && selectedImageUri == null) || loading || !isInternetAvailable(context)) return
        val text = input; val uri = selectedImageUri
        input = ""; selectedImageUri = null

        val key = dbRef.push().key ?: return
        dbRef.child(key).setValue(ChatMessage(key, text, true, uri?.toString(), System.currentTimeMillis()))
        loading = true

        scope.launch {
            val bitmap = uri?.let { context.contentResolver.openInputStream(it).use { s -> BitmapFactory.decodeStream(s) } }
            val response = aiClient.generate(text, bitmap)
            val rKey = dbRef.push().key ?: ""
            dbRef.child(rKey).setValue(ChatMessage(rKey, response ?: "No response", false, null, System.currentTimeMillis()))
            loading = false
        }
    }

    Box(Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
        if (messages.isEmpty() && !loading) EmptyChatImage()
        Column {
            LazyColumn(Modifier.weight(1f).padding(12.dp), state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages, key = { it.id }) { msg -> ChatBubble(msg, onLongPress = { deleteTargetId = msg.id }) }
                if (loading) item { ContainedLoadingIndicator() }
            }
            if (selectedImageUri != null) {
                AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.padding(20.dp, 0.dp, 0.dp, 10.dp).size(80.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color.Black.copy(0.08f), RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
            }
            InputBar(text = input, onChange = { input = it }, onSend = { send() }, onImageClick = { imagePicker.launch("image/*") })
        }
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
        bottomEnd = if (isUser) 5.dp else 22.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }
                .widthIn(max = 320.dp)
                .clip(bubbleShape)
                .background(if (isUser) Color(0x66C8E6C9) else Color(0x80FFECB3).copy(alpha = 0.45f))
                .border(1.dp, Color.White.copy(alpha = 0.80f), bubbleShape)
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
                color = Color(0xB3000000) // Light gray modern tone
            )
        }
    }
}

@Composable
fun InputBar(text: String, onChange: (String) -> Unit, onSend: () -> Unit, onImageClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(5.dp, 10.dp).clip(RoundedCornerShape(30.dp)).background(Color.White.copy(0.55f)).border(1.dp, Color.White.copy(0.30f), RoundedCornerShape(30.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onImageClick, Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(0.25f))) {
            Icon(painterResource(R.drawable.folder), null, Modifier.size(24.dp))
        }
        TextField(value = text, onValueChange = onChange, Modifier.weight(1f), placeholder = { Text("Ask Echo…", color = Color.Black.copy(0.45f)) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
        IconButton(onClick = onSend, enabled = text.isNotBlank(), modifier = Modifier.size(46.dp).clip(CircleShape).background(if (text.isNotBlank()) Color.Black else Color.Black.copy(0.25f))) {
            Icon(painterResource(R.drawable.send), null, tint = Color.White)
        }
    }
}