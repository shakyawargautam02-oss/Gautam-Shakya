package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FileEntity
import com.example.ui.theme.*

@Composable
fun EditorTabsBar(
    openFiles: List<FileEntity>,
    activeFileId: String?,
    dirtyFileIds: Set<String>,
    onSelectTab: (FileEntity) -> Unit,
    onCloseTab: (FileEntity) -> Unit,
    onSaveCurrentFile: () -> Unit,
    onRunOrPreview: () -> Unit,
    onToggleFindReplace: () -> Unit,
    onFormatCode: () -> Unit,
    onCloseAllTabs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Surface(
        color = VsCodeTabBar,
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable tabs list
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(scrollState)
            ) {
                if (openFiles.isEmpty()) {
                    Text(
                        text = "No open editors",
                        style = MaterialTheme.typography.bodySmall,
                        color = VsCodeTextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                openFiles.forEach { file ->
                    val isActive = file.id == activeFileId
                    val isDirty = dirtyFileIds.contains(file.id)

                    EditorTabItem(
                        file = file,
                        isActive = isActive,
                        isDirty = isDirty,
                        onClick = { onSelectTab(file) },
                        onClose = { onCloseTab(file) }
                    )
                }
            }

            // Quick Toolbar Actions on Right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                // Save button
                IconButton(
                    onClick = onSaveCurrentFile,
                    enabled = activeFileId != null,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save File",
                        tint = if (dirtyFileIds.contains(activeFileId)) VsCodeAccent else VsCodeTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Run / Live Preview button
                IconButton(
                    onClick = onRunOrPreview,
                    enabled = activeFileId != null,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("run_preview_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run or Live Preview",
                        tint = GitAdded,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Find & Replace toggle button
                IconButton(
                    onClick = onToggleFindReplace,
                    enabled = activeFileId != null,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Find and Replace",
                        tint = VsCodeText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // More Menu
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions",
                            tint = VsCodeText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Format Document") },
                            leadingIcon = { Icon(Icons.Default.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMoreMenu = false
                                onFormatCode()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Close All Tabs") },
                            leadingIcon = { Icon(Icons.Default.CloseFullscreen, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMoreMenu = false
                                onCloseAllTabs()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditorTabItem(
    file: FileEntity,
    isActive: Boolean,
    isDirty: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val (icon, tint) = getFileIconAndTint(file.name, file.isFolder)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(if (isActive) VsCodeTabActive else VsCodeTabInactive)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
            .testTag("tab_${file.name}")
    ) {
        // Active top indicator bar (VS Code style blue top border)
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(VsCodeAccent)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = file.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) Color.White else VsCodeTextMuted,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Dirty indicator or Close button
            if (isDirty) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                        .clickable(onClick = onClose)
                        .padding(2.dp)
                )
            } else {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tab",
                        tint = VsCodeTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
