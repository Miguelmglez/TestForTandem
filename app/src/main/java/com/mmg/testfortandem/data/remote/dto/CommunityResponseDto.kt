package com.mmg.testfortandem.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * The DTO representing the API response for a community members request.
 * CommunityResponseDto is a Wrapper for the list of community members, along with any error information.
 */
@Serializable
data class CommunityResponseDto(
    val response: List<CommunityMemberDto>? = null,
    val errorCode: String? = null,
    val type: String,
)

@Serializable
data class CommunityMemberDto(
    val id: Long,
    val topic: String,
    val firstName: String,
    val pictureUrl: String,
    val natives: List<String> = emptyList(),
    val learns: List<String> = emptyList(),
    val referenceCnt: Int,
)
