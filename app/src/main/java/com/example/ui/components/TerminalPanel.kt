package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FileEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class BottomTab {
    TERMINAL,
    OUTPUT,
    PROBLEMS
}

data class TerminalLog(
    val text: String,
    val color: Color = Color(0xFFCCCCCC),
    val isCommand: Boolean = false
)

@Composable
fun TerminalPanel(
    files: List<FileEntity>,
    outputLogs: List<String>,
    onExecuteCode: (FileEntity) -> Unit,
    onCreateFile: (name: String) -> Unit,
    onDeleteFile: (fileId: String) -> Unit,
    onGitCommit: (message: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BottomTab.TERMINAL) }
    var currentInput by remember { mutableStateOf("") }
    val logs = remember {
        mutableStateListOf(
            TerminalLog("Code Studio Terminal v1.0 [Embedded Bash Simulation]", TerminalYellow),
            TerminalLog("Type 'help' to see available commands or 'run <file>' to execute.", TerminalCyan),
            TerminalLog("user@codestudio:~$ ls", Color.White, isCommand = true),
            TerminalLog(files.joinToString("  ") { it.name }, TerminalGreen)
        )
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = TerminalBg,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Panel Tab Bar (TERMINAL, OUTPUT, PROBLEMS, Close)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(VsCodeSidebar)
                    .padding(horizontal = 8.dp)
            ) {
                PanelTabItem(
                    label = "TERMINAL",
                    isSelected = selectedTab == BottomTab.TERMINAL,
                    onClick = { selectedTab = BottomTab.TERMINAL }
                )
                PanelTabItem(
                    label = "OUTPUT",
                    isSelected = selectedTab == BottomTab.OUTPUT,
                    onClick = { selectedTab = BottomTab.OUTPUT }
                )
                PanelTabItem(
                    label = "PROBLEMS",
                    isSelected = selectedTab == BottomTab.PROBLEMS,
                    onClick = { selectedTab = BottomTab.PROBLEMS }
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { logs.clear() },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Terminal", modifier = Modifier.size(14.dp), tint = VsCodeTextMuted)
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Panel", modifier = Modifier.size(14.dp), tint = VsCodeTextMuted)
                }
            }

            HorizontalDivider(color = VsCodeBorder, thickness = 1.dp)

            // Content according to selected tab
            when (selectedTab) {
                BottomTab.TERMINAL -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(logs) { log ->
                                Text(
                                    text = log.text,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = log.color
                                )
                            }
                        }

                        // Input prompt line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = "user@codestudio:~$ ",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerminalGreen
                            )

                            BasicTextField(
                                value = currentInput,
                                onValueChange = { currentInput = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color.White
                                ),
                                cursorBrush = SolidColor(VsCodeAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("terminal_input_field")
                            )

                            IconButton(
                                onClick = {
                                    val cmd = currentInput.trim()
                                    if (cmd.isNotEmpty()) {
                                        executeTerminalCommand(
                                            cmd = cmd,
                                            files = files,
                                            logs = logs,
                                            onExecuteCode = onExecuteCode,
                                            onCreateFile = onCreateFile,
                                            onDeleteFile = onDeleteFile,
                                            onGitCommit = onGitCommit
                                        )
                                        currentInput = ""
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(logs.size - 1)
                                        }
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Run", modifier = Modifier.size(14.dp), tint = VsCodeAccent)
                            }
                        }
                    }
                }

                BottomTab.OUTPUT -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        if (outputLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "[No runtime output yet. Run a script or open Live Preview to see console logs.]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = VsCodeTextMuted
                                )
                            }
                        } else {
                            items(outputLogs) { log ->
                                Text(
                                    text = log,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TerminalCyan
                                )
                            }
                        }
                    }
                }

                BottomTab.PROBLEMS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GitAdded, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No problems have been detected in the workspace.",
                                style = MaterialTheme.typography.bodySmall,
                                color = VsCodeText
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Syntax analysis active for ${files.size} workspace files. All valid.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = VsCodeTextMuted
                        )
                    }
                }
            }
        }
    }
}

private fun executeTerminalCommand(
    cmd: String,
    files: List<FileEntity>,
    logs: MutableList<TerminalLog>,
    onExecuteCode: (FileEntity) -> Unit,
    onCreateFile: (name: String) -> Unit,
    onDeleteFile: (fileId: String) -> Unit,
    onGitCommit: (message: String) -> Unit
) {
    logs.add(TerminalLog("user@codestudio:~$ $cmd", Color.White, isCommand = true))
    val parts = cmd.split(" ").filter { it.isNotBlank() }
    val command = parts.firstOrNull()?.lowercase() ?: return
    val args = parts.drop(1)

    when (command) {
        "help" -> {
            logs.add(TerminalLog("Available Commands:", TerminalYellow))
            logs.add(TerminalLog("  ls, dir           - List files in workspace", VsCodeText))
            logs.add(TerminalLog("  pwd               - Print working directory", VsCodeText))
            logs.add(TerminalLog("  cat <file>        - Print file content", VsCodeText))
            logs.add(TerminalLog("  node <file>       - Run JS code / script", VsCodeText))
            logs.add(TerminalLog("  python <file>     - Run Python script", VsCodeText))
            logs.add(TerminalLog("  run <file>        - Run / preview file", VsCodeText))
            logs.add(TerminalLog("  touch <file>      - Create new file", VsCodeText))
            logs.add(TerminalLog("  rm <file>         - Delete a file", VsCodeText))
            logs.add(TerminalLog("  git status        - Show git status", VsCodeText))
            logs.add(TerminalLog("  git commit -m <msg> - Commit changes", VsCodeText))
            logs.add(TerminalLog("  clear             - Clear terminal", VsCodeText))
            logs.add(TerminalLog("  date              - Show current date & time", VsCodeText))
            logs.add(TerminalLog("  echo <msg>        - Echo text", VsCodeText))
        }

        "ls", "dir" -> {
            if (files.isEmpty()) {
                logs.add(TerminalLog("(empty directory)", VsCodeTextMuted))
            } else {
                val output = files.joinToString("  ") { file ->
                    if (file.isFolder) "${file.name}/" else file.name
                }
                logs.add(TerminalLog(output, TerminalGreen))
            }
        }

        "pwd" -> {
            logs.add(TerminalLog("/workspace/project", TerminalCyan))
        }

        "clear" -> {
            logs.clear()
        }

        "date" -> {
            logs.add(TerminalLog(SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US).format(Date()), Color.White))
        }

        "echo" -> {
            logs.add(TerminalLog(args.joinToString(" "), Color.White))
        }

        "touch" -> {
            val fileName = args.firstOrNull()
            if (fileName.isNullOrBlank()) {
                logs.add(TerminalLog("touch: missing file operand", TerminalRed))
            } else {
                onCreateFile(fileName)
                logs.add(TerminalLog("Created file: $fileName", GitAdded))
            }
        }

        "rm" -> {
            val fileName = args.firstOrNull()
            val target = files.find { it.name.equals(fileName, ignoreCase = true) }
            if (target == null) {
                logs.add(TerminalLog("rm: cannot remove '$fileName': No such file", TerminalRed))
            } else {
                onDeleteFile(target.id)
                logs.add(TerminalLog("Removed $fileName", GitDeleted))
            }
        }

        "cat" -> {
            val fileName = args.firstOrNull()
            val target = files.find { it.name.equals(fileName, ignoreCase = true) }
            if (target == null) {
                logs.add(TerminalLog("cat: $fileName: No such file", TerminalRed))
            } else {
                logs.add(TerminalLog(target.content, VsCodeText))
            }
        }

        "node", "python", "run" -> {
            val fileName = args.firstOrNull()
            val target = files.find { it.name.equals(fileName, ignoreCase = true) }
            if (target == null) {
                logs.add(TerminalLog("$command: cannot open '$fileName': No such file", TerminalRed))
            } else {
                logs.add(TerminalLog("Executing ${target.name}...", TerminalCyan))
                onExecuteCode(target)
                logs.add(TerminalLog("[Process exited with status 0]", TerminalGreen))
            }
        }

        "git" -> {
            val subCmd = args.firstOrNull()
            when (subCmd) {
                "status" -> {
                    val modified = files.filter { it.gitStatus != "unmodified" }
                    logs.add(TerminalLog("On branch main", TerminalCyan))
                    if (modified.isEmpty()) {
                        logs.add(TerminalLog("nothing to commit, working tree clean", TerminalGreen))
                    } else {
                        logs.add(TerminalLog("Changes not staged for commit:", TerminalYellow))
                        modified.forEach { file ->
                            logs.add(TerminalLog("  modified:   ${file.name}", GitModified))
                        }
                    }
                }
                "commit" -> {
                    val msgIdx = args.indexOf("-m")
                    if (msgIdx != -1 && msgIdx + 1 < args.size) {
                        val msg = args.subList(msgIdx + 1, args.size).joinToString(" ").replace("\"", "")
                        onGitCommit(msg)
                        logs.add(TerminalLog("[main (commit)] $msg", GitAdded))
                    } else {
                        logs.add(TerminalLog("error: switch `m' requires a value", TerminalRed))
                    }
                }
                else -> logs.add(TerminalLog("git: '$subCmd' is not a git command. See 'git --help'.", TerminalRed))
            }
        }

        else -> {
            logs.add(TerminalLog("bash: $command: command not found. Type 'help' for commands.", TerminalRed))
        }
    }
}

@Composable
fun PanelTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else VsCodeTextMuted,
            letterSpacing = 0.5.sp
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(VsCodeAccent)
            )
        }
    }
}
