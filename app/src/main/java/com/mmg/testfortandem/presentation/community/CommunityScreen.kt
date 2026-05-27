package com.mmg.testfortandem.presentation.community


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mmg.testfortandem.R
import com.mmg.testfortandem.data.paging.PagingDataException
import com.mmg.testfortandem.data.remote.DataError
import com.mmg.testfortandem.domain.model.LikedMember
import com.mmg.testfortandem.presentation.components.MemberCard
import com.mmg.testfortandem.presentation.theme.BackgroundCream
import com.mmg.testfortandem.presentation.theme.DividerColor
import com.mmg.testfortandem.presentation.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: CommunityViewModel = hiltViewModel()) {
    val members = viewModel.members.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val toggleErrorMessage = stringResource(R.string.error_toggle_like)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CommunityUiEvent.ToggleLikeFailed ->
                    snackbarHostState.showSnackbar(toggleErrorMessage)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.community_title),
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundCream,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundCream,
    ) { padding ->
        CommunityContent(
            members = members,
            paddingValues = padding,
            onLikeClick = viewModel::onLikeToggled,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityContent(
    members: LazyPagingItems<LikedMember>,
    paddingValues: PaddingValues,
    onLikeClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshing = members.loadState.refresh is LoadState.Loading && members.itemCount > 0

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { members.refresh() },
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        val refreshState = members.loadState.refresh

        when {
            refreshState is LoadState.Loading && members.itemCount == 0 -> {
                CenteredLoading()
            }
            refreshState is LoadState.Error && members.itemCount == 0 -> {
                CenteredError(
                    message = (refreshState.error as? PagingDataException)
                        ?.dataError.toDisplayMessage(),
                    onRetry = members::retry,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        count = members.itemCount,
                        key = members.itemKey { it.member.id },
                    ) { index ->
                        val likedMember = members[index] ?: return@items
                        MemberCard(
                            likedMember = likedMember,
                            onLikeClick = onLikeClick,
                        )
                        HorizontalDivider(color = DividerColor)
                    }

                    // Append loading / error states for subsequent pages
                    when (members.loadState.append) {
                        is LoadState.Loading -> {
                            item { CenteredLoading() }
                        }
                        is LoadState.Error -> {
                            item {
                                AppendError(
                                    onRetry = members::retry,
                                )
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenteredError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = message, color = TextPrimary)
            Spacer(
                modifier = Modifier.height(12.dp),
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
private fun AppendError(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun DataError?.toDisplayMessage(): String = when (this) {
    DataError.NoConnection -> stringResource(R.string.error_no_connection)
    DataError.Timeout -> stringResource(R.string.error_timeout)
    else -> stringResource(R.string.error_generic)
}
