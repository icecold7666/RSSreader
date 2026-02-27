package com.example.rss.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端配置
 */
object RetrofitClient {

    private const val BASE_URL = "https://example.com" // 实际使用时不需要固定URL

    /**
     * 创建OkHttpClient实例
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                // 添加User-Agent
                val request = chain.request()
                    .newBuilder()
                    .addHeader("User-Agent", "RSSReader/1.0")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     * 创建Retrofit实例
     */
    fun create(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * 获取RssApiService实例
     */
    fun getRssApiService(): RssApiService {
        return create().create(RssApiService::class.java)
    }
}