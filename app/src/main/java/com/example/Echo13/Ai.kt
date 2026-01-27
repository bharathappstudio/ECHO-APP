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

        // Allow content behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Transparent system bars
        window.statusBarColor = SysColor.TRANSPARENT
        window.navigationBarColor = SysColor.TRANSPARENT

        // Fullscreen layout
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Light icons
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // Set Compose UI
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
        painter = painterResource(id = R.drawable.k2),
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
// MODERN GLASS TOP BAR (NO ERRORS • CLEAN • 2026 STYLE)
// ======================================================
@Composable
fun EchoTopBar() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.55f))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.30f),
                    RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT ACTION
            IconButton(
                onClick = {
                    context.startActivity(
                        Intent(context, DataControlsActivity::class.java)
                    )
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.30f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mes),
                    contentDescription = "Data Controls",
                    tint = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            // TITLE
            Text(
                text = "Echo Ai",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(Modifier.weight(1f))

            // USER AVATAR
            UserAvatar(size = 38.dp) {
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

    // Gemin/home/asus-s14/Downloadsi AI model
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
// MAIN CHAT SCREEN (LOGIC + UI)
// ======================================================
@Composable
fun ChatScreen(model: GenerativeModel) {

    // Get current Android context
    val context = LocalContext.current

    // Keyboard controller (hide keyboard after send)
    val keyboard = LocalSoftwareKeyboardController.current

    // Coroutine scope for async work
    val scope = rememberCoroutineScope()

    // LazyColumn scroll state
    val listState = rememberLazyListState()

    // Get logged-in Firebase user ID
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Firebase database reference for user's chats
    val dbRef = FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .child(uid)
        .child("chats")

    // Chat messages list (Compose observable)
    val messages = remember { mutableStateListOf<ChatMessage>() }

    // User input text
    var input by remember { mutableStateOf("") }

    // Loading state (disable send while AI responds)
    var loading by remember { mutableStateOf(false) }

    // Message ID selected for copy/delete
    var deleteTargetId by remember { mutableStateOf<String?>(null) }

    // Selected image URI
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker launcher
    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
        }

    // ======================================================
    // LISTEN TO FIREBASE CHAT CHANGES
    // ======================================================
    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {

            // Called when DB data changes
            override fun onDataChange(s: DataSnapshot) {

                // Clear current messages
                messages.clear()

                // Convert Firebase data to ChatMessage list
                s.children
                    .mapNotNull { it.getValue(ChatMessage::class.java) }
                    .forEach { messages.add(it) }

                // Sort messages by time
                messages.sortBy { it.timestamp }
            }

            override fun onCancelled(e: DatabaseError) {}
        })
    }

    // ======================================================
    // AUTO SCROLL TO LAST MESSAGE
    // ======================================================
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    // ======================================================
    // SEND MESSAGE FUNCTION
    // ======================================================
    fun send() {

        // Prevent empty send or double send
        if ((input.isBlank() && selectedImageUri == null) || loading) return

        // Internet check
        if (!isInternetAvailable(context)) return

        // Store values locally
        val question = input.trim()
        val uri = selectedImageUri

        // Reset UI
        input = ""
        selectedImageUri = null
        keyboard?.hide()

        // Push user message to Firebase
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

        // Start loading
        loading = true

        // Launch AI response
        scope.launch {
            try {

                // If image exists, send image + text
                val response = if (uri != null) {

                    val bitmap =
                        context.contentResolver
                            .openInputStream(uri)
                            .use { BitmapFactory.decodeStream(it) }

                    model.generateContent(
                        content {
                            image(bitmap!!)
                            text(question.ifBlank { "Analyze image" })
                        }
                    ).text

                } else {
                    // Text only message
                    model.generateContent(question).text
                }

                // Push AI response to Firebase
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

                // Error message
                val rKey = dbRef.push().key ?: ""
                dbRef.child(rKey).setValue(
                    ChatMessage(
                        id = rKey,
                        text = "Error: ${e.message}",
                        isUser = false,
                        imageUri = null,
                        timestamp = System.currentTimeMillis()
                    )
                )

            } finally {
                // Stop loading
                loading = false
            }
        }
    }

    // ======================================================
    // MAIN CHAT UI
    // ======================================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Adjust for keyboard
    ) {

        // Show empty image when no chats
        if (messages.isEmpty()) {
            EmptyChatImage()
        }

        Column {

            // ===================== MESSAGE LIST =====================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn()
                    ) {
                        ChatBubble(
                            msg = msg,
                            onLongPress = { deleteTargetId = msg.id }
                        )
                    }
                }
            }

            // ===================== MODERN IMAGE PREVIEW =====================
            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp, bottom = 10.dp)
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.04f))
                            .border(
                                width = 1.dp,
                                color = Color.Black.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }


            // ===================== INPUT BAR =====================
            InputBar(
                text = input,
                onChange = { input = it },
                onSend = { send() },
                onImageClick = { imagePicker.launch("image/*") }
            )
        }
    }

    // ======================================================
    // COPY / DELETE DIALOG
    // ======================================================
    if (deleteTargetId != null) {

        // Clipboard manager
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Text to copy
        val messageToCopy =
            messages.firstOrNull { it.id == deleteTargetId }?.text ?: ""

        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Message options") },

            confirmButton = {

                // Copy button
                TextButton(
                    onClick = {
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("chat", messageToCopy)
                        )
                        deleteTargetId = null
                    }
                ) {
                    Text("Copy")
                }

                // Delete button
                TextButton(
                    onClick = {
                        dbRef.child(deleteTargetId!!).removeValue()
                        deleteTargetId = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            }
        )
    }
}

// ======================================================
// MODERN CHAT BUBBLE (USING YOUR OLD COLOR STYLE)
// ======================================================
@Composable
fun ChatBubble(
    msg: ChatMessage,
    onLongPress: () -> Unit
) {
    val isUser = msg.isUser

    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isUser) 20.dp else 6.dp,
        bottomEnd = if (isUser) 6.dp else 20.dp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 6.dp),
        horizontalArrangement =
            if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .background(
                    if (isUser)
                        Color(0xB3C8E6C9) // YOUR OLD USER COLOR
                    else
                        Color(0xFFFFECB3).copy(alpha = 0.4f) // YOUR OLD BOT COLOR
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 35f),
                    bubbleShape
                )
                .padding(14.dp)
        ) {

            // Modern image message (Material 3 style)
            if (msg.imageUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F0F1A)
                    )
                ) {
                    AsyncImage(
                        model = msg.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }


            // Text message
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
// MODERN CLEAN INPUT BAR (NO BLUR • NO ERRORS)
// ======================================================
@Composable
fun InputBar(
    text: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    onImageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.50f))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.25f),
                RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // MODERN IMAGE BUTTON
        IconButton(
            onClick = onImageClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.22f)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.mes),
                contentDescription = "Image",
                tint = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier.size(25.dp)
            )
        }


        Spacer(Modifier.width(10.dp))

        // TEXT FIELD (SAFE)
        TextField(
            value = text,
            onValueChange = onChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Ask Echo…",
                    color = Color.Black.copy(alpha = 0.45f)
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black
            )
        )

        Spacer(Modifier.width(10.dp))

        // SEND BUTTON
        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank(),
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (text.isNotBlank())
                        Color.Black
                    else
                        Color.Black.copy(alpha = 0.25f)
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.send),
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}





