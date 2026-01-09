package com.ai.Echo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.content.Intent
import android.graphics.Color as SysColor

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage

import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.IgnoreExtraProperties

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.content.ClipData
import android.content.ClipboardManager


// ---------------- INTERNET CHECK ----------------
fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        cm.activeNetworkInfo?.isConnected == true
    }
}

// ---------------- DATA ----------------
@IgnoreExtraProperties
data class ChatMessage(
    var id: String = "",
    var text: String = "",
    var isUser: Boolean = false,
    var animated: Boolean = false,
    var isThinking: Boolean = false,
    var timestamp: Long = 0L
)

// ---------------- ACTIVITY ----------------
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

// ---------------- BACKGROUND ----------------
@Composable
fun Background() {
    Image(
        painter = painterResource(id = R.drawable.k2),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

// ---------------- USER AVATAR ----------------
@Composable
fun UserAvatar(size: Dp = 40.dp, onClick: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName ?: "U"

    val photoUrl = user?.photoUrl
        ?.toString()
        ?.replace("s96-c", "s4096-c")
        ?.replace("s400-c", "s4096-c")

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

// ---------------- TOP BAR ----------------
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
                .height(58.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFFFF8E1).copy(alpha = 0.50f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(40.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    context.startActivity(
                        Intent(context, DataControlsActivity::class.java)
                    )
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 22f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mes),
                    contentDescription = "Data Controls",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "Welcome to Echo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.weight(1f))

            UserAvatar(size = 40.dp) {
                context.startActivity(Intent(context, Setting::class.java))
            }

            Spacer(Modifier.width(8.dp))
        }
    }
}

// ---------------- ROOT ----------------
@Composable
fun ChatApp() {
    val model = remember {
        GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = "AIzaSyDWjz5rVhG8jWspAvimx1XvYyXLKVuRDtU"
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

// ---------------- CHAT SCREEN ----------------
@Composable
fun ChatScreen(model: GenerativeModel) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val dbRef = FirebaseDatabase.getInstance().reference
        .child("users")
        .child(uid)
        .child("chats")

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                snapshot.children.forEach { child ->
                    val msg = child.getValue(ChatMessage::class.java)
                    if (msg != null) messages.add(msg)
                }
                messages.sortBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    fun saveMessage(text: String, isUser: Boolean) {
        val key = dbRef.push().key ?: return
        dbRef.child(key).setValue(
            ChatMessage(
                id = key,
                text = text,
                isUser = isUser,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun deleteMessage(id: String) {
        dbRef.child(id).removeValue()
    }

    fun send() {
        if (input.isBlank() || loading) return
        if (!isInternetAvailable(context)) {
            saveMessage("No internet connection", false)
            return
        }

        val question = input.trim()
        input = ""
        keyboard?.hide()

        saveMessage(question, true)
        loading = true

        scope.launch {
            try {
                delay(800)
                val response = model.generateContent(question).text ?: "No response"
                saveMessage(response, false)
            } catch (e: Exception) {
                saveMessage("Error: ${e.message}", false)
            } finally {
                loading = false
            }
        }
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AnimatedMessage(
                    msg = msg,
                    onLongPress = { deleteTargetId = msg.id }
                )
            }
        }

        InputBar(text = input, onChange = { input = it }, onSend = { send() })
    }

    //                                     delet ui

    if (deleteTargetId != null) {

        val context = LocalContext.current
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val messageToCopy =
            messages.firstOrNull { it.id == deleteTargetId }?.text ?: ""

        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Message options",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Choose what you want to do with this message.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // COPY BUTTON
                    TextButton(
                        onClick = {
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("chat_message", messageToCopy)
                            )
                            deleteTargetId = null
                        }
                    ) {
                        Text("Copy")
                    }

                    // DELETE BUTTON
                    TextButton(
                        onClick = {
                            deleteMessage(deleteTargetId!!)
                            deleteTargetId = null
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteTargetId = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}

// ---------------- MESSAGE ----------------
@Composable
fun AnimatedMessage(
    msg: ChatMessage,
    onLongPress: () -> Unit
) {
    AnimatedVisibility(visible = true, enter = fadeIn()) {
        if (msg.isUser)
            UserBubble(msg.text, onLongPress)
        else
            AiBubble(msg, onLongPress)
    }
}

// ---------------- AI BUBBLE ----------------
@Composable
fun AiBubble(msg: ChatMessage, onLongPress: () -> Unit) {
    var shown by rememberSaveable(msg.id) { mutableStateOf(msg.text) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            Modifier
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
                .widthIn(50.dp, 500.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFFECB3).copy(alpha = 0.4f))
                .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(shown, fontSize = 16.sp)
        }
    }
}

// ---------------- USER BUBBLE ----------------
@Composable
fun UserBubble(text: String, onLongPress: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xB3C8E6C9))
                .border(2.dp, Color.White.copy(alpha = 0.80f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(text, fontSize = 16.sp)
        }
    }
}

// ---------------- INPUT BAR ----------------
@Composable
fun InputBar(text: String, onChange: (String) -> Unit, onSend: () -> Unit) {
    val enabled = text.isNotBlank()

    Row(
        Modifier.fillMaxWidth().padding(10.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 40f))
            .border(2.dp, Color.White, RoundedCornerShape(22.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            value = text,
            onValueChange = onChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask Echo…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.width(10.dp))

        IconButton(
            onClick = onSend,
            enabled = enabled,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0x807BE17B) else Color.White.copy(alpha = 0.25f))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = "Send"
            )
        }
    }
}
