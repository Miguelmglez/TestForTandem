package com.mmg.testfortandem.data.remote.api

import com.mmg.testfortandem.data.remote.dto.CommunityResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CommunityApi {

    /**
     * Fetches a single page of community members.
     * @param page the page number to fetch, starting from 1. The API returns 20 members per page, so page 1 returns members 1-20, page 2 returns members 21-40, etc.
     */
    @GET("api/community_{page}.json")
    suspend fun getCommunity(@Path("page") page: Int): CommunityResponseDto
}