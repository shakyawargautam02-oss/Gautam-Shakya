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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FileEntity
import com.example.data.db.GitCommitEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SourceControlView(
    branchName: String,
    files: List<FileEntity>,
    commits: List<GitCommitEntity>,
    onCommit: (message: String) -> Unit,
    onSelectFile: (FileEntity) -> Unit,
    onChangeBranch: (newBranch: String) -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    var showBranchDialog by remember { mutableStateOf(false) }

    val changedFiles = remember(files) {
        files.filter { it.gitStatus != "unmodified" }
    }

    Surface(
        color = VsCodeSidebar,
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "SOURCE CONTROL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeTextMuted,
                    letterSpacing = 1.sp
                )

                IconButton(onClick = onCloseSidebar, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Close", modifier = Modifier.size(18.dp), tint = VsCodeText)
                }
            }

            HorizontalDivider(color = VsCodeBorder, thickness = 1.dp)

            // Branch selector bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                onClick = { showBranchDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AccountTree, contentDescription = "Branch", tint = VsCodeAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = branchName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text("switch", style = MaterialTheme.typography.labelSmall, color = VsCodeTextMuted)
                }
            }

            // Commit Box
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    placeholder = { Text("Message (Ctrl+Enter to commit)", fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("git_commit_message_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val trimmed = commitMessage.trim()
                        if (trimmed.isNotEmpty()) {
                            onCommit(trimmed)
                            commitMessage = ""
                        }
                    },
                    enabled = commitMessage.isNotBlank() && changedFiles.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("git_commit_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Commit (${changedFiles.size})", fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = VsCodeBorder, thickness = 1.dp)

            // Changes & History list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            ) {
                // Changes Header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CHANGES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VsCodeTextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge {
                            Text("${changedFiles.size}")
                        }
                    }
                }

                if (changedFiles.isEmpty()) {
                    item {
                        Text(
                            text = "No local changes to commit",
                            style = MaterialTheme.typography.bodySmall,
                            color = VsCodeTextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(changedFiles, key = { "changed_${it.id}" }) { file ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFile(file) }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            val (icon, tint) = getFileIconAndTint(file.name, file.isFolder)
                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = VsCodeText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            val statusLabel = if (file.gitStatus == "added") "U" else "M"
                            val statusColor = if (file.gitStatus == "added") GitAdded else GitModified
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                // Commits History Header
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "COMMIT HISTORY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VsCodeTextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge {
                            Text("${commits.size}")
                        }
                    }
                }

                items(commits, key = { "commit_${it.id}" }) { commit ->
                    val dateStr = remember(commit.timestamp) {
                        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(commit.timestamp))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = commit.commitHash,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = VsCodeAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                color = VsCodeTextMuted
                            )
                        }
                        Text(
                            text = commit.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(color = VsCodeBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }

    // Branch switch/create dialog
    if (showBranchDialog) {
        var newBranchInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBranchDialog = false },
            title = { Text("Switch / Create Git Branch") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current: $branchName", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = newBranchInput,
                        onValueChange = { newBranchInput = it },
                        placeholder = { Text("e.g., feature/login, develop") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newBranchInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onChangeBranch(trimmed)
                            showBranchDialog = false
                        }
                    }
                ) {
                    Text("Switch Branch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBranchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
