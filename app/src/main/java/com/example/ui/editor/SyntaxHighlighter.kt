package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.*
import java.util.regex.Pattern

class CodeVisualTransformation(private val language: String) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = highlightCode(text.text, language)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }

    companion object {
        fun highlightCode(code: String, language: String): AnnotatedString {
            if (code.isEmpty()) return AnnotatedString("")

            val builder = AnnotatedString.Builder(code)
            val len = code.length

            when (language.lowercase()) {
                "javascript", "typescript", "js", "ts" -> highlightJs(builder, code)
                "html", "xml", "htm" -> highlightHtml(builder, code)
                "css", "scss" -> highlightCss(builder, code)
                "python", "py" -> highlightPython(builder, code)
                "kotlin", "kt", "kts", "java" -> highlightKotlin(builder, code)
                "json" -> highlightJson(builder, code)
                "markdown", "md" -> highlightMarkdown(builder, code)
                else -> highlightGeneric(builder, code)
            }

            return builder.toAnnotatedString()
        }

        private fun highlightJs(builder: AnnotatedString.Builder, text: String) {
            val keywords = "\\b(async|await|break|case|catch|class|const|continue|debugger|default|delete|do|else|export|extends|finally|for|function|get|if|import|in|instanceof|let|new|null|of|return|set|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield|true|false)\\b"
            val numbers = "\\b\\d+(\\.\\d+)?\\b"
            val strings = "\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'|`(\\\\.|[^`\\\\])*`"
            val comments = "//.*|/\\*[\\s\\S]*?\\*/"
            val functions = "\\b([a-zA-Z_\$][a-zA-Z0-9_\$]*)(?=\\s*\\()"
            val classes = "\\b[A-Z][a-zA-Z0-9_\$]*\\b"

            applyPattern(builder, text, comments, SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic))
            applyPattern(builder, text, strings, SpanStyle(color = SyntaxString))
            applyPattern(builder, text, numbers, SpanStyle(color = SyntaxNumber))
            applyPattern(builder, text, keywords, SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.SemiBold))
            applyPattern(builder, text, functions, SpanStyle(color = SyntaxFunction))
            applyPattern(builder, text, classes, SpanStyle(color = SyntaxType))
        }

        private fun highlightPython(builder: AnnotatedString.Builder, text: String) {
            val keywords = "\\b(and|as|assert|async|await|break|class|continue|def|del|elif|else|except|False|finally|for|from|global|if|import|in|is|lambda|None|nonlocal|not|or|pass|raise|return|True|try|while|with|yield|self)\\b"
            val numbers = "\\b\\d+(\\.\\d+)?\\b"
            val strings = "\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'"
            val comments = "#.*"
            val functions = "\\b([a-zA-Z_]\\w*)(?=\\s*\\()"
            val classes = "\\b[A-Z]\\w*\\b"

            applyPattern(builder, text, comments, SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic))
            applyPattern(builder, text, strings, SpanStyle(color = SyntaxString))
            applyPattern(builder, text, numbers, SpanStyle(color = SyntaxNumber))
            applyPattern(builder, text, keywords, SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.SemiBold))
            applyPattern(builder, text, functions, SpanStyle(color = SyntaxFunction))
            applyPattern(builder, text, classes, SpanStyle(color = SyntaxType))
        }

        private fun highlightKotlin(builder: AnnotatedString.Builder, text: String) {
            val keywords = "\\b(as|as\\?|break|class|continue|do|else|false|for|fun|if|in|!in|interface|is|!is|null|object|package|return|super|this|throw|true|try|typealias|val|var|when|while|by|catch|constructor|delegate|dynamic|field|file|finally|get|import|init|param|property|receiver|set|setparam|where|actual|abstract|annotation|companion|const|crossinline|data|enum|expect|external|final|infix|inline|inner|internal|lateinit|noinline|open|operator|out|override|private|protected|public|reified|sealed|suspend|tailrec|vararg)\\b"
            val numbers = "\\b\\d+(\\.\\d+)?[fFL]?\\b"
            val strings = "\"\"\"[\\s\\S]*?\"\"\"|\"(\\\\.|[^\"\\\\])*\""
            val comments = "//.*|/\\*[\\s\\S]*?\\*/"
            val functions = "\\b([a-zA-Z_]\\w*)(?=\\s*\\()"
            val types = "\\b[A-Z]\\w*\\b"

            applyPattern(builder, text, comments, SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic))
            applyPattern(builder, text, strings, SpanStyle(color = SyntaxString))
            applyPattern(builder, text, numbers, SpanStyle(color = SyntaxNumber))
            applyPattern(builder, text, keywords, SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.SemiBold))
            applyPattern(builder, text, functions, SpanStyle(color = SyntaxFunction))
            applyPattern(builder, text, types, SpanStyle(color = SyntaxType))
        }

        private fun highlightHtml(builder: AnnotatedString.Builder, text: String) {
            val tags = "</?[a-zA-Z0-9\\-]+|/?>"
            val attributes = "\\b[a-zA-Z0-9\\-]+(?=\\=)"
            val strings = "\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'"
            val comments = "<!--[\\s\\S]*?-->"

            applyPattern(builder, text, comments, SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic))
            applyPattern(builder, text, tags, SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold))
            applyPattern(builder, text, attributes, SpanStyle(color = SyntaxProperty))
            applyPattern(builder, text, strings, SpanStyle(color = SyntaxString))
        }

        private fun highlightCss(builder: AnnotatedString.Builder, text: String) {
            val properties = "\\b[a-zA-Z\\-]+(?=\\s*:)"
            val values = "#[a-fA-F0-9]{3,8}|\\b\\d+(px|em|rem|%|vh|vw|s|ms)?\\b"
            val selectors = "[.#]?[a-zA-Z0-9\\-_]+(?=\\s*\\{)"
            val comments = "/\\*[\\s\\S]*?\\*/"
            val atRules = "@[a-zA-Z\\-]+"

            applyPattern(builder, text, comments, SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic))
            applyPattern(builder, text, selectors, SpanStyle(color = SyntaxFunction))
            applyPattern(builder, text, atRules, SpanStyle(color = SyntaxKeyword))
            applyPattern(builder, text, properties, SpanStyle(color = SyntaxProperty))
            applyPattern(builder, text, values, SpanStyle(color = SyntaxNumber))
        }

        private fun highlightJson(builder: AnnotatedString.Builder, text: String) {
            val keys = "\"(\\\\.|[^\"\\\\])*\"(?=\\s*:)"
            val strings = ":\\s*(\"(\\\\.|[^\"\\\\])*\")"
            val numbers = "\\b-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b"
            val booleans = "\\b(true|false|null)\\b"

            applyPattern(builder, text, keys, SpanStyle(color = SyntaxProperty, fontWeight = FontWeight.Medium))
            applyPattern(builder, text, strings, SpanStyle(color = SyntaxString))
            applyPattern(builder, text, numbers, SpanStyle(color = SyntaxNumber))
            applyPattern(builder, text, booleans, SpanStyle(color = SyntaxKeyword))
        }

        private fun highlightMarkdown(builder: AnnotatedString.Builder, text: String) {
            val headers = "^#{1,6}\\s.*$"
            val bold = "\\*\\*.*?\\*\\*|__.*?__"
            val italic = "\\*.*?\\*|_.*?_"
            val codeInline = "`.*?`"
            val lists = "^\\s*[-*+]\\s|\\d+\\.\\s"

            applyPattern(builder, text, headers, SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold))
            applyPattern(builder, text, bold, SpanStyle(fontWeight = FontWeight.Bold))
            applyPattern(builder, text, italic, SpanStyle(fontStyle = FontStyle.Italic))
            applyPattern(builder, text, codeInline, SpanStyle(color = SyntaxString, background = Color(0xFF282828)))
            applyPattern(builder, text, lists, SpanStyle(color = SyntaxProperty))
        }

        private fun highlightGeneric(builder: AnnotatedString.Builder, text: String) {
            val numbers = "\\b\\d+(\\.\\d+)?\\b"
            val strings = "\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'"
            val comments = "//.*|#.*"
            applyPattern(builder, text, comments, SpanStyle(color = SyntaxComment))
            applyPattern(builder, text, strings, SpanStyle(color = SyntaxString))
            applyPattern(builder, text, numbers, SpanStyle(color = SyntaxNumber))
        }

        private fun applyPattern(
            builder: AnnotatedString.Builder,
            text: String,
            regexStr: String,
            style: SpanStyle
        ) {
            try {
                val pattern = Pattern.compile(regexStr, Pattern.MULTILINE)
                val matcher = pattern.matcher(text)
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    if (start in 0 until text.length && end <= text.length && start < end) {
                        builder.addStyle(style, start, end)
                    }
                }
            } catch (_: Exception) {
                // Ignore regex compilation or matching issues gracefully
            }
        }
    }
}
