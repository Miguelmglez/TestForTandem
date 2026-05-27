package com.mmg.testfortandem.domain.usecase

import com.mmg.testfortandem.domain.repository.CommunityRepository
import javax.inject.Inject

class ToggleMemberLikeUseCase @Inject constructor(
    private val repository: CommunityRepository,
) {
    suspend operator fun invoke(memberId: Long) {
        repository.toggleLike(memberId)
    }
}
