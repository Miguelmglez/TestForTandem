package com.mmg.testfortandem.domain.usecase

import com.mmg.testfortandem.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLikedIdsUseCase @Inject constructor(
    private val repository: CommunityRepository,
) {
    operator fun invoke(): Flow<Set<Long>> = repository.observeLikedIds()
}
