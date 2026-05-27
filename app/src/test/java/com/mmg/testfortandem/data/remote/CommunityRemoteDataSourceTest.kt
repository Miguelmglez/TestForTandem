package com.mmg.testfortandem.data.remote

import com.google.common.truth.Truth.assertThat
import com.mmg.testfortandem.data.remote.api.CommunityApi
import com.mmg.testfortandem.data.remote.dto.CommunityMemberDto
import com.mmg.testfortandem.data.remote.dto.CommunityResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityRemoteDataSourceTest {

    private val api: CommunityApi = mockk()
    private val dataSource = CommunityRemoteDataSource(api, UnconfinedTestDispatcher())

    @Test
    fun `returns Success with mapped members on successful response`() = runTest {
        coEvery { api.getCommunity(1) } returns CommunityResponseDto(
            response = listOf(sampleDto(id = 1L), sampleDto(id = 2L)),
            errorCode = null,
            type = "success",
        )

        val result = dataSource.fetchPage(1)

        assertThat(result).isInstanceOf(DataResult.Success::class.java)
        val members = (result as DataResult.Success).data
        assertThat(members).hasSize(2)
        assertThat(members.map { it.id }).containsExactly(1L, 2L).inOrder()
    }

    @Test
    fun `discards corrupt members but keeps valid ones`() = runTest {
        coEvery { api.getCommunity(1) } returns CommunityResponseDto(
            response = listOf(
                sampleDto(id = 1L),
                sampleDto(id = 2L, firstName = ""),  // domain validation fails
                sampleDto(id = 3L),
            ),
            errorCode = null,
            type = "success",
        )

        val result = dataSource.fetchPage(1) as DataResult.Success
        assertThat(result.data.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun `returns Server failure when type is not success`() = runTest {
        coEvery { api.getCommunity(1) } returns CommunityResponseDto(
            response = null,
            errorCode = "42",
            type = "error",
        )

        val result = dataSource.fetchPage(1)

        assertThat(result).isInstanceOf(DataResult.Failure::class.java)
        val failure = (result as DataResult.Failure).error
        assertThat(failure).isInstanceOf(DataError.Server::class.java)
        assertThat((failure as DataError.Server).code).isEqualTo(42)
    }

    @Test
    fun `translates UnknownHostException to NoConnection`() = runTest {
        coEvery { api.getCommunity(any()) } throws UnknownHostException()

        val result = dataSource.fetchPage(1)

        assertThat(result).isEqualTo(DataResult.Failure(DataError.NoConnection))
    }

    @Test
    fun `translates SocketTimeoutException to Timeout`() = runTest {
        coEvery { api.getCommunity(any()) } throws SocketTimeoutException()

        val result = dataSource.fetchPage(1)

        assertThat(result).isEqualTo(DataResult.Failure(DataError.Timeout))
    }

    @Test
    fun `translates HttpException to Server with code`() = runTest {
        val body = "".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getCommunity(any()) } throws HttpException(
            Response.error<Any>(503, body)
        )

        val result = dataSource.fetchPage(1)

        assertThat(result).isEqualTo(DataResult.Failure(DataError.Server(503)))
    }

    @Test(expected = CancellationException::class)
    fun `propagates CancellationException without swallowing`() = runTest {
        coEvery { api.getCommunity(any()) } throws CancellationException("test")

        dataSource.fetchPage(1)
    }

    private fun sampleDto(
        id: Long = 1L,
        firstName: String = "Tobi",
    ) = CommunityMemberDto(
        id = id,
        firstName = firstName,
        topic = "topic",
        pictureUrl = "https://example.com/p.png",
        natives = listOf("de"),
        learns = listOf("en"),
        referenceCnt = 5,
    )
}
