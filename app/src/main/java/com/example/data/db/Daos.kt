package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastOpenedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("UPDATE projects SET lastOpenedAt = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET gitBranch = :branch WHERE id = :id")
    suspend fun updateBranch(id: String, branch: String)
}

@Dao
interface FileDao {
    @Query("SELECT * FROM files WHERE projectId = :projectId ORDER BY isFolder DESC, name ASC")
    fun getFilesByProject(projectId: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE projectId = :projectId AND isFolder = 0")
    suspend fun getCodeFiles(projectId: String): List<FileEntity>

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): FileEntity?

    @Query("SELECT * FROM files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: String, path: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("UPDATE files SET content = :content, updatedAt = :updatedAt, gitStatus = :gitStatus WHERE id = :id")
    suspend fun updateContent(id: String, content: String, updatedAt: Long = System.currentTimeMillis(), gitStatus: String = "modified")

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFileById(id: String)

    @Query("UPDATE files SET gitStatus = 'unmodified' WHERE projectId = :projectId")
    suspend fun resetAllGitStatus(projectId: String)
}

@Dao
interface GitDao {
    @Query("SELECT * FROM git_commits WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCommitsByProject(projectId: String): Flow<List<GitCommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: GitCommitEntity)

    @Query("DELETE FROM git_commits WHERE projectId = :projectId")
    suspend fun clearCommits(projectId: String)
}
