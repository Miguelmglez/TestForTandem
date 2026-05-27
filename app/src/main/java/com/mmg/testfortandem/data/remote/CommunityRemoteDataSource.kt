package com.mmg.testfortandem.data.remote

import com.mmg.testfortandem.data.remote.api.CommunityApi
import com.mmg.testfortandem.data.remote.mapper.toDomain
import com.mmg.testfortandem.di.IoDispatcher
import com.mmg.testfortandem.domain.model.CommunityMember
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class CommunityRemoteDataSource @Inject constructor(
    private val api: CommunityApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun fetchPage(page: Int): DataResult<List<CommunityMember>> =
        withContext(ioDispatcher) {
            try {
                val response = api.getCommunity(page)

                if (response.type != TYPE_SUCCESS || response.response == null) {
                    DataResult.Failure(
                        DataError.Server(code = response.errorCode?.toIntOrNull() ?: -1)
                    )
                } else {
                    val members = response.response.mapNotNull { dto ->
                        runCatching { dto.toDomain() }.getOrNull()
                    }
                    DataResult.Success(members)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: UnknownHostException) {
                DataResult.Failure(DataError.NoConnection)
            } catch (e: SocketTimeoutException) {
                DataResult.Failure(DataError.Timeout)
            } catch (e: HttpException) {
                DataResult.Failure(DataError.Server(code = e.code()))
            } catch (e: IOException) {
                DataResult.Failure(DataError.NoConnection)
            } catch (e: Exception) {
                DataResult.Failure(DataError.Unknown(cause = e))
            }
        }

    companion object {
        const val PAGE_SIZE = 20
        private const val TYPE_SUCCESS = "success"
    }
}
