package com.example.rss.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * RSS API服务接口
 */
interface RssApiService {

    /**
     * 获取RSS Feed内容
     * @param url RSS源URL
     * @return ResponseBody 响应体
     */
    @GET
    suspend fun fetchRssFeed(@Url url: String): ResponseBody

    /**
     * 获取Atom Feed内容
     * @param url Atom源URL
     * @return ResponseBody 响应体
     */
    @GET
    suspend fun fetchAtomFeed(@Url url: String): ResponseBody

    /**
     * 获取JSON Feed内容（现代RSS格式）
     * @param url JSON源URL
     * @return ResponseBody 响应体
     */
    @GET
    suspend fun fetchJsonFeed(@Url url: String): ResponseBody
}