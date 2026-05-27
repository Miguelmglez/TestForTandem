package com.mmg.testfortandem.presentation.community

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mmg.testfortandem.domain.model.CommunityMember
import com.mmg.testfortandem.domain.usecase.ObserveCommunityUseCase
import com.mmg.testfortandem.domain.usecase.ObserveLikedIdsUseCase
import com.mmg.testfortandem.domain.usecase.ToggleMemberLikeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityViewModelTest {

    private val observeCommunity: ObserveCommunityUseCase = mockk()
    private val observeLikedIds: ObserveLikedIdsUseCase = mockk()
    private val toggleMemberLike: ToggleMemberLikeUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeCommunity.invoke() } returns flowOf(PagingData.empty<CommunityMember>())
        every { observeLikedIds.invoke() } returns flowOf(emptySet())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onLikeToggled invokes the use case with member id`() = runTest {
        coEvery { toggleMemberLike.invoke(any()) } returns Unit
        val viewModel = CommunityViewModel(observeCommunity, observeLikedIds, toggleMemberLike)

        viewModel.onLikeToggled(123L)

        coVerify(exactly = 1) { toggleMemberLike.invoke(123L) }
    }

    @Test
    fun `emits ToggleLikeFailed event when use case throws`() = runTest {
        coEvery { toggleMemberLike.invoke(any()) } throws RuntimeException("db error")
        val viewModel = CommunityViewModel(observeCommunity, observeLikedIds, toggleMemberLike)

        viewModel.events.test {
            viewModel.onLikeToggled(123L)
            assertThat(awaitItem()).isEqualTo(CommunityUiEvent.ToggleLikeFailed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit event on successful toggle`() = runTest {
        coEvery { toggleMemberLike.invoke(any()) } returns Unit
        val viewModel = CommunityViewModel(observeCommunity, observeLikedIds, toggleMemberLike)

        viewModel.events.test {
            viewModel.onLikeToggled(123L)
            expectNoEvents()
        }
    }
}
