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
import com.example.data.db.FileEntity
import com.example.ui.theme.*

data class SearchMatch(
    val file: FileEntity,
    val lineNumber: Int,
    val lineText: String,
    val matchIndex: Int
)

@Composable
fun SearchPanel(
    files: List<FileEntity>,
    onSelectMatch: (FileEntity, matchIndex: Int) -> Unit,
    onReplaceAll: (search: String, replace: String) -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var isReplaceExpanded by remember { mutableStateOf(false) }
    var matchCase by remember { mutableStateOf(false) }

    val matches = remember(searchQuery, files, matchCase) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val list = mutableListOf<SearchMatch>()
            for (file in files) {
                if (file.isFolder) continue
                val lines = file.content.split('\n')
                var charOffset = 0
                for ((lineIdx, line) in lines.withIndex()) {
                    var idx = line.indexOf(searchQuery, 0, ignoreCase = !matchCase)
                    while (idx >= 0) {
                        list.add(
                            SearchMatch(
                                file = file,
                                lineNumber = lineIdx + 1,
                                lineText = line.trim(),
                                matchIndex = charOffset + idx
                            )
                        )
                        idx = line.indexOf(searchQuery, idx + 1, ignoreCase = !matchCase)
                    }
                    charOffset += line.length + 1
                }
            }
            list
        }
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
                    text = "SEARCH",
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

            // Search inputs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isReplaceExpanded = !isReplaceExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isReplaceExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                            contentDescription = "Toggle Replace",
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search (in workspace)", fontSize = 12.sp) },
                        singleLine = true,
                        trailingIcon = {
                            FilterChip(
                                selected = matchCase,
                                onClick = { matchCase = !matchCase },
                                label = { Text("Aa", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.height(26.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("workspace_search_input")
                    )
                }

                AnimatedVisibility(visible = isReplaceExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 24.dp)
                    ) {
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Replace", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        Button(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    onReplaceAll(searchQuery, replaceQuery)
                                }
                            },
                            enabled = matches.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.FindReplace, contentDescription = "Replace All", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Results count
            if (searchQuery.isNotBlank()) {
                Text(
                    text = "${matches.size} results in ${matches.map { it.file.id }.distinct().size} files",
                    style = MaterialTheme.typography.labelSmall,
                    color = VsCodeTextMuted,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Results list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            ) {
                val groupedByFile = matches.groupBy { it.file }
                groupedByFile.forEach { (file, fileMatches) ->
                    item(key = "header_${file.id}") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            val (icon, tint) = getFileIconAndTint(file.name, false)
                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Badge {
                                Text("${fileMatches.size}")
                            }
                        }
                    }

                    items(fileMatches) { match ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectMatch(match.file, match.matchIndex) }
                                .padding(start = 24.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "${match.lineNumber}: ",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = VsCodeGutter
                            )
                            Text(
                                text = match.lineText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = VsCodeText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
