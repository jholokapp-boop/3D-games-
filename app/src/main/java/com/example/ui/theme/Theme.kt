package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CyberRunnerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkCyberColorScheme,
        typography = Typography,
        content = content
    )
}
