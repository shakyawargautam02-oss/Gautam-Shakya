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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeTheme
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

data class ExtensionMock(
    val name: String,
    val publisher: String,
    val description: String,
    val downloads: String,
    var isInstalled: Boolean
)

@Composable
fun ExtensionsAndSettingsPanel(
    currentTheme: IdeTheme,
    onSelectTheme: (IdeTheme) -> Unit,
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    showLineNumbers: Boolean,
    onToggleLineNumbers: () -> Unit,
    wordWrap: Boolean,
    onToggleWordWrap: () -> Unit,
    autoSave: Boolean,
    onToggleAutoSave: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Settings, 1: Extensions

    val extensions = remember {
        mutableStateListOf(
            ExtensionMock("Prettier - Code Formatter", "Prettier", "Code formatter using opinionated formatting rules", "42M", true),
            ExtensionMock("Python", "Microsoft", "IntelliSense, linting, debugging, code formatting", "98M", true),
            ExtensionMock("ESLint", "Microsoft", "Integrates ESLint JavaScript into VS Code", "31M", true),
            ExtensionMock("GitLens — Git supercharged", "GitKraken", "Supercharge Git within VS Code", "28M", false),
            ExtensionMock("Tailwind CSS IntelliSense", "Tailwind Labs", "Intelligent Tailwind CSS tooling", "15M", false),
            ExtensionMock("Material Icon Theme", "Philipp Kief", "Material Design Icons for VS Code", "22M", true)
        )
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
                    text = if (selectedTab == 0) "SETTINGS" else "EXTENSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeTextMuted,
                    letterSpacing = 1.sp
                )

                IconButton(onClick = onCloseSidebar, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Close", modifier = Modifier.size(18.dp), tint = VsCodeText)
                }
            }

            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = VsCodeSidebar,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Preferences", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Marketplace", fontSize = 12.sp) }
                )
            }

            HorizontalDivider(color = VsCodeBorder, thickness = 1.dp)

            if (selectedTab == 0) {
                // Settings tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme Selector
                    item {
                        Text("Color Theme", style = MaterialTheme.typography.labelMedium, color = VsCodeTextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        IdeTheme.values().forEach { theme ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (theme == currentTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { onSelectTheme(theme) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = theme == currentTheme,
                                    onClick = { onSelectTheme(theme) },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(theme.displayName, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
                        }
                    }

                    // Font Size Slider
                    item {
                        Text("Font Size: ${fontSize.toInt()} sp", style = MaterialTheme.typography.labelMedium, color = VsCodeTextMuted)
                        Slider(
                            value = fontSize,
                            onValueChange = onFontSizeChange,
                            valueRange = 10f..22f,
                            steps = 11,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Toggles
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Show Line Numbers", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Switch(checked = showLineNumbers, onCheckedChange = { onToggleLineNumbers() })
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Word Wrap", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Switch(checked = wordWrap, onCheckedChange = { onToggleWordWrap() })
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Auto Save", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Switch(checked = autoSave, onCheckedChange = { onToggleAutoSave() })
                        }
                    }
                }
            } else {
                // Extensions Marketplace
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(extensions) { ext ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ext.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text(ext.publisher, fontSize = 11.sp, color = VsCodeTextMuted)
                                    }
                                    Button(
                                        onClick = { ext.isInstalled = !ext.isInstalled },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (ext.isInstalled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(if (ext.isInstalled) "Installed" else "Install", fontSize = 10.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(ext.description, fontSize = 11.sp, color = VsCodeText, maxLines = 2)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("⬇ ${ext.downloads}", fontSize = 10.sp, color = VsCodeTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
