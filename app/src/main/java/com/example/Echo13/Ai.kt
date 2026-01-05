package com.ai.Echo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import android.graphics.Color as SysColor

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
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val animated: Boolean = false,
    val isThinking: Boolean = false
)

// ---------------- LOCAL STORAGE ----------------
private const val PREFS = "echo_chat"
private const val KEY_CHAT = "chat_data"

fun saveChats(context: Context, list: List<ChatMessage>) {
    val arr = JSONArray()
    list.forEach {
        val o = JSONObject()
        o.put("id", it.id)
        o.put("text", it.text)
        o.put("isUser", it.isUser)
        arr.put(o)
    }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_CHAT, arr.toString())
        .apply()
}

fun loadChats(context: Context): List<ChatMessage> {
    val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_CHAT, null) ?: return emptyList()

    val arr = JSONArray(json)
    val list = mutableListOf<ChatMessage>()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list.add(
            ChatMessage(
                id = o.getString("id"),
                text = o.getString("text"),
                isUser = o.getBoolean("isUser"),
                animated = false
            )
        )
    }
    return list
}

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

// ---------------- ROOT ----------------
@Composable
fun ChatApp() {
    val model = remember {
        GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = "AIzaSyCHW7Gcae4-RU1Upyq5kTnnW_1RH_OQiQA"
        )
    }

    Box(Modifier.fillMaxSize()) {
        Background()
        ChatScreen(model)
        TopRightRoundButton()
    }
}

// ---------------- TOP RIGHT BUTTON ----------------
@Composable
fun TopRightRoundButton() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, end = 20.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(
            onClick = {
                context.startActivity(
                    android.content.Intent(context, Setting::class.java)
                )
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.45f))
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.set),
                contentDescription = "Settings",
                tint = Color.Black
            )
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

    val messages = remember {
        mutableStateListOf<ChatMessage>().apply {
            addAll(loadChats(context))
            if (isEmpty()) {
                add(
                    ChatMessage(
                        text = "Hi, I am Echo 👋",
                        isUser = false,
                        animated = true
                    )
                )
            }
        }
    }

    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.lastIndex)
        saveChats(context, messages.filter { !it.isThinking })
    }

    fun send() {
        if (input.isBlank() || loading) return

        if (!isInternetAvailable(context)) {
            messages.add(
                ChatMessage(
                    text = "⚠️ No internet connection",
                    isUser = false,
                    animated = true
                )
            )
            return
        }

        val question = input.trim()
        input = ""
        keyboard?.hide()

        messages.add(ChatMessage(text = question, isUser = true, animated = true))
        loading = true

        val thinkingId = UUID.randomUUID().toString()
        messages.add(
            ChatMessage(
                id = thinkingId,
                text = "Echo is thinking",
                isUser = false,
                isThinking = true
            )
        )

        scope.launch {
            try {
                delay(800)
                val response = model.generateContent(question).text ?: "No response"
                messages.removeAll { it.id == thinkingId }
                messages.add(
                    ChatMessage(
                        text = response,
                        isUser = false,
                        animated = true
                    )
                )
            } catch (e: Exception) {
                messages.removeAll { it.id == thinkingId }
                messages.add(
                    ChatMessage(
                        text = "Error: ${e.message}",
                        isUser = false,
                        animated = true
                    )
                )
            } finally {
                loading = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 80.dp, bottom = 16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AnimatedMessage(msg)
            }
        }

        InputBar(
            text = input,
            onChange = { input = it },
            onSend = { send() }
        )
    }
}

// ---------------- MESSAGE ----------------
@Composable
fun AnimatedMessage(msg: ChatMessage) {
    AnimatedVisibility(visible = true, enter = fadeIn()) {
        when {
            msg.isThinking -> ThinkingAnimatedText(msg.text)
            msg.isUser -> UserBubble(msg.text)
            else -> AiBubble(msg)
        }
    }
}

// ---------------- THINKING ----------------
@Composable
fun ThinkingAnimatedText(baseText: String) {
    var dots by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            dots = ""
            delay(300)
            dots = "."
            delay(300)
            dots = ".."
            delay(300)
            dots = "..."
            delay(300)
        }
    }
    Text(
        text = "$baseText$dots",
        fontSize = 14.sp,
        color = Color.Black.copy(alpha = 0.55f),
        modifier = Modifier.padding(start = 12.dp)
    )
}

// ---------------- AI BUBBLE (ANIMATION ONE TIME) ----------------
@Composable
fun AiBubble(msg: ChatMessage) {
    var shown by remember { mutableStateOf(msg.text) }
    var animatedDone by remember { mutableStateOf(!msg.animated) }

    LaunchedEffect(msg.id) {
        if (!animatedDone) {
            shown = ""
            for (c in msg.text) {
                shown += c
                delay(14)
            }
            animatedDone = true
        }
    }

    Box(
        Modifier
            .widthIn(50.dp, 500.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFEDB3).copy(alpha = 0.4f))
            .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(shown, fontSize = 16.sp, color = Color(0xFF111111), lineHeight = 22.sp)
    }
}

// ---------------- USER BUBBLE ----------------
@Composable
fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFC8E6C9))
                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(text, fontSize = 16.sp, color = Color.Black)
        }
    }
}

// ---------------- INPUT BAR ----------------
@Composable
fun InputBar(
    text: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val enabled = text.isNotBlank()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.40f))
            .border(2.dp, Color.White.copy(alpha = 1f), RoundedCornerShape(22.dp))
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
                .background(
                    if (enabled) Color(0x807BE17B)
                    else Color.White.copy(alpha = 0.25f)
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = "Send",
                tint = if (enabled) Color.Black else Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
