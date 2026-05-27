package com.mmg.testfortandem.domain.model

/**
 * Represents a community member along with their like state.
 * So the viewmodel can work with a single stream of data that contains all the information it needs to display the UI,
 * without having to combine data from multiple sources.
 */
data class LikedMember(
    val member: CommunityMember,
    val isLiked: Boolean,
)