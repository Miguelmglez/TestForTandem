package com.mmg.testfortandem.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mmg.testfortandem.data.local.CommunityLocalDataSource
import com.mmg.testfortandem.data.paging.CommunityPagingSource
import com.mmg.testfortandem.data.remote.CommunityRemoteDataSource
import com.mmg.testfortandem.domain.model.CommunityMember
import com.mmg.testfortandem.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for the community feature, coordinating between remote and local data sources.
 */
@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val remoteDataSource: CommunityRemoteDataSource,
    private val localDataSource: CommunityLocalDataSource,
) : CommunityRepository {

    override fun observeCommunity(): Flow<PagingData<CommunityMember>> {
        return Pager(
            config = PagingConfig(
                pageSize = CommunityRemoteDataSource.PAGE_SIZE,
                initialLoadSize = CommunityRemoteDataSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { CommunityPagingSource(remoteDataSource) },
        ).flow
    }

    override fun observeLikedIds(): Flow<Set<Long>> = localDataSource.observeLikedIds()

    override suspend fun toggleLike(memberId: Long) {
        localDataSource.toggleLike(memberId)
    }
}