package com.mmg.testfortandem.domain.repository

import androidx.paging.PagingData
import com.mmg.testfortandem.domain.model.CommunityMember
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for the community feature, abstracting away the details of data sources.
 * The viewmodel will depend on this interface, and the implementation will be provided via dependency injection.
 * This allows for separation of concerns, easier testing, and flexibility in changing data sources without affecting the viewmodel or UI layers.
 */
interface CommunityRepository {

    fun observeCommunity(): Flow<PagingData<CommunityMember>>

    fun observeLikedIds(): Flow<Set<Long>>

    suspend fun toggleLike(memberId: Long)
}