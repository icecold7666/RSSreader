package com.example.rss.util

import android.content.Context
import com.example.rss.data.local.database.AppDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseBackupManager {
    private val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun createBackup(context: Context): File {
        val dbName = AppDatabase.DATABASE_NAME
        val dbFile = context.getDatabasePath(dbName)
        require(dbFile.exists()) { "数据库文件不存在" }

        val backupDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "backup").apply {
            if (!exists()) mkdirs()
        }
        val timestamp = formatter.format(Date())
        val target = File(backupDir, "rss_backup_$timestamp.db")
        dbFile.copyTo(target, overwrite = true)

        val wal = File(dbFile.parentFile, "$dbName-wal")
        if (wal.exists()) {
            wal.copyTo(File(backupDir, "rss_backup_$timestamp.db-wal"), overwrite = true)
        }
        val shm = File(dbFile.parentFile, "$dbName-shm")
        if (shm.exists()) {
            shm.copyTo(File(backupDir, "rss_backup_$timestamp.db-shm"), overwrite = true)
        }
        return target
    }
}
