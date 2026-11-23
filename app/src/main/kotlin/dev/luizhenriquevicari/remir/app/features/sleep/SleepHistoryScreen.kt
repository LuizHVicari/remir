package dev.luizhenriquevicari.remir.app.features.sleep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.luizhenriquevicari.remir.app.R
import dev.luizhenriquevicari.remir.app.common.asString
import dev.luizhenriquevicari.remir.app.common.toLocalizedMonth
import dev.luizhenriquevicari.remir.app.designsystem.RemirPreview
import dev.luizhenriquevicari.remir.app.designsystem.components.ErrorState
import dev.luizhenriquevicari.remir.app.designsystem.components.Loading
import dev.luizhenriquevicari.remir.app.designsystem.components.RemirTopBar
import dev.luizhenriquevicari.remir.app.designsystem.components.SecondaryButton
import dev.luizhenriquevicari.remir.app.designsystem.components.SwipeToDeleteBox
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import org.koin.compose.viewmodel.koinViewModel

/** How many rows before the end of the list the next page starts loading, so it is usually there before the user. */
private const val LOAD_MORE_THRESHOLD = 5

/** Wires the ViewModel to the screen. The only part that knows about Koin/StateFlow. */
@Composable
fun SleepHistoryRoute(
    onBack: () -> Unit,
    onEdit: (SleepRecordDto) -> Unit,
    viewModel: SleepHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Also runs when coming back from editing a night, so the list shows it changed.
    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    SleepHistoryScreen(
        state = state,
        onBack = onBack,
        onLoadMore = viewModel::loadMore,
        onEdit = onEdit,
        onDelete = viewModel::delete,
        onUndoDelete = viewModel::undoDelete,
        onDeletedDismissed = viewModel::onDeletedDismissed,
        onActionErrorShown = viewModel::onActionErrorShown,
    )
}

/** Stateless: state + lambdas only. The snackbar host is the only local UI state. */
@Composable
fun SleepHistoryScreen(
    state: SleepHistoryState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
    onEdit: (SleepRecordDto) -> Unit,
    onDelete: (SleepRecordDto) -> Unit,
    onUndoDelete: () -> Unit,
    onDeletedDismissed: () -> Unit,
    onActionErrorShown: () -> Unit,
) {
    val snackbarHost = remember { SnackbarHostState() }
    DeletedNightSnackbar(snackbarHost, state.deleted != null, onUndoDelete, onDeletedDismissed)
    ActionErrorSnackbar(snackbarHost, state.actionError, onActionErrorShown)
    Scaffold(
        topBar = { RemirTopBar(stringResource(R.string.sleep_history_title), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.loading -> Loading(modifier)
            state.items.isEmpty() && state.error != null -> ErrorState(state.error.asString(), modifier, onLoadMore)
            state.items.isEmpty() -> EmptyHistory(modifier)
            else -> NightsList(state, onLoadMore, onEdit, onDelete, modifier)
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
    onEdit: (SleepRecordDto) -> Unit,
    onDelete: (SleepRecordDto) -> Unit,
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
    // Nights are already newest first, so each month's nights are next to each other.
    val months = remember(state.items) { state.items.groupBy { it.wokeUpAt.toLocalizedMonth() } }
    val deleteDescription = stringResource(R.string.sleep_delete)

    LazyColumn(state = listState, modifier = modifier.fillMaxSize()) {
        months.forEach { (month, nights) ->
            item(key = month) { MonthHeader(month, Modifier.animateItem()) }
            items(nights, key = { it.id }) { night ->
                SwipeToDeleteBox(
                    onDelete = { onDelete(night) },
                    deleteDescription = deleteDescription,
                    modifier = Modifier.animateItem(),
                ) {
                    NightEntry(night) { onEdit(night) }
                }
            }
        }
        item { ListFooter(state, onLoadMore) }
    }
}

/** The row and the line under it, so both move together when swiped. */
@Composable
private fun NightEntry(
    night: SleepRecordDto,
    onClick: () -> Unit,
) {
    Column {
        SleepNightRow(night, onClick = onClick, modifier = Modifier.padding(horizontal = 20.dp))
        HorizontalDivider(Modifier.padding(horizontal = 20.dp))
    }
}

@Composable
private fun MonthHeader(
    month: String,
    modifier: Modifier = Modifier,
) {
    Text(
        month,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
    )
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

@Composable
private fun PreviewSleepHistoryScreen(state: SleepHistoryState) {
    RemirPreview {
        SleepHistoryScreen(
            state = state,
            onBack = {},
            onLoadMore = {},
            onEdit = {},
            onDelete = {},
            onUndoDelete = {},
            onDeletedDismissed = {},
            onActionErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SleepHistoryScreenPreview() {
    PreviewSleepHistoryScreen(SleepHistoryState(loading = false, items = sampleNights, endReached = true))
}

@PreviewLightDark
@Composable
private fun SleepHistoryScreenLoadingMorePreview() {
    PreviewSleepHistoryScreen(SleepHistoryState(loading = false, items = sampleNights, loadingMore = true))
}

@PreviewLightDark
@Composable
private fun SleepHistoryScreenEmptyPreview() {
    PreviewSleepHistoryScreen(SleepHistoryState(loading = false, endReached = true))
}
