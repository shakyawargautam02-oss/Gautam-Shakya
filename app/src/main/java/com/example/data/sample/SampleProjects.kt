package com.example.data.sample

import com.example.data.db.FileEntity
import com.example.data.db.GitCommitEntity
import com.example.data.db.ProjectEntity
import java.util.UUID

object SampleProjects {

    fun createInitialProjects(): Triple<List<ProjectEntity>, List<FileEntity>, List<GitCommitEntity>> {
        val webProjectId = "proj_web_dev"
        val pyProjectId = "proj_python_algo"
        val ktProjectId = "proj_kotlin_dev"

        val projects = listOf(
            ProjectEntity(
                id = webProjectId,
                name = "Web App (HTML/CSS/JS)",
                description = "Modern interactive web application with live preview and DOM manipulation",
                gitBranch = "main"
            ),
            ProjectEntity(
                id = pyProjectId,
                name = "Python Algorithms",
                description = "Classic sorting algorithms, Fibonacci sequence, and JSON configuration",
                gitBranch = "master"
            ),
            ProjectEntity(
                id = ktProjectId,
                name = "Kotlin Utilities",
                description = "Idiomatic Kotlin data structures and extension functions",
                gitBranch = "main"
            )
        )

        val files = mutableListOf<FileEntity>()

        // Web Project Files
        files.add(
            FileEntity(
                id = UUID.randomUUID().toString(),
                projectId = webProjectId,
                path = "/index.html",
                name = "index.html",
                language = "html",
                content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Code Studio Preview</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="card">
    <div class="badge">VS Code for Android</div>
    <h1 id="title">Code Studio</h1>
    <p class="subtitle">Edit HTML, CSS, &amp; JavaScript live on your mobile device!</p>
    
    <div class="counter-box">
      <button id="decrementBtn" class="btn secondary">-</button>
      <span id="counterValue">0</span>
      <button id="incrementBtn" class="btn primary">+</button>
    </div>

    <div class="actions">
      <button id="colorBtn" class="btn accent">Change Theme</button>
      <button id="logBtn" class="btn secondary">Log Diagnostics</button>
    </div>

    <div id="outputLog" class="terminal-log">
      Console Ready &gt;_
    </div>
  </div>

  <script src="app.js"></script>
</body>
</html>
""".trimIndent()
            )
        )

        files.add(
            FileEntity(
                id = UUID.randomUUID().toString(),
                projectId = webProjectId,
                path = "/style.css",
                name = "style.css",
                language = "css",
                content = """/* Code Studio Modern Stylesheet */
:root {
  --bg-color: #12141a;
  --card-bg: #1e222d;
  --primary: #007acc;
  --primary-hover: #0098ff;
  --accent: #9d4edd;
  --text-main: #f0f2f5;
  --text-muted: #8b949e;
  --border-color: #30363d;
}

body {
  margin: 0;
  padding: 20px;
  background-color: var(--bg-color);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 90vh;
}

.card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 24px;
  max-width: 420px;
  width: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
}

.badge {
  display: inline-block;
  background: rgba(0, 122, 204, 0.2);
  color: #4fc3f7;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 12px;
}

h1 {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 700;
}

.subtitle {
  color: var(--text-muted);
  font-size: 13px;
  margin-top: 0;
  margin-bottom: 20px;
}

.counter-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin: 20px 0;
  background: rgba(255, 255, 255, 0.03);
  padding: 14px;
  border-radius: 12px;
}

#counterValue {
  font-size: 32px;
  font-weight: bold;
  color: #00bcd4;
  min-width: 48px;
  text-align: center;
}

.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.btn {
  flex: 1;
  padding: 10px 14px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn.primary { background: var(--primary); color: #fff; }
.btn.secondary { background: #2d333b; color: #adbac7; }
.btn.accent { background: var(--accent); color: #fff; }

.terminal-log {
  background: #0d1117;
  border: 1px solid #21262d;
  border-radius: 8px;
  padding: 10px;
  font-family: monospace;
  font-size: 12px;
  color: #58a6ff;
  min-height: 24px;
}
""".trimIndent()
            )
        )

        files.add(
            FileEntity(
                id = UUID.randomUUID().toString(),
                projectId = webProjectId,
                path = "/app.js",
                name = "app.js",
                language = "javascript",
                content = """// Code Studio Interactive Script
let count = 0;
const counterEl = document.getElementById('counterValue');
const logEl = document.getElementById('outputLog');

function updateDisplay() {
  counterEl.textContent = count;
  console.log(`[STATE] Counter updated to: ` + count);
  if (count > 0) {
    counterEl.style.color = '#73C991';
  } else if (count < 0) {
    counterEl.style.color = '#F48771';
  } else {
    counterEl.style.color = '#00BCD4';
  }
}

document.getElementById('incrementBtn')?.addEventListener('click', () => {
  count++;
  updateDisplay();
  logEl.textContent = `> Counter incremented to ` + count;
});

document.getElementById('decrementBtn')?.addEventListener('click', () => {
  count--;
  updateDisplay();
  logEl.textContent = `> Counter decremented to ` + count;
});

const colors = ['#9d4edd', '#007acc', '#2ea44f', '#d29922', '#f85149'];
let colorIndex = 0;
document.getElementById('colorBtn')?.addEventListener('click', () => {
  colorIndex = (colorIndex + 1) % colors.length;
  const picked = colors[colorIndex];
  document.querySelector('.card').style.borderColor = picked;
  logEl.textContent = `> Accent color switched to ` + picked;
  console.log('Switched accent theme color: ' + picked);
});

document.getElementById('logBtn')?.addEventListener('click', () => {
  const info = {
    app: 'Code Studio Mobile',
    timestamp: new Date().toLocaleTimeString(),
    screen: window.innerWidth + 'x' + window.innerHeight
  };
  console.log('App diagnostics:', JSON.stringify(info));
  logEl.textContent = `> Diagnostics sent to console: ` + info.timestamp;
});

console.log('Code Studio Web App initialized successfully!');
""".trimIndent()
            )
        )

        files.add(
            FileEntity(
                id = UUID.randomUUID().toString(),
                projectId = webProjectId,
                path = "/README.md",
                name = "README.md",
                language = "markdown",
                content = """# Code Studio Mobile Workspace
A full-featured mobile code editor inspired by Visual Studio Code.

## Features
- **Multi-Tab Editing**: Switch between open files effortlessly
- **Syntax Highlighting**: Rich token coloring for JS, HTML, CSS, Python, Kotlin, and JSON
- **Interactive Runner**: Test web apps in real-time with WebView and live JS console
- **Integrated Terminal**: Execute simulated commands (`ls`, `cat`, `node`, `git status`)
- **Source Control**: Stage files, write commit messages, and browse commit logs
- **Mobile Symbol Toolbar**: Fast typing with one-tap braces, quotes, and punctuation

*Created with Jetpack Compose & Material 3.*
""".trimIndent()
            )
        )

        // Python Project Files
        files.add(
            FileEntity(
                id = UUID.randomUUID().toString(),
                projectId = pyProjectId,
                path = "/algorithms.py",
                name = "algorithms.py",
                language = "python",
                content = """# Python Algorithms Suite
def quick_sort(arr):
    \"\"\"Divide and conquer quicksort algorithm.\"\"\"
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quick_sort(left) + middle + quick_sort(right)

def fibonacci_memo(n, memo=None):
    \"\"\"Fibonacci sequence with memoization.\"\"\"
    if memo is None:
        memo = {}
    if n in memo:
        return memo[n]
    if n <= 1:
        return n
    memo[n] = fibonacci_memo(n - 1, memo) + fibonacci_memo(n - 2, memo)
    return memo[n]

if __name__ == "__main__":
    sample_data = [64, 34, 25, 12, 22, 11, 90]
    sorted_data = quick_sort(sample_data)
    print(f"Original: {sample_data}")
    print(f"QuickSorted: {sorted_data}")
    print(f"Fibonacci(15): {fibonacci_memo(15)}")
""".trimIndent()
            )
        )

        files.add(
            FileEntity(
                id = UUID.randomUUID().toString(),
                projectId = pyProjectId,
                path = "/config.json",
                name = "config.json",
                language = "json",
                content = """{
  "name": "algorithms-suite",
  "version": "1.0.0",
  "author": "Code Studio Developer",
  "settings": {
    "optimize": true,
    "maxIterations": 1000,
    "debugMode": false
  },
  "supportedLanguages": [
    "python",
    "javascript",
    "kotlin"
  ]
}
""".trimIndent()
            )
        )

        // Kotlin Project Files
        files.add(
            FileEntity(
                id = UUID.randomUUID().toString(),
                projectId = ktProjectId,
                path = "/MathUtils.kt",
                name = "MathUtils.kt",
                language = "kotlin",
                content = """package com.codestudio.utils

/**
 * Idiomatic Kotlin utility functions for calculations.
 */
object MathUtils {

    fun isPrime(number: Int): Boolean {
        if (number <= 1) return false
        for (i in 2..Math.sqrt(number.toDouble()).toInt()) {
            if (number % i == 0) return false
        }
        return true
    }

    fun List<Int>.calculateMedian(): Double {
        if (isEmpty()) return 0.0
        val sorted = this.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[middle].toDouble()
        } else {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        }
    }
}

fun main() {
    val numbers = listOf(5, 2, 9, 1, 7, 6)
    println("Numbers: " + numbers)
    println("Median: " + with(MathUtils) { numbers.calculateMedian() })
    println("Is 29 Prime? " + MathUtils.isPrime(29))
}
""".trimIndent()
            )
        )

        val commits = listOf(
            GitCommitEntity(
                id = UUID.randomUUID().toString(),
                projectId = webProjectId,
                commitHash = "8a3f1b4",
                message = "feat: initial commit with responsive card and interactive scripts",
                filesChangedCount = 4,
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            GitCommitEntity(
                id = UUID.randomUUID().toString(),
                projectId = pyProjectId,
                commitHash = "3c9d2e1",
                message = "feat: implement quicksort and memoized fibonacci",
                filesChangedCount = 2,
                timestamp = System.currentTimeMillis() - 43200000L
            )
        )

        return Triple(projects, files, commits)
    }
}
