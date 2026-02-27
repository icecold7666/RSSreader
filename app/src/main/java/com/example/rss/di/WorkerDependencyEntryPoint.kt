package com.example.rss.di

import com.example.rss.data.local.dao.ArticleDao
import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.remote.api.RssApiService
import com.example.rss.data.remote.api.RssParser
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerDependencyEntryPoint {
    fun rssSourceDao(): RssSourceDao
    fun articleDao(): ArticleDao
    fun rssApiService(): RssApiService
    fun rssParser(): RssParser
}
