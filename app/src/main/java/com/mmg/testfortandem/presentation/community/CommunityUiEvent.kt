package com.mmg.testfortandem.presentation.community

/**
 * Represents one-time UI events that the [CommunityScreen] should react to.
 * So we avoid exposing one-shot events as part of the continuous state stream
 * (e.g. `Flow<PagingData<...>>`), which can lead to issues like events being re-emitted on configuration changes.
 */
sealed interface CommunityUiEvent {
    data object ToggleLikeFailed : CommunityUiEvent
}