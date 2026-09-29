package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.FileEntity
import com.example.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPreviewDialog(
    files: List<FileEntity>,
    activeFile: FileEntity?,
    onLogCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isConsoleOpen by remember { mutableStateOf(false) }
    val consoleLogs = remember { mutableStateListOf<String>() }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Prepare complete HTML bundle with embedded CSS and JS
    val bundledHtml = remember(files, activeFile) {
        val htmlFile = files.find { it.name.endsWith(".html") } ?: activeFile
        val cssFiles = files.filter { it.name.endsWith(".css") }
        val jsFiles = files.filter { it.name.endsWith(".js") }

        val baseHtml = if (htmlFile != null && htmlFile.name.endsWith(".html")) {
            htmlFile.content
        } else {
            """<!DOCTYPE html>
<html>
<head><title>Code Studio Preview</title></head>
<body>
  <h2>Output for ${activeFile?.name ?: "Current File"}</h2>
  <pre style="background:#222;color:#fff;padding:12px;border-radius:8px;">${activeFile?.content ?: "No content"}</pre>
</body>
</html>"""
        }

        // Inline CSS
        val combinedCss = cssFiles.joinToString("\n") { it.content }
        // Inline JS
        val combinedJs = jsFiles.joinToString("\n") { it.content }

        // Inject console bridge & CSS & JS
        val injectedScript = """
<script>
(function() {
  var oldLog = console.log;
  console.log = function() {
    var args = Array.prototype.slice.call(arguments);
    var msg = args.map(function(a) { 
      return typeof a === 'object' ? JSON.stringify(a) : String(a); 
    }).join(' ');
    oldLog.apply(console, arguments);
  };
})();
</script>
"""
        var result = baseHtml
        if (combinedCss.isNotEmpty()) {
            result = result.replace("</head>", "<style>\n$combinedCss\n</style>\n</head>")
        }
        if (combinedJs.isNotEmpty()) {
            result = result.replace("</body>", "$injectedScript\n<script>\n$combinedJs\n</script>\n</body>")
        }
        result
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Browser URL Bar Header
                Surface(
                    color = VsCodeSidebar,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close Preview", tint = VsCodeText)
                        }

                        // Simulated URL Address Bar
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GitAdded, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "http://localhost:3000/${activeFile?.name ?: "index.html"}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = VsCodeText
                                )
                            }
                        }

                        IconButton(
                            onClick = { webViewRef?.reload() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = VsCodeText)
                        }

                        IconButton(
                            onClick = { isConsoleOpen = !isConsoleOpen },
                            modifier = Modifier.size(32.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (consoleLogs.isNotEmpty()) {
                                        Badge { Text("${consoleLogs.size}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = "Developer Console", tint = if (isConsoleOpen) VsCodeAccent else VsCodeText)
                            }
                        }
                    }
                }

                HorizontalDivider(color = VsCodeBorder, thickness = 1.dp)

                // WebView Display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewRef = this
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowContentAccess = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                        message?.message()?.let { msg ->
                                            val formatted = "[${message.messageLevel()}] $msg"
                                            consoleLogs.add(formatted)
                                            onLogCaptured(formatted)
                                        }
                                        return super.onConsoleMessage(message)
                                    }
                                }
                                webViewClient = WebViewClient()
                                loadDataWithBaseURL(
                                    "http://localhost:3000/",
                                    bundledHtml,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            }
                        },
                        update = { webView ->
                            webViewRef = webView
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("preview_webview")
                    )
                }

                // Collapsible Interactive Console Log Viewer
                AnimatedVisibility(visible = isConsoleOpen) {
                    Surface(
                        color = TerminalBg,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VsCodeSidebar)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "BROWSER CONSOLE (${consoleLogs.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = TerminalGreen
                                )
                                IconButton(
                                    onClick = { consoleLogs.clear() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = VsCodeTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                if (consoleLogs.isEmpty()) {
                                    item {
                                        Text(
                                            text = "> Ready. Tap buttons in the web preview to trigger events.",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = VsCodeTextMuted
                                        )
                                    }
                                } else {
                                    items(consoleLogs) { log ->
                                        Text(
                                            text = "> $log",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = TerminalCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
