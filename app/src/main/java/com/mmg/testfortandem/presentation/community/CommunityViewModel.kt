package com.mmg.testfortandem.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.mmg.testfortandem.domain.model.LikedMember
import com.mmg.testfortandem.domain.usecase.ObserveCommunityUseCase
import com.mmg.testfortandem.domain.usecase.ObserveLikedIdsUseCase
import com.mmg.testfortandem.domain.usecase.ToggleMemberLikeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    observeCommunity: ObserveCommunityUseCase,
    observeLikedIds: ObserveLikedIdsUseCase,
    private val toggleMemberLike: ToggleMemberLikeUseCase,
) : ViewModel() {

    val members: Flow<PagingData<LikedMember>> =
        observeCommunity()
            .cachedIn(viewModelScope)
            .combine(observeLikedIds()) { pagingData, likedIds ->
                pagingData.map { member ->
                    LikedMember(
                        member = member,
                        isLiked = member.id in likedIds,
                    )
                }
            }

    private val _events = Channel<CommunityUiEvent>(Channel.BUFFERED)
    val events: Flow<CommunityUiEvent> = _events.receiveAsFlow()

    fun onLikeToggled(memberId: Long) {
        viewModelScope.launch {
            try {
                toggleMemberLike(memberId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(CommunityUiEvent.ToggleLikeFailed)
            }
        }
    }
}
