package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeBorder

enum class ActivityPanel {
    EXPLORER,
    SEARCH,
    SOURCE_CONTROL,
    RUNNER,
    TERMINAL,
    EXTENSIONS,
    SETTINGS
}

@Composable
fun ActivityBar(
    currentPanel: ActivityPanel?,
    onSelectPanel: (ActivityPanel?) -> Unit,
    modifiedFilesCount: Int,
    onOpenCommandPalette: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VsCodeActivityBar,
        modifier = modifier
            .width(52.dp)
            .fillMaxHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            // Top Primary Actions
            ActivityBarItem(
                icon = Icons.Default.Folder,
                label = "Explorer",
                isSelected = currentPanel == ActivityPanel.EXPLORER,
                onClick = {
                    onSelectPanel(if (currentPanel == ActivityPanel.EXPLORER) null else ActivityPanel.EXPLORER)
                },
                testTag = "activity_explorer"
            )

            ActivityBarItem(
                icon = Icons.Default.Search,
                label = "Search",
                isSelected = currentPanel == ActivityPanel.SEARCH,
                onClick = {
                    onSelectPanel(if (currentPanel == ActivityPanel.SEARCH) null else ActivityPanel.SEARCH)
                },
                testTag = "activity_search"
            )

            ActivityBarItem(
                icon = Icons.Default.AccountTree,
                label = "Source Control",
                isSelected = currentPanel == ActivityPanel.SOURCE_CONTROL,
                badgeCount = modifiedFilesCount,
                onClick = {
                    onSelectPanel(if (currentPanel == ActivityPanel.SOURCE_CONTROL) null else ActivityPanel.SOURCE_CONTROL)
                },
                testTag = "activity_git"
            )

            ActivityBarItem(
                icon = Icons.Default.PlayArrow,
                label = "Run & Debug",
                isSelected = currentPanel == ActivityPanel.RUNNER,
                onClick = {
                    onSelectPanel(if (currentPanel == ActivityPanel.RUNNER) null else ActivityPanel.RUNNER)
                },
                testTag = "activity_runner"
            )

            ActivityBarItem(
                icon = Icons.Default.Terminal,
                label = "Terminal",
                isSelected = currentPanel == ActivityPanel.TERMINAL,
                onClick = {
                    onSelectPanel(if (currentPanel == ActivityPanel.TERMINAL) null else ActivityPanel.TERMINAL)
                },
                testTag = "activity_terminal"
            )

            ActivityBarItem(
                icon = Icons.Default.Extension,
                label = "Extensions & Themes",
                isSelected = currentPanel == ActivityPanel.EXTENSIONS,
                onClick = {
                    onSelectPanel(if (currentPanel == ActivityPanel.EXTENSIONS) null else ActivityPanel.EXTENSIONS)
                },
                testTag = "activity_extensions"
            )

            Spacer(modifier = Modifier.weight(1f))

            // Command Palette Button (Ctrl+Shift+P)
            IconButton(
                onClick = onOpenCommandPalette,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("command_palette_trigger")
            ) {
                Icon(
                    Icons.Default.Terminal,
                    contentDescription = "Command Palette",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom Settings Action
            ActivityBarItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentPanel == ActivityPanel.SETTINGS,
                onClick = {
                    onSelectPanel(if (currentPanel == ActivityPanel.SETTINGS) null else ActivityPanel.SETTINGS)
                },
                testTag = "activity_settings"
            )
        }
    }
}

@Composable
fun ActivityBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    testTag: String = ""
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        // Active indicator line on the left
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .height(30.dp)
                    .background(Color.White)
            )
        }

        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )

            if (badgeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else "$badgeCount",
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
