package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.asString
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import dev.luizhenriquevicari.remir.app.designsystem.components.SecondaryButton
import org.koin.compose.viewmodel.koinViewModel

/** How many rows before the end of the list the next page starts loading, so it is usually there before the user. */
private const val LOAD_MORE_THRESHOLD = 5

/** Wires the ViewModel to the screen. The only part that knows about Koin/StateFlow. */
@Composable
fun SleepHistoryRoute(
    onBack: () -> Unit,
    viewModel: SleepHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SleepHistoryScreen(state = state, onBack = onBack, onLoadMore = viewModel::loadMore)
}

/** Stateless: state + lambdas only. */
@Composable
fun SleepHistoryScreen(
    state: SleepHistoryState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Scaffold(topBar = { RemirTopBar(stringResource(R.string.sleep_history_title), onBack = onBack) }) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.loading -> Loading(modifier)
            state.items.isEmpty() && state.error != null -> ErrorState(state.error.asString(), modifier, onLoadMore)
            state.items.isEmpty() -> EmptyHistory(modifier)
            else -> NightsList(state, onLoadMore, modifier)
        }
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.sleep_empty), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NightsList(
    state: SleepHistoryState,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // True when the user is close to the last row. Keyed on the item count too, so a page that did not fill the
    // screen asks for the next one right away instead of waiting for a scroll.
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) >= info.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(nearEnd, state.items.size) { if (nearEnd) onLoadMore() }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        items(state.items, key = { it.id }) {
            SleepNightRow(it)
            HorizontalDivider()
        }
        item { ListFooter(state, onLoadMore) }
    }
}

/** A spinner while the next page loads, or a way to try again when it failed. */
@Composable
private fun ListFooter(
    state: SleepHistoryState,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        when {
            state.loadingMore -> CircularProgressIndicator()
            state.error != null -> SecondaryButton(stringResource(R.string.retry), onClick = onRetry)
        }
    }
}

@PreviewLightDark
@Composable
private fun SleepHistoryScreenPreview() {
    RemirPreview {
        SleepHistoryScreen(
            SleepHistoryState(loading = false, items = sampleNights, endReached = true),
            onBack = {},
            onLoadMore = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepHistoryScreenLoadingMorePreview() {
    RemirPreview {
        SleepHistoryScreen(
            SleepHistoryState(loading = false, items = sampleNights, loadingMore = true),
            onBack = {},
            onLoadMore = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepHistoryScreenEmptyPreview() {
    RemirPreview {
        SleepHistoryScreen(SleepHistoryState(loading = false, endReached = true), onBack = {}, onLoadMore = {})
    }
}
