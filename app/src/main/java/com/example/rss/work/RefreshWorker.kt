package com.example.rss.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.rss.data.local.entity.RssSourceEntity
import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.service.ContentFetchService
import com.example.rss.di.WorkerDependencyEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

/**
 * 刷新Worker
 * 定期刷新RSS源的文章内容
 */
class RefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val entryPoint: WorkerDependencyEntryPoint =
        EntryPointAccessors.fromApplication(applicationContext, WorkerDependencyEntryPoint::class.java)

    private val rssSourceDao: RssSourceDao = entryPoint.rssSourceDao()
    private val contentFetchService: ContentFetchService by lazy {
        ContentFetchService(
            rssSourceDao = entryPoint.rssSourceDao(),
            articleDao = entryPoint.articleDao(),
            rssApiService = entryPoint.rssApiService(),
            rssParser = entryPoint.rssParser()
        )
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d("RefreshWorker", "开始刷新RSS源")

            // 获取需要刷新的源
            val sources = getSourcesToRefresh()
            if (sources.isEmpty()) {
                Log.d("RefreshWorker", "没有需要刷新的RSS源")
                return Result.success()
            }

            // 批量刷新
            var successCount = 0
            var totalCount = sources.size

            coroutineScope {
                sources.forEach { source ->
                    try {
                        val result = contentFetchService.fetchArticlesForSource(source.id)
                        if (result.isSuccess) {
                            successCount++
                            Log.d("RefreshWorker", "成功刷新源: ${source.title}")
                        } else {
                            Log.w("RefreshWorker", "刷新源失败: ${source.title}, 错误: ${result.exceptionOrNull()?.message}")
                        }

                        // 更新最后刷新时间
                        rssSourceDao.updateLastUpdate(source.id)
                    } catch (e: Exception) {
                        Log.e("RefreshWorker", "刷新源时发生异常: ${source.title}", e)
                    }
                }
            }

            Log.d("RefreshWorker", "刷新完成: $successCount/$totalCount")

            // 如果成功刷新了至少一个源，返回成功
            if (successCount > 0) {
                Result.success()
            } else {
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e("RefreshWorker", "刷新过程中发生异常", e)
            Result.failure()
        }
    }

    /**
     * 获取需要刷新的RSS源
     */
    private suspend fun getSourcesToRefresh(): List<RssSourceEntity> {
        return rssSourceDao.getActiveSources().firstOrNull() ?: emptyList()
    }

    /**
     * 创建刷新请求
     */
    companion object {
        private const val TAG = "RefreshWorker"

        /**
         * 创建定期刷新请求
         */
        fun createPeriodicRequest(
            repeatInterval: Long,
            repeatIntervalTimeUnit: TimeUnit
        ): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<RefreshWorker>(
                repeatInterval,
                repeatIntervalTimeUnit
            )
            .setConstraints(getConstraints())
            .setInitialDelay(1, TimeUnit.MINUTES) // 首次延迟1分钟
            .addTag(TAG)
            .build()
        }

        /**
         * 创建一次性刷新请求
         */
        fun createOneTimeRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RefreshWorker>()
            .setConstraints(getConstraints())
            .setInitialDelay(1, TimeUnit.MINUTES) // 首次延迟1分钟
            .addTag(TAG)
            .build()
        }

        /**
         * 获取工作约束
         */
        private fun getConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络连接
                .setRequiresBatteryNotLow(true) // 电池电量不能低
                .setRequiresStorageNotLow(true) // 存储空间不能低
                .build()
        }
    }
}
