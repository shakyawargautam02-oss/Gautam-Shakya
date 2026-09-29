package com.example.data.repository

import com.example.data.db.*
import com.example.data.sample.SampleProjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class WorkspaceRepository(
    private val projectDao: ProjectDao,
    private val fileDao: FileDao,
    private val gitDao: GitDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun getFilesForProject(projectId: String): Flow<List<FileEntity>> {
        return fileDao.getFilesByProject(projectId)
    }

    fun getCommitsForProject(projectId: String): Flow<List<GitCommitEntity>> {
        return gitDao.getCommitsByProject(projectId)
    }

    suspend fun ensureInitialData() {
        val projects = allProjects.first()
        if (projects.isEmpty()) {
            val (initialProjects, initialFiles, initialCommits) = SampleProjects.createInitialProjects()
            for (proj in initialProjects) {
                projectDao.insertProject(proj)
            }
            fileDao.insertFiles(initialFiles)
            for (commit in initialCommits) {
                gitDao.insertCommit(commit)
            }
        }
    }

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun createProject(name: String, description: String): ProjectEntity {
        val project = ProjectEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            lastOpenedAt = System.currentTimeMillis()
        )
        projectDao.insertProject(project)

        // Add a default README.md
        val readme = FileEntity(
            id = UUID.randomUUID().toString(),
            projectId = project.id,
            path = "/README.md",
            name = "README.md",
            content = "# $name\n\n$description\n\nWelcome to your new workspace in Code Studio!",
            language = "markdown",
            gitStatus = "added"
        )
        fileDao.insertFile(readme)
        return project
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.deleteProject(project)
    }

    suspend fun updateProjectLastOpened(id: String) {
        projectDao.updateLastOpened(id)
    }

    suspend fun getFileById(id: String): FileEntity? = fileDao.getFileById(id)

    suspend fun getCodeFiles(projectId: String): List<FileEntity> = fileDao.getCodeFiles(projectId)

    suspend fun saveFileContent(fileId: String, content: String) {
        fileDao.updateContent(fileId, content)
    }

    suspend fun createFile(
        projectId: String,
        name: String,
        parentPath: String = "/",
        initialContent: String = "",
        isFolder: Boolean = false
    ): FileEntity {
        val normalizedPath = if (parentPath.endsWith("/")) "$parentPath$name" else "$parentPath/$name"
        val language = detectLanguage(name)
        val file = FileEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            path = normalizedPath,
            name = name,
            content = initialContent,
            language = language,
            isFolder = isFolder,
            gitStatus = "added"
        )
        fileDao.insertFile(file)
        return file
    }

    suspend fun deleteFile(fileId: String) {
        fileDao.deleteFileById(fileId)
    }

    suspend fun commitChanges(projectId: String, message: String): GitCommitEntity {
        val files = fileDao.getCodeFiles(projectId)
        val modifiedCount = files.count { it.gitStatus != "unmodified" }.coerceAtLeast(1)
        
        val shortHash = UUID.randomUUID().toString().substring(0, 7)
        val commit = GitCommitEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            commitHash = shortHash,
            message = message,
            timestamp = System.currentTimeMillis(),
            filesChangedCount = modifiedCount
        )
        gitDao.insertCommit(commit)
        fileDao.resetAllGitStatus(projectId)
        return commit
    }

    private fun detectLanguage(filename: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "html", "htm" -> "html"
            "js", "mjs", "cjs" -> "javascript"
            "ts", "tsx" -> "typescript"
            "css", "scss", "sass" -> "css"
            "py", "pyw" -> "python"
            "kt", "kts" -> "kotlin"
            "java" -> "java"
            "json" -> "json"
            "md", "markdown" -> "markdown"
            "sh", "bash" -> "shell"
            "xml" -> "html"
            else -> "text"
        }
    }
}
