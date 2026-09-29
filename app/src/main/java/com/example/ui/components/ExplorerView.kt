package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FileEntity
import com.example.data.db.ProjectEntity
import com.example.ui.theme.*

@Composable
fun ExplorerView(
    currentProject: ProjectEntity?,
    projects: List<ProjectEntity>,
    files: List<FileEntity>,
    activeFileId: String?,
    onSelectFile: (FileEntity) -> Unit,
    onSelectProject: (ProjectEntity) -> Unit,
    onCreateFile: (name: String, isFolder: Boolean) -> Unit,
    onDeleteFile: (FileEntity) -> Unit,
    onCreateProject: (name: String, description: String) -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showProjectDropdown by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<FileEntity?>(null) }
    var isNewFolderMode by remember { mutableStateOf(false) }

    Surface(
        color = VsCodeSidebar,
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Explorer Top Title Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "EXPLORER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeTextMuted,
                    letterSpacing = 1.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            isNewFolderMode = false
                            showNewFileDialog = true
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("explorer_new_file_btn")
                    ) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "New File", modifier = Modifier.size(16.dp), tint = VsCodeText)
                    }

                    IconButton(
                        onClick = {
                            isNewFolderMode = true
                            showNewFileDialog = true
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("explorer_new_folder_btn")
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", modifier = Modifier.size(16.dp), tint = VsCodeText)
                    }

                    IconButton(
                        onClick = onCloseSidebar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Collapse Sidebar", modifier = Modifier.size(18.dp), tint = VsCodeText)
                    }
                }
            }

            HorizontalDivider(color = VsCodeBorder, thickness = 1.dp)

            // Current Project Header (Collapsible / Switcher)
            Surface(
                color = VsCodeSidebar,
                onClick = { showProjectDropdown = !showProjectDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (showProjectDropdown) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                        contentDescription = "Toggle Projects",
                        tint = VsCodeTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentProject?.name?.uppercase() ?: "NO WORKSPACE",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Workspace", modifier = Modifier.size(14.dp), tint = VsCodeAccent)
                    }
                }
            }

            // Project Switcher List (Dropdown)
            AnimatedVisibility(visible = showProjectDropdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Workspaces",
                        style = MaterialTheme.typography.labelSmall,
                        color = VsCodeTextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    projects.forEach { proj ->
                        val isCurrent = proj.id == currentProject?.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectProject(proj)
                                    showProjectDropdown = false
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrent) Icons.Default.Check else Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = if (isCurrent) VsCodeAccent else VsCodeTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = proj.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrent) Color.White else VsCodeText,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalDivider(color = VsCodeBorder, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            // File Tree List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            ) {
                if (files.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Workspace is empty.\nTap + to create a file.",
                                style = MaterialTheme.typography.bodySmall,
                                color = VsCodeTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                items(files, key = { it.id }) { file ->
                    val isActive = file.id == activeFileId
                    FileTreeItem(
                        file = file,
                        isActive = isActive,
                        onClick = { onSelectFile(file) },
                        onDelete = { fileToDelete = file }
                    )
                }
            }
        }
    }

    // New File / Folder Dialog
    if (showNewFileDialog) {
        var inputName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = {
                Text(if (isNewFolderMode) "New Folder" else "New File")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        placeholder = { Text(if (isNewFolderMode) "e.g., components" else "e.g., script.js, style.css") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_file_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = inputName.trim()
                        if (trimmed.isNotEmpty()) {
                            onCreateFile(trimmed, isNewFolderMode)
                            showNewFileDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_file_btn")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Project Workspace Dialog
    if (showNewProjectDialog) {
        var projName by remember { mutableStateOf("") }
        var projDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Workspace") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        label = { Text("Workspace Name") },
                        placeholder = { Text("e.g., React App, Portfolio") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = projDesc,
                        onValueChange = { projDesc = it },
                        label = { Text("Description") },
                        placeholder = { Text("Brief description of workspace") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = projName.trim()
                        if (trimmed.isNotEmpty()) {
                            onCreateProject(trimmed, projDesc.trim())
                            showNewProjectDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Delete File Dialog
    fileToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete '${target.name}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFile(target)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FileTreeItem(
    file: FileEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 8.dp)
            .testTag("file_item_${file.name}")
    ) {
        // File Icon
        val (iconVector, iconTint) = getFileIconAndTint(file.name, file.isFolder)
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = file.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) Color.White else VsCodeText,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Git Status indicator ("M" or "U")
        if (file.gitStatus == "modified") {
            Text(
                text = "M",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GitModified,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        } else if (file.gitStatus == "added") {
            Text(
                text = "U",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GitAdded,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = VsCodeTextMuted,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

fun getFileIconAndTint(fileName: String, isFolder: Boolean): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    if (isFolder) {
        return Pair(Icons.Default.Folder, Color(0xFFDCDCAA))
    }
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "html", "htm" -> Pair(Icons.Default.Language, Color(0xFFE44D26))
        "css", "scss" -> Pair(Icons.Default.Palette, Color(0xFF264DE4))
        "js", "mjs" -> Pair(Icons.Default.Code, Color(0xFFF7DF1E))
        "ts", "tsx" -> Pair(Icons.Default.Code, Color(0xFF3178C6))
        "py" -> Pair(Icons.Default.Terminal, Color(0xFF3776AB))
        "kt", "kts" -> Pair(Icons.Default.DataObject, Color(0xFF7F52FF))
        "java" -> Pair(Icons.Default.Coffee, Color(0xFFB07219))
        "json" -> Pair(Icons.Default.DataArray, Color(0xFFF5A623))
        "md", "markdown" -> Pair(Icons.Default.Description, Color(0xFF42A5F5))
        "sh", "bash" -> Pair(Icons.Default.Terminal, Color(0xFF4EAA25))
        else -> Pair(Icons.Default.InsertDriveFile, VsCodeTextMuted)
    }
}
