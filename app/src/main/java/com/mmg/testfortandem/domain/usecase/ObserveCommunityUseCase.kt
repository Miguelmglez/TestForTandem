package com.mmg.testfortandem.domain.usecase

import androidx.paging.PagingData
import com.mmg.testfortandem.domain.model.CommunityMember
import com.mmg.testfortandem.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCommunityUseCase @Inject constructor(
    private val repository: CommunityRepository,
) {
    operator fun invoke(): Flow<PagingData<CommunityMember>> =
        repository.observeCommunity()
}
