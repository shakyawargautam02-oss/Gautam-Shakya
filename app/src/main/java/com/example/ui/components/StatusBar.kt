package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBar(
    branchName: String,
    currentLine: Int,
    currentCol: Int,
    language: String,
    onToggleTerminal: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            // Left Status Items
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onToggleTerminal)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = "Git Branch",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = branchName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "0↓ 0↑",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HighlightOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = " 0",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = " 0",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Right Status Items
            Text(
                text = "Ln $currentLine, Col $currentCol",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = "Spaces: 2",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = "UTF-8",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = language.replaceFirstChar { it.uppercase() },
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier
                    .clickable(onClick = onOpenCommandPalette)
                    .padding(end = 6.dp)
                    .testTag("status_language_badge")
            )

            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
