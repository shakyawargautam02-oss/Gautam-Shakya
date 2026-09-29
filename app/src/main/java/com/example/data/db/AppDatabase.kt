package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        FileEntity::class,
        GitCommitEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun fileDao(): FileDao
    abstract fun gitDao(): GitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codestudio.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
