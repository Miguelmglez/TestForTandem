package com.mmg.testfortandem.data.local

import com.mmg.testfortandem.data.local.dao.LikedMemberDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Data source for the community feature, backed by Room.
 *
 * Translates map to set of liked member ids, which is the only information we need to persist about the like state of community members.
 */
class CommunityLocalDataSource @Inject constructor(
    private val dao: LikedMemberDao,
) {
    fun observeLikedIds(): Flow<Set<Long>> =
        dao.observeAllIds().map { it.toSet() }
            .flowOn(Dispatchers.IO)

    suspend fun toggleLike(memberId: Long) = dao.toggleLike(memberId)
}