package com.example.ui

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.WorkspaceRepository
import com.example.ui.components.ActivityPanel
import com.example.ui.theme.IdeTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Stack

data class EditorUndoState(
    val text: String,
    val selection: TextRange
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = WorkspaceRepository(
        projectDao = database.projectDao(),
        fileDao = database.fileDao(),
        gitDao = database.gitDao()
    )

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _currentFiles = MutableStateFlow<List<FileEntity>>(emptyList())
    val currentFiles: StateFlow<List<FileEntity>> = _currentFiles.asStateFlow()

    private val _commits = MutableStateFlow<List<GitCommitEntity>>(emptyList())
    val commits: StateFlow<List<GitCommitEntity>> = _commits.asStateFlow()

    // Open tabs & active file
    private val _openTabs = MutableStateFlow<List<FileEntity>>(emptyList())
    val openTabs: StateFlow<List<FileEntity>> = _openTabs.asStateFlow()

    private val _activeFile = MutableStateFlow<FileEntity?>(null)
    val activeFile: StateFlow<FileEntity?> = _activeFile.asStateFlow()

    // Editor content & undo/redo
    var editorTextValue = MutableStateFlow(TextFieldValue(""))
        private set

    private val undoStack = Stack<EditorUndoState>()
    private val redoStack = Stack<EditorUndoState>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Dirty file IDs (unsaved files)
    private val _dirtyFileIds = MutableStateFlow<Set<String>>(emptySet())
    val dirtyFileIds: StateFlow<Set<String>> = _dirtyFileIds.asStateFlow()

    // Activity bar & side panels
    private val _currentActivityPanel = MutableStateFlow<ActivityPanel?>(ActivityPanel.EXPLORER)
    val currentActivityPanel: StateFlow<ActivityPanel?> = _currentActivityPanel.asStateFlow()

    // Terminal bottom panel
    private val _isTerminalOpen = MutableStateFlow(false)
    val isTerminalOpen: StateFlow<Boolean> = _isTerminalOpen.asStateFlow()

    // Console logs (captured from web runner & terminal)
    private val _outputLogs = MutableStateFlow<List<String>>(emptyList())
    val outputLogs: StateFlow<List<String>> = _outputLogs.asStateFlow()

    // Dialogs
    private val _isPreviewOpen = MutableStateFlow(false)
    val isPreviewOpen: StateFlow<Boolean> = _isPreviewOpen.asStateFlow()

    private val _isCommandPaletteOpen = MutableStateFlow(false)
    val isCommandPaletteOpen: StateFlow<Boolean> = _isCommandPaletteOpen.asStateFlow()

    private val _isFindReplaceOpen = MutableStateFlow(false)
    val isFindReplaceOpen: StateFlow<Boolean> = _isFindReplaceOpen.asStateFlow()

    // Cursor position in active file
    private val _currentLine = MutableStateFlow(1)
    val currentLine: StateFlow<Int> = _currentLine.asStateFlow()

    private val _currentCol = MutableStateFlow(1)
    val currentCol: StateFlow<Int> = _currentCol.asStateFlow()

    // Preferences
    private val _currentTheme = MutableStateFlow(IdeTheme.VS_CODE_DARK)
    val currentTheme: StateFlow<IdeTheme> = _currentTheme.asStateFlow()

    private val _fontSize = MutableStateFlow(13f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _showLineNumbers = MutableStateFlow(true)
    val showLineNumbers: StateFlow<Boolean> = _showLineNumbers.asStateFlow()

    private val _wordWrap = MutableStateFlow(false)
    val wordWrap: StateFlow<Boolean> = _wordWrap.asStateFlow()

    private val _autoSave = MutableStateFlow(true)
    val autoSave: StateFlow<Boolean> = _autoSave.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialData()

            // Observe projects and select the first project by default
            allProjects.collect { projects ->
                if (projects.isNotEmpty() && _currentProject.value == null) {
                    selectProject(projects.first())
                }
            }
        }
    }

    fun selectProject(project: ProjectEntity) {
        _currentProject.value = project
        viewModelScope.launch {
            repository.updateProjectLastOpened(project.id)

            launch {
                repository.getFilesForProject(project.id).collect { files ->
                    _currentFiles.value = files
                    // If no file active, open the first file (like index.html or first code file)
                    if (_activeFile.value == null && files.isNotEmpty()) {
                        val firstFile = files.find { !it.isFolder } ?: files.first()
                        openFile(firstFile)
                    }
                }
            }

            launch {
                repository.getCommitsForProject(project.id).collect { commits ->
                    _commits.value = commits
                }
            }
        }
    }

    fun openFile(file: FileEntity) {
        if (file.isFolder) return

        // Auto-save previous file if dirty
        if (_autoSave.value) {
            saveCurrentFile()
        }

        // Add to open tabs if not present
        val currentTabs = _openTabs.value.toMutableList()
        if (currentTabs.none { it.id == file.id }) {
            currentTabs.add(file)
            _openTabs.value = currentTabs
        }

        _activeFile.value = file
        editorTextValue.value = TextFieldValue(file.content, selection = TextRange(0, 0))

        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
    }

    fun closeTab(file: FileEntity) {
        val currentTabs = _openTabs.value.toMutableList()
        val index = currentTabs.indexOfFirst { it.id == file.id }
        if (index != -1) {
            currentTabs.removeAt(index)
            _openTabs.value = currentTabs

            if (_activeFile.value?.id == file.id) {
                if (currentTabs.isNotEmpty()) {
                    val nextIndex = (index - 1).coerceAtLeast(0)
                    openFile(currentTabs[nextIndex])
                } else {
                    _activeFile.value = null
                    editorTextValue.value = TextFieldValue("")
                }
            }
        }
    }

    fun closeAllTabs() {
        if (_autoSave.value) {
            saveCurrentFile()
        }
        _openTabs.value = emptyList()
        _activeFile.value = null
        editorTextValue.value = TextFieldValue("")
    }

    fun onEditorTextChanged(newValue: TextFieldValue) {
        val previous = editorTextValue.value
        if (previous.text != newValue.text) {
            undoStack.push(EditorUndoState(previous.text, previous.selection))
            redoStack.clear()
            _canUndo.value = true
            _canRedo.value = false

            // Mark dirty
            _activeFile.value?.let { file ->
                val dirty = _dirtyFileIds.value.toMutableSet()
                dirty.add(file.id)
                _dirtyFileIds.value = dirty

                // Auto save if enabled
                if (_autoSave.value) {
                    viewModelScope.launch {
                        repository.saveFileContent(file.id, newValue.text)
                    }
                }
            }
        }
        editorTextValue.value = newValue
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = editorTextValue.value
            redoStack.push(EditorUndoState(current.text, current.selection))
            val previous = undoStack.pop()
            editorTextValue.value = TextFieldValue(previous.text, previous.selection)
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = editorTextValue.value
            undoStack.push(EditorUndoState(current.text, current.selection))
            val next = redoStack.pop()
            editorTextValue.value = TextFieldValue(next.text, next.selection)
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
        }
    }

    fun saveCurrentFile() {
        val file = _activeFile.value ?: return
        val content = editorTextValue.value.text
        viewModelScope.launch {
            repository.saveFileContent(file.id, content)
            val dirty = _dirtyFileIds.value.toMutableSet()
            dirty.remove(file.id)
            _dirtyFileIds.value = dirty
        }
    }

    fun formatCode() {
        val text = editorTextValue.value.text
        if (text.isBlank()) return

        // Simple opinionated code formatter: cleans whitespace, fixes brace indentation
        val lines = text.split("\n")
        val formatted = StringBuilder()
        var indentLevel = 0
        val indentStr = "  "

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                formatted.append("\n")
                continue
            }

            if (trimmed.startsWith("}") || trimmed.startsWith("]") || trimmed.startsWith(")")) {
                indentLevel = (indentLevel - 1).coerceAtLeast(0)
            }

            for (i in 0 until indentLevel) {
                formatted.append(indentStr)
            }
            formatted.append(trimmed).append("\n")

            if (trimmed.endsWith("{") || trimmed.endsWith("[") || trimmed.endsWith("(")) {
                indentLevel++
            }
        }

        val result = formatted.toString().trimEnd() + "\n"
        onEditorTextChanged(TextFieldValue(result, TextRange(result.length)))
    }

    fun createFile(name: String, isFolder: Boolean = false) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val file = repository.createFile(proj.id, name, isFolder = isFolder)
            if (!isFolder) {
                openFile(file)
            }
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file.id)
            closeTab(file)
        }
    }

    fun createProject(name: String, description: String) {
        viewModelScope.launch {
            val newProj = repository.createProject(name, description)
            selectProject(newProj)
        }
    }

    fun commitGitChanges(message: String) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            repository.commitChanges(proj.id, message)
            logTerminalOutput("[Git] Committed with message: $message")
        }
    }

    fun switchGitBranch(newBranch: String) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            database.projectDao().updateBranch(proj.id, newBranch)
            _currentProject.value = proj.copy(gitBranch = newBranch)
            logTerminalOutput("[Git] Switched to branch '$newBranch'")
        }
    }

    fun replaceAllInWorkspace(search: String, replace: String) {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val files = repository.getCodeFiles(proj.id)
            for (file in files) {
                if (file.content.contains(search, ignoreCase = true)) {
                    val updated = file.content.replace(search, replace, ignoreCase = true)
                    repository.saveFileContent(file.id, updated)
                    if (_activeFile.value?.id == file.id) {
                        editorTextValue.value = TextFieldValue(updated)
                    }
                }
            }
        }
    }

    fun logTerminalOutput(msg: String) {
        val logs = _outputLogs.value.toMutableList()
        logs.add(msg)
        _outputLogs.value = logs
    }

    fun selectActivityPanel(panel: ActivityPanel?) {
        _currentActivityPanel.value = panel
    }

    fun toggleTerminal() {
        _isTerminalOpen.value = !_isTerminalOpen.value
    }

    fun setPreviewOpen(open: Boolean) {
        _isPreviewOpen.value = open
    }

    fun setCommandPaletteOpen(open: Boolean) {
        _isCommandPaletteOpen.value = open
    }

    fun toggleFindReplace() {
        _isFindReplaceOpen.value = !_isFindReplaceOpen.value
    }

    fun setCursorPosition(line: Int, col: Int) {
        _currentLine.value = line
        _currentCol.value = col
    }

    fun setTheme(theme: IdeTheme) {
        _currentTheme.value = theme
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
    }

    fun toggleLineNumbers() {
        _showLineNumbers.value = !_showLineNumbers.value
    }

    fun toggleWordWrap() {
        _wordWrap.value = !_wordWrap.value
    }

    fun toggleAutoSave() {
        _autoSave.value = !_autoSave.value
    }
}
