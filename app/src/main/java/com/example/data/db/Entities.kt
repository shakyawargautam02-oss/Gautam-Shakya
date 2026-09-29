package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val gitBranch: String = "main"
)

@Entity(
    tableName = "files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId", "path"], unique = true)]
)
data class FileEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val path: String, // e.g. "/index.html" or "/src/utils.js"
    val name: String,
    val content: String,
    val language: String, // "html", "javascript", "css", "python", "kotlin", "json", "markdown"
    val isFolder: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val gitStatus: String = "unmodified" // "unmodified", "modified", "added", "deleted"
)

@Entity(
    tableName = "git_commits",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class GitCommitEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val commitHash: String,
    val message: String,
    val author: String = "Developer <dev@codestudio.local>",
    val timestamp: Long = System.currentTimeMillis(),
    val filesChangedCount: Int = 1
)
