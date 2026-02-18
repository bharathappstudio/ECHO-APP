package com.ai.Echo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class EchoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloWorld()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HelloWorld() {
    // Colors using 0xFF Hex Format
    val backgroundOrange = Color(0xFF66BB6A) // Light Orange
    val indicatorWhite = Color(0xFFFFFFFF)    // White
    val footerGray = Color(0xFFFFFFFF)       // Footer Text Gray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundOrange)
    ) {
        // Main Content Area (Centers the Loading Indicator)
        Box(
            modifier = Modifier
                .weight(1f) // Takes up all available space above the footer
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator(
                color = indicatorWhite
            )
        }

        // Footer Section
        // Footer with Custom Font Styling
        Text(
            text = "UI Was Developing By Bharath",
            fontSize = 12.sp,
            color = footerGray,
            // Using a built-in modern Sans-Serif font
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )
    }
}