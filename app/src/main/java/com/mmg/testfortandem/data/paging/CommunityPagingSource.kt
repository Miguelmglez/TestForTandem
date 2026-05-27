package com.mmg.testfortandem.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mmg.testfortandem.data.remote.CommunityRemoteDataSource
import com.mmg.testfortandem.data.remote.DataError
import com.mmg.testfortandem.data.remote.DataResult
import com.mmg.testfortandem.domain.model.CommunityMember

/**
 * Bridges the page-based remote API into Paging 3.
 *
 * The API returns up to [CommunityRemoteDataSource.PAGE_SIZE] members per page;
 * a page with fewer members is the last page (no metadata is provided
 * by the API to indicate end-of-pagination, so we infer it from size).
 *
 * Failures from the data source are surfaced as [LoadResult.Error] with
 * a synthetic [PagingDataException] that carries the typed [DataError]
 * up to the UI, where it can be translated into a user-facing message
 * without leaking transport details.
 */
class CommunityPagingSource(
    private val remoteDataSource: CommunityRemoteDataSource,
) : PagingSource<Int, CommunityMember>() {

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, CommunityMember> {
        val page = params.key ?: FIRST_PAGE
        return when (val result = remoteDataSource.fetchPage(page)) {
            is DataResult.Success -> {
                val members = result.data
                LoadResult.Page(
                    data = members,
                    prevKey = if (page == FIRST_PAGE) null else page - 1,
                    nextKey = if (members.size < CommunityRemoteDataSource.PAGE_SIZE) {
                        null
                    } else {
                        page + 1
                    },
                )
            }
            is DataResult.Failure -> LoadResult.Error(PagingDataException(result.error))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CommunityMember>): Int? {
        val anchor = state.anchorPosition ?: return null
        val closest = state.closestPageToPosition(anchor) ?: return null
        return closest.prevKey?.plus(1) ?: closest.nextKey?.minus(1)
    }

    companion object {
        const val FIRST_PAGE = 1
    }
}

/**
 * Carries a typed [DataError] through Paging's `Throwable`-based error channel.
 * Mapped back into the domain by the UI layer.
 */
class PagingDataException(val dataError: DataError) : Exception()