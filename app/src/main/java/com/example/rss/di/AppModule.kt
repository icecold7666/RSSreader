package com.example.rss.di

import android.content.Context
import com.example.rss.data.local.dao.ArticleDao
import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.local.database.AppDatabase
import com.example.rss.data.local.database.DatabaseFactory
import com.example.rss.data.remote.api.RetrofitClient
import com.example.rss.data.remote.api.RssApiService
import com.example.rss.data.remote.api.RssParser
import com.example.rss.data.repository.ArticleRepositoryImpl
import com.example.rss.data.repository.RssSourceRepositoryImpl
import com.example.rss.domain.repository.ArticleRepository
import com.example.rss.domain.repository.RssSourceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return DatabaseFactory.getDatabase(context)
    }

    @Provides
    fun provideRssSourceDao(database: AppDatabase): RssSourceDao = database.rssSourceDao()

    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao = database.articleDao()

    @Provides
    @Singleton
    fun provideRssParser(): RssParser = RssParser()

    @Provides
    @Singleton
    fun provideRssApiService(): RssApiService = RetrofitClient.getRssApiService()

    @Provides
    @Singleton
    fun provideRssSourceRepository(
        rssSourceDao: RssSourceDao
    ): RssSourceRepository = RssSourceRepositoryImpl(rssSourceDao)

    @Provides
    @Singleton
    fun provideArticleRepository(
        articleDao: ArticleDao,
        rssSourceDao: RssSourceDao
    ): ArticleRepository = ArticleRepositoryImpl(articleDao, rssSourceDao)
}
