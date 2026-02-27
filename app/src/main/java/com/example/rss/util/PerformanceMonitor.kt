package com.example.rss.util

import android.os.SystemClock
import android.util.Log

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private const val COLD_START_TARGET_MS = 2000L
    private const val PARSE_100_TARGET_MS = 3000L
    private const val MEMORY_TARGET_MB = 200L

    fun reportColdStart(processStartElapsedMs: Long) {
        val elapsed = SystemClock.elapsedRealtime() - processStartElapsedMs
        Log.i(TAG, "cold_start_ms=$elapsed target_ms=$COLD_START_TARGET_MS")
    }

    fun reportParse(sourceId: Long, articleCount: Int, durationMs: Long) {
        Log.i(TAG, "parse_ms=$durationMs article_count=$articleCount source_id=$sourceId")
        if (articleCount >= 100 && durationMs > PARSE_100_TARGET_MS) {
            Log.w(TAG, "parse_100_budget_exceeded duration_ms=$durationMs target_ms=$PARSE_100_TARGET_MS")
        }
    }

    fun reportMemory() {
        val rt = Runtime.getRuntime()
        val usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)
        Log.i(TAG, "heap_used_mb=$usedMb target_mb=$MEMORY_TARGET_MB")
        if (usedMb > MEMORY_TARGET_MB) {
            Log.w(TAG, "memory_budget_exceeded heap_used_mb=$usedMb target_mb=$MEMORY_TARGET_MB")
        }
    }
}
