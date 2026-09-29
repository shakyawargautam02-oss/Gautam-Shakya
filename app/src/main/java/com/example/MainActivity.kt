package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.editor.CodeEditorView
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

            CodeStudioTheme(ideTheme = currentTheme) {
                CodeStudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CodeStudioApp(viewModel: MainViewModel) {
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val currentFiles by viewModel.currentFiles.collectAsStateWithLifecycle()
    val commits by viewModel.commits.collectAsStateWithLifecycle()

    val openTabs by viewModel.openTabs.collectAsStateWithLifecycle()
    val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
    val editorTextValue by viewModel.editorTextValue.collectAsStateWithLifecycle()

    val dirtyFileIds by viewModel.dirtyFileIds.collectAsStateWithLifecycle()
    val currentActivityPanel by viewModel.currentActivityPanel.collectAsStateWithLifecycle()
    val isTerminalOpen by viewModel.isTerminalOpen.collectAsStateWithLifecycle()
    val outputLogs by viewModel.outputLogs.collectAsStateWithLifecycle()

    val isPreviewOpen by viewModel.isPreviewOpen.collectAsStateWithLifecycle()
    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsStateWithLifecycle()
    val isFindReplaceOpen by viewModel.isFindReplaceOpen.collectAsStateWithLifecycle()

    val currentLine by viewModel.currentLine.collectAsStateWithLifecycle()
    val currentCol by viewModel.currentCol.collectAsStateWithLifecycle()

    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val showLineNumbers by viewModel.showLineNumbers.collectAsStateWithLifecycle()
    val wordWrap by viewModel.wordWrap.collectAsStateWithLifecycle()
    val autoSave by viewModel.autoSave.collectAsStateWithLifecycle()

    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()

    val modifiedCount = remember(currentFiles) {
        currentFiles.count { it.gitStatus != "unmodified" }
    }

    // Handle Android system Back button
    BackHandler(enabled = currentActivityPanel != null || isTerminalOpen || isPreviewOpen || isCommandPaletteOpen) {
        when {
            isPreviewOpen -> viewModel.setPreviewOpen(false)
            isCommandPaletteOpen -> viewModel.setCommandPaletteOpen(false)
            isTerminalOpen -> viewModel.toggleTerminal()
            currentActivityPanel != null -> viewModel.selectActivityPanel(null)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            // Top VS Code Menu & Breadcrumb Bar
            Surface(
                color = VsCodeSidebar,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Code Studio Icon",
                        tint = VsCodeAccent,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Code Studio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "  •  ${currentProject?.name ?: "No Project"} ${if (activeFile != null) "> " + activeFile?.name else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VsCodeTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Quick Top Action Icons
                    IconButton(
                        onClick = { viewModel.setPreviewOpen(true) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Run App",
                            tint = GitAdded,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleTerminal() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Toggle Terminal",
                            tint = if (isTerminalOpen) VsCodeAccent else VsCodeText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setCommandPaletteOpen(true) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardCommandKey,
                            contentDescription = "Command Palette",
                            tint = VsCodeText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Authentic Bottom Status Bar
            StatusBar(
                branchName = currentProject?.gitBranch ?: "main",
                currentLine = currentLine,
                currentCol = currentCol,
                language = activeFile?.language ?: "plaintext",
                onToggleTerminal = { viewModel.toggleTerminal() },
                onOpenCommandPalette = { viewModel.setCommandPaletteOpen(true) }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. VS Code Left Activity Bar
            ActivityBar(
                currentPanel = currentActivityPanel,
                onSelectPanel = { viewModel.selectActivityPanel(it) },
                modifiedFilesCount = modifiedCount,
                onOpenCommandPalette = { viewModel.setCommandPaletteOpen(true) }
            )

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(VsCodeBorder)
            )

            // 2. Collapsible Side Panel (Explorer, Search, Git, Extensions/Settings)
            AnimatedVisibility(
                visible = currentActivityPanel != null,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it }
            ) {
                Row(modifier = Modifier.fillMaxHeight()) {
                    when (currentActivityPanel) {
                        ActivityPanel.EXPLORER -> {
                            ExplorerView(
                                currentProject = currentProject,
                                projects = allProjects,
                                files = currentFiles,
                                activeFileId = activeFile?.id,
                                onSelectFile = { viewModel.openFile(it) },
                                onSelectProject = { viewModel.selectProject(it) },
                                onCreateFile = { name, isFolder -> viewModel.createFile(name, isFolder) },
                                onDeleteFile = { viewModel.deleteFile(it) },
                                onCreateProject = { name, desc -> viewModel.createProject(name, desc) },
                                onCloseSidebar = { viewModel.selectActivityPanel(null) }
                            )
                        }

                        ActivityPanel.SEARCH -> {
                            SearchPanel(
                                files = currentFiles,
                                onSelectMatch = { file, matchIdx ->
                                    viewModel.openFile(file)
                                },
                                onReplaceAll = { s, r -> viewModel.replaceAllInWorkspace(s, r) },
                                onCloseSidebar = { viewModel.selectActivityPanel(null) }
                            )
                        }

                        ActivityPanel.SOURCE_CONTROL -> {
                            SourceControlView(
                                branchName = currentProject?.gitBranch ?: "main",
                                files = currentFiles,
                                commits = commits,
                                onCommit = { viewModel.commitGitChanges(it) },
                                onSelectFile = { viewModel.openFile(it) },
                                onChangeBranch = { viewModel.switchGitBranch(it) },
                                onCloseSidebar = { viewModel.selectActivityPanel(null) }
                            )
                        }

                        ActivityPanel.RUNNER -> {
                            // Quick runner launcher panel
                            Surface(
                                color = VsCodeSidebar,
                                modifier = Modifier
                                    .width(260.dp)
                                    .fillMaxHeight()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("RUN & DEBUG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VsCodeTextMuted)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.setPreviewOpen(true) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Launch Web Live Preview")
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.selectActivityPanel(null)
                                            if (!isTerminalOpen) viewModel.toggleTerminal()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Run in Terminal")
                                    }
                                }
                            }
                        }

                        ActivityPanel.TERMINAL -> {
                            LaunchedEffect(Unit) {
                                if (!isTerminalOpen) viewModel.toggleTerminal()
                                viewModel.selectActivityPanel(null)
                            }
                        }

                        ActivityPanel.EXTENSIONS, ActivityPanel.SETTINGS -> {
                            ExtensionsAndSettingsPanel(
                                currentTheme = currentTheme,
                                onSelectTheme = { viewModel.setTheme(it) },
                                fontSize = fontSize,
                                onFontSizeChange = { viewModel.setFontSize(it) },
                                showLineNumbers = showLineNumbers,
                                onToggleLineNumbers = { viewModel.toggleLineNumbers() },
                                wordWrap = wordWrap,
                                onToggleWordWrap = { viewModel.toggleWordWrap() },
                                autoSave = autoSave,
                                onToggleAutoSave = { viewModel.toggleAutoSave() },
                                onCloseSidebar = { viewModel.selectActivityPanel(null) }
                            )
                        }

                        null -> Unit
                    }

                    // Vertical divider between panel and editor
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(VsCodeBorder)
                    )
                }
            }

            // 3. Center Area: Tabs + Code Editor + Terminal
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Editor Tabs Bar
                EditorTabsBar(
                    openFiles = openTabs,
                    activeFileId = activeFile?.id,
                    dirtyFileIds = dirtyFileIds,
                    onSelectTab = { viewModel.openFile(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onSaveCurrentFile = { viewModel.saveCurrentFile() },
                    onRunOrPreview = { viewModel.setPreviewOpen(true) },
                    onToggleFindReplace = { viewModel.toggleFindReplace() },
                    onFormatCode = { viewModel.formatCode() },
                    onCloseAllTabs = { viewModel.closeAllTabs() }
                )

                HorizontalDivider(color = VsCodeBorder, thickness = 1.dp)

                // Breadcrumbs Bar
                if (activeFile != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "${currentProject?.name ?: "workspace"} > ${activeFile?.path?.removePrefix("/") ?: ""}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = VsCodeTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(color = VsCodeBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                }

                // Code Editor Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (activeFile != null) {
                        CodeEditorView(
                            textValue = editorTextValue,
                            onValueChange = { viewModel.onEditorTextChanged(it) },
                            language = activeFile?.language ?: "plaintext",
                            fontSizeSp = fontSize,
                            showLineNumbers = showLineNumbers,
                            wordWrap = wordWrap,
                            isFindReplaceOpen = isFindReplaceOpen,
                            onCloseFindReplace = { viewModel.toggleFindReplace() },
                            onCursorPositionChanged = { line, col -> viewModel.setCursorPosition(line, col) },
                            onFormatCode = { viewModel.formatCode() },
                            onUndo = { viewModel.undo() },
                            onRedo = { viewModel.redo() },
                            canUndo = canUndo,
                            canRedo = canRedo
                        )
                    } else {
                        // Empty Welcome / No File open view (Iconic VS Code Welcome screen)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = VsCodeAccent,
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = "Code Studio",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Editing evolved for Android",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VsCodeTextMuted
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.selectActivityPanel(ActivityPanel.EXPLORER)
                                        }
                                    ) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open Explorer")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.createFile("index.html")
                                        }
                                    ) {
                                        Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("New File")
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Bottom Integrated Terminal / Output Panel
                AnimatedVisibility(visible = isTerminalOpen) {
                    TerminalPanel(
                        files = currentFiles,
                        outputLogs = outputLogs,
                        onExecuteCode = { targetFile ->
                            viewModel.logTerminalOutput("[Runner] Executed ${targetFile.name} successfully.")
                            if (targetFile.name.endsWith(".html")) {
                                viewModel.setPreviewOpen(true)
                            }
                        },
                        onCreateFile = { name -> viewModel.createFile(name) },
                        onDeleteFile = { id ->
                            currentFiles.find { it.id == id }?.let { viewModel.deleteFile(it) }
                        },
                        onGitCommit = { msg -> viewModel.commitGitChanges(msg) },
                        onClose = { viewModel.toggleTerminal() }
                    )
                }
            }
        }
    }

    // Web Live Runner Dialog
    if (isPreviewOpen) {
        WebPreviewDialog(
            files = currentFiles,
            activeFile = activeFile,
            onLogCaptured = { logMsg -> viewModel.logTerminalOutput(logMsg) },
            onDismiss = { viewModel.setPreviewOpen(false) }
        )
    }

    // Command Palette Modal
    if (isCommandPaletteOpen) {
        val commands = listOf(
            CommandItem("Format Document", "Editor", "Shift+Alt+F", Icons.Default.FormatAlignLeft) {
                viewModel.formatCode()
            },
            CommandItem("Run / Live Preview", "Workbench", "Ctrl+F5", Icons.Default.PlayArrow) {
                viewModel.setPreviewOpen(true)
            },
            CommandItem("Toggle Integrated Terminal", "View", "Ctrl+`", Icons.Default.Terminal) {
                viewModel.toggleTerminal()
            },
            CommandItem("Find and Replace", "Editor", "Ctrl+F", Icons.Default.Search) {
                viewModel.toggleFindReplace()
            },
            CommandItem("Save File", "File", "Ctrl+S", Icons.Default.Save) {
                viewModel.saveCurrentFile()
            },
            CommandItem("Git: Commit All Changes", "Source Control", "Ctrl+Enter", Icons.Default.AccountTree) {
                viewModel.selectActivityPanel(ActivityPanel.SOURCE_CONTROL)
            },
            CommandItem("View: Show Explorer", "View", "Ctrl+Shift+E", Icons.Default.Folder) {
                viewModel.selectActivityPanel(ActivityPanel.EXPLORER)
            },
            CommandItem("Preferences: Change Color Theme", "Preferences", null, Icons.Default.Palette) {
                viewModel.selectActivityPanel(ActivityPanel.SETTINGS)
            },
            CommandItem("Toggle Word Wrap", "View", "Alt+Z", Icons.Default.WrapText) {
                viewModel.toggleWordWrap()
            },
            CommandItem("Toggle Line Numbers", "View", null, Icons.Default.FormatListNumbered) {
                viewModel.toggleLineNumbers()
            },
            CommandItem("Close All Tabs", "View", "Ctrl+K Ctrl+W", Icons.Default.CloseFullscreen) {
                viewModel.closeAllTabs()
            }
        )

        CommandPaletteDialog(
            commands = commands,
            onDismiss = { viewModel.setCommandPaletteOpen(false) }
        )
    }
}
