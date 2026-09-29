package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CodeEditorView(
    textValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    language: String,
    fontSizeSp: Float,
    showLineNumbers: Boolean,
    wordWrap: Boolean,
    isFindReplaceOpen: Boolean,
    onCloseFindReplace: () -> Unit,
    onCursorPositionChanged: (line: Int, col: Int) -> Unit,
    onFormatCode: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentMatchIdx by remember { mutableIntStateOf(0) }

    // Track line & column
    LaunchedEffect(textValue.selection) {
        val cursor = textValue.selection.start
        val text = textValue.text
        if (cursor <= text.length) {
            val textBeforeCursor = text.substring(0, cursor)
            val line = textBeforeCursor.count { it == '\n' } + 1
            val col = cursor - textBeforeCursor.lastIndexOf('\n')
            onCursorPositionChanged(line, col)
        }
    }

    // Search matches calculation
    LaunchedEffect(searchQuery, textValue.text) {
        if (searchQuery.isNotEmpty() && textValue.text.contains(searchQuery, ignoreCase = true)) {
            val list = mutableListOf<Int>()
            var idx = textValue.text.indexOf(searchQuery, 0, ignoreCase = true)
            while (idx >= 0) {
                list.add(idx)
                idx = textValue.text.indexOf(searchQuery, idx + 1, ignoreCase = true)
            }
            matchIndices = list
            if (currentMatchIdx >= list.size) {
                currentMatchIdx = 0
            }
        } else {
            matchIndices = emptyList()
            currentMatchIdx = 0
        }
    }

    val visualTransformation = remember(language) {
        CodeVisualTransformation(language)
    }

    val linesCount = remember(textValue.text) {
        textValue.text.count { it == '\n' } + 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Floating Find & Replace Bar
        AnimatedVisibility(visible = isFindReplaceOpen) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Find...", fontSize = 12.sp) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("find_input")
                        )

                        Text(
                            text = if (matchIndices.isNotEmpty()) "${currentMatchIdx + 1}/${matchIndices.size}" else "0/0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(
                            onClick = {
                                if (matchIndices.isNotEmpty()) {
                                    currentMatchIdx = (currentMatchIdx - 1 + matchIndices.size) % matchIndices.size
                                    val matchStart = matchIndices[currentMatchIdx]
                                    onValueChange(textValue.copy(selection = TextRange(matchStart, matchStart + searchQuery.length)))
                                }
                            },
                            enabled = matchIndices.isNotEmpty(),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous Match", modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                if (matchIndices.isNotEmpty()) {
                                    currentMatchIdx = (currentMatchIdx + 1) % matchIndices.size
                                    val matchStart = matchIndices[currentMatchIdx]
                                    onValueChange(textValue.copy(selection = TextRange(matchStart, matchStart + searchQuery.length)))
                                }
                            },
                            enabled = matchIndices.isNotEmpty(),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Match", modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = onCloseFindReplace,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Find", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Replace...", fontSize = 12.sp) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("replace_input")
                        )

                        Button(
                            onClick = {
                                if (matchIndices.isNotEmpty()) {
                                    val matchStart = matchIndices[currentMatchIdx]
                                    val newText = textValue.text.replaceRange(matchStart, matchStart + searchQuery.length, replaceQuery)
                                    onValueChange(textValue.copy(text = newText, selection = TextRange(matchStart + replaceQuery.length)))
                                }
                            },
                            enabled = matchIndices.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Replace", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val newText = textValue.text.replace(searchQuery, replaceQuery, ignoreCase = true)
                                    onValueChange(textValue.copy(text = newText))
                                }
                            },
                            enabled = matchIndices.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("All", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Code Editor Body
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Line Numbers Gutter
                if (showLineNumbers) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .width(42.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        for (i in 1..linesCount) {
                            Text(
                                text = "$i",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.45f).sp,
                                    color = VsCodeGutter
                                )
                            )
                        }
                    }

                    // Vertical Divider line
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(VsCodeBorder)
                    )
                }

                // Text Field Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        .then(if (!wordWrap) Modifier.horizontalScroll(horizontalScrollState) else Modifier)
                ) {
                    BasicTextField(
                        value = textValue,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.45f).sp,
                            color = VsCodeText
                        ),
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(VsCodeAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 400.dp)
                            .testTag("code_editor_field")
                    )
                }
            }
        }

        // Mobile Developer Accessory / Symbol Toolbar (Fast coding keys!)
        CodeAccessoryToolbar(
            onInsertText = { symbol ->
                val currentText = textValue.text
                val selection = textValue.selection
                val start = selection.min
                val end = selection.max
                val newText = currentText.replaceRange(start, end, symbol)
                val newCursor = start + symbol.length
                onValueChange(
                    textValue.copy(
                        text = newText,
                        selection = TextRange(newCursor, newCursor)
                    )
                )
            },
            onUndo = onUndo,
            onRedo = onRedo,
            canUndo = canUndo,
            canRedo = canRedo,
            onFormat = onFormatCode
        )
    }
}

@Composable
fun CodeAccessoryToolbar(
    onInsertText: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onFormat: () -> Unit
) {
    val scrollState = rememberScrollState()
    val quickSymbols = listOf(
        "  ", // Tab
        "{", "}", "(", ")", "[", "]",
        ";", ":", "=", "=>", "<", ">",
        "\"", "'", "`",
        "/", "\\", "!", "?",
        "$", "&", "|", "+", "-", "*",
        "//", "/*", "*/"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp)
        ) {
            // Undo / Redo / Format buttons
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onFormat,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.FormatAlignLeft,
                    contentDescription = "Format Document",
                    tint = VsCodeAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(VsCodeBorder)
                    .padding(horizontal = 2.dp)
            )

            // Symbol keys
            quickSymbols.forEach { sym ->
                val displayLabel = if (sym == "  ") "TAB" else sym
                Surface(
                    onClick = { onInsertText(sym) },
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 3.dp, vertical = 5.dp)
                        .height(34.dp)
                        .widthIn(min = 32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = displayLabel,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = if (displayLabel == "TAB") 11.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
