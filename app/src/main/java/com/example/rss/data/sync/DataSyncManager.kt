package com.example.rss.data.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rss.work.RefreshWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DataSyncManager(
    private val context: Context
) {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun schedulePeriodicRefresh(intervalMinutes: Long = 60) {
        val request = PeriodicWorkRequestBuilder<RefreshWorker>(intervalMinutes, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "refresh_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelAllRefreshes() {
        WorkManager.getInstance(context).cancelUniqueWork("refresh_work")
        _isRefreshing.value = false
    }

    fun refreshNow() {
        _isRefreshing.value = true
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<RefreshWorker>().build())
    }
}
