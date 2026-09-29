package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class CommandItem(
    val title: String,
    val category: String,
    val shortcut: String? = null,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun CommandPaletteDialog(
    commands: List<CommandItem>,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredCommands = remember(query, commands) {
        if (query.isBlank()) commands
        else commands.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = VsCodeSidebar,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Command Search input
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type a command or search...", fontSize = 13.sp) },
                    leadingIcon = {
                        Text(
                            text = ">",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = VsCodeAccent,
                            fontSize = 16.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("command_palette_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    if (filteredCommands.isEmpty()) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text("No matching commands", color = VsCodeTextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    items(filteredCommands) { cmd ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    cmd.action()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = cmd.icon,
                                contentDescription = null,
                                tint = VsCodeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cmd.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = cmd.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VsCodeTextMuted
                                )
                            }
                            cmd.shortcut?.let { shortcut ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = shortcut,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = VsCodeTextMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = VsCodeBorder.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
