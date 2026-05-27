package com.mmg.testfortandem.data.repository

import com.mmg.testfortandem.data.local.CommunityLocalDataSource
import com.mmg.testfortandem.data.remote.CommunityRemoteDataSource
import com.mmg.testfortandem.data.repository.CommunityRepositoryImpl
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.coJustRun
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CommunityRepositoryImplTest {

    private val remote: CommunityRemoteDataSource = mockk()
    private val local: CommunityLocalDataSource = mockk()
    private val repository = CommunityRepositoryImpl(remote, local)

    @Test
    fun `toggleLike delegates to local data source`() = runTest {
        coJustRun { local.toggleLike(any()) }

        repository.toggleLike(42L)

        coVerify(exactly = 1) { local.toggleLike(42L) }
    }
}
