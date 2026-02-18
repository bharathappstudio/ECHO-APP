// ===================== PACKAGE NAME =====================
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
import java.io.ByteArrayOutputStream

// ===================== ACTIVITY + COMPOSE =====================
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent

// ===================== COMPOSE UI =====================
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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

// ===================== OPEN ROUTER / NETWORKING =====================
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import kotlinx.coroutines.delay

// ======================================================
// OPEN ROUTER CLIENT HELPER (REPLACES GEMINI SDK)
// ======================================================
class OpenRouterManager(private val apiKey: String) {
    private val client = OkHttpClient()
    private val modelName = "google/gemini-2.0-flash-001" // Latest available Gemini on OpenRouter

    suspend fun generateResponse(prompt: String, base64Image: String? = null): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://openrouter.ai/api/v1/chat/completions"

                val messages = JSONArray()
                val messageObject = JSONObject()
                messageObject.put("role", "user")

                val contentArray = JSONArray()

                // Add Text
                contentArray.put(JSONObject().apply {
                    put("type", "text")
                    put("text", prompt)
                })

                // Add Image if exists
                base64Image?.let {
                    contentArray.put(JSONObject().apply {
                        put("type", "image_url")
                        put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$it"))
                    })
                }

                messageObject.put("content", contentArray)
                messages.put(messageObject)

                val jsonBody = JSONObject().apply {
                    put("model", modelName)
                    put("messages", messages)
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    val jsonRes = JSONObject(resBody ?: "")
                    jsonRes.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                }
            } catch (e: Exception) {
                "Error: ${e.localizedMessage}"
            }
        }
    }
}

// ======================================================
// INTERNET CHECK FUNCTION
// ======================================================
fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) cm.activeNetwork else null
    val caps = cm.getNetworkCapabilities(network)
    return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
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
            } else { append(part) }
        }
    }
}

class Ai : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { MaterialTheme { ChatApp() } }
    }
}

@Composable
fun Background() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3E0)))
}

@Composable
fun EmptyChatImage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.bg), contentDescription = null, modifier = Modifier.size(300.dp), alpha = 0.9f)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun UserAvatar(size: Dp = 40.dp, onClick: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName ?: "U"
    val photoUrl = user?.photoUrl?.toString()
    var isInitialLoading by remember { mutableStateOf(true) }
    val googleColors = listOf(Color(0xFF4285F4), Color(0xFFEA4335), Color(0xFFFBBC05), Color(0xFF34A853))
    var colorIndex1 by remember { mutableIntStateOf(0) }
    var colorIndex2 by remember { mutableIntStateOf(1) }

    LaunchedEffect(isInitialLoading) {
        if (isInitialLoading) {
            launch {
                while (true) {
                    delay(500)
                    colorIndex1 = (colorIndex1 + 1) % googleColors.size
                    colorIndex2 = (colorIndex2 + 1) % googleColors.size
                }
            }
            delay(2500)
            isInitialLoading = false
        }
    }

    val animatedColor1 by animateColorAsState(targetValue = googleColors[colorIndex1], animationSpec = tween(500))
    val animatedColor2 by animateColorAsState(targetValue = googleColors[colorIndex2], animationSpec = tween(500))

    Box(modifier = Modifier.size(size).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)).clickable { if (!isInitialLoading) onClick() }, contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = isInitialLoading) { loading ->
            if (loading) {
                LoadingIndicator(modifier = Modifier.size(50.dp).graphicsLayer(alpha = 0.99f).drawWithContent {
                    drawContent()
                    drawRect(brush = Brush.linearGradient(colors = listOf(animatedColor1, animatedColor2)), blendMode = BlendMode.SrcAtop)
                }, color = animatedColor1)
            } else {
                if (photoUrl != null) {
                    AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Text(text = name.first().toString().uppercase(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EchoTopBar() {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp)) {
        Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(30.dp)).background(Color.White.copy(alpha = 0.30f)).blur(15.dp))
        Row(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(30.dp)).border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(30.dp)).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { context.startActivity(Intent(context, DataControlsActivity::class.java)) }, modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White)) {
                Icon(painter = painterResource(id = R.drawable.mes), contentDescription = null, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Echo AI", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Bharath App Studio", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.weight(1f))
            UserAvatar(size = 40.dp) { context.startActivity(Intent(context, Setting::class.java)) }
        }
    }
}

// ======================================================
// CHAT APP ROOT (API KEY UPDATED HERE)
// ======================================================
@Composable
fun ChatApp() {
    val openRouterManager = remember {
        OpenRouterManager(apiKey = "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")
    }

    Box(Modifier.fillMaxSize()) {
        Background()
        Column(Modifier.fillMaxSize()) {
            EchoTopBar()
            Box(Modifier.weight(1f)) {
                ChatScreen(openRouterManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContainedLoadingIndicator() {
    val smileyColors = listOf(Color(0xFF4285F4), Color(0xFFEA4335), Color(0xFFFBBC05), Color(0xFF34A853))
    var colorIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(800); colorIndex = (colorIndex + 1) % smileyColors.size } }
    val animatedColor by animateColorAsState(targetValue = smileyColors[colorIndex], animationSpec = tween(600))
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), horizontalArrangement = Arrangement.Start) {
        Box(modifier = Modifier.clip(RoundedCornerShape(200.dp)).background(Color(0xFFF0F4C3).copy(alpha = 0.45f)).border(0.5.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(200.dp)).padding(horizontal = 16.dp, vertical = 12.dp)) {
            LoadingIndicator(modifier = Modifier.size(30.dp), color = animatedColor)
        }
    }
}

// ======================================================
// MAIN CHAT SCREEN (UPDATED FOR OPENROUTER)
// ======================================================
@Composable
fun ChatScreen(apiManager: OpenRouterManager) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(uid).child("chats")

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedImageUri = uri }

    LaunchedEffect(Unit) {
        dbRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val newList = s.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                messages.clear()
                messages.addAll(newList)
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    LaunchedEffect(messages.size, loading) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    // Helper to convert Bitmap to Base64 for OpenRouter
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
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
        dbRef.child(key).setValue(ChatMessage(id = key, text = question, isUser = true, imageUri = uri?.toString(), timestamp = System.currentTimeMillis()))

        loading = true
        scope.launch {
            try {
                var base64Str: String? = null
                if (uri != null) {
                    val bitmap = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
                    if (bitmap != null) base64Str = bitmapToBase64(bitmap)
                }

                val response = apiManager.generateResponse(
                    prompt = if (question.isEmpty()) "Describe this image" else question,
                    base64Image = base64Str
                )

                val rKey = dbRef.push().key ?: ""
                dbRef.child(rKey).setValue(ChatMessage(id = rKey, text = response, isUser = false, timestamp = System.currentTimeMillis()))
            } catch (e: Exception) {
                val rKey = dbRef.push().key ?: ""
                dbRef.child(rKey).setValue(ChatMessage(id = rKey, text = "Error: ${e.message}", isUser = false, timestamp = System.currentTimeMillis()))
            } finally {
                loading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
        if (messages.isEmpty() && !loading) EmptyChatImage()
        Column {
            LazyColumn(modifier = Modifier.weight(1f).padding(12.dp), state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages, key = { it.id }) { msg -> ChatBubble(msg = msg, onLongPress = { deleteTargetId = msg.id }) }
                if (loading) item(key = "loading") { ContainedLoadingIndicator() }
            }
            if (selectedImageUri != null) {
                AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.padding(start = 20.dp, bottom = 10.dp).size(80.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
            }
            InputBar(text = input, onChange = { input = it }, onSend = { send() }, onImageClick = { imagePicker.launch("image/*") })
        }
    }

    if (deleteTargetId != null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val messageToCopy = messages.firstOrNull { it.id == deleteTargetId }?.text ?: ""
        AlertDialog(onDismissRequest = { deleteTargetId = null }, shape = RoundedCornerShape(20.dp), title = { Text("Message options") }, confirmButton = {
            TextButton(onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("chat", messageToCopy)); deleteTargetId = null }) { Text("Copy") }
            TextButton(onClick = { dbRef.child(deleteTargetId!!).removeValue(); deleteTargetId = null }) { Text("Delete", color = Color.Red) }
        })
    }
}

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 10.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }
                .widthIn(max = 320.dp)
                .clip(bubbleShape)
                .background(if (isUser) Color(0x66C8E6C9) else Color(0x80FFECB3).copy(alpha = 0.45f))
                .border(1.dp, Color.White.copy(alpha = 0.80f), bubbleShape)
                .padding(horizontal = 10.dp, vertical = 12.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 55f))
            .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(30.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onImageClick,
            modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)).border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(painter = painterResource(id = R.drawable.folder), contentDescription = null, tint = Color.Black.copy(alpha = 0.85f), modifier = Modifier.size(24.dp))
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