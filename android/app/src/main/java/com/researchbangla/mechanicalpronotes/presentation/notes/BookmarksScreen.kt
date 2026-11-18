package com.researchbangla.mechanicalpronotes.presentation.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.model.BookmarkEntityType
import com.researchbangla.mechanicalpronotes.domain.model.UserBookmark
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode

@Composable
fun BookmarksScreen(
    viewModel: NotesViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToItem: (String, BookmarkEntityType) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    BookmarksScreenContent(
        state = state,
        onOpenDrawer = onOpenDrawer,
        onNavigateToItem = onNavigateToItem,
        onRemoveBookmark = viewModel::removeBookmark
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreenContent(
    state: NotesUiState,
    onOpenDrawer: () -> Unit,
    onNavigateToItem: (String, BookmarkEntityType) -> Unit,
    onRemoveBookmark: (UserBookmark) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Bookmarks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        title = "No Bookmarks Saved",
                        message = "Bookmark critical equipment, boiler inspection points, or calculations for immediate one-tap offline access on the plant floor."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        Text(
                            text = "Pinned Engineering References (${state.bookmarks.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.semantics { heading() }
                        )
                    }

                    items(state.bookmarks, key = { it.id }) { bookmark ->
                        BookmarkItemCard(
                            bookmark = bookmark,
                            onClick = { onNavigateToItem(bookmark.entityId, bookmark.entityType) },
                            onRemove = { onRemoveBookmark(bookmark) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkItemCard(
    bookmark: UserBookmark,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (bookmark.entityType) {
                        BookmarkEntityType.EQUIPMENT -> Icons.Default.PrecisionManufacturing
                        BookmarkEntityType.BOILER_SECTION -> Icons.Default.LocalFireDepartment
                        BookmarkEntityType.CALCULATION -> Icons.Default.Calculate
                        BookmarkEntityType.CHECKLIST -> Icons.Default.Checklist
                        BookmarkEntityType.TECHNICAL_STANDARD -> Icons.Default.MenuBook
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = bookmark.entityId,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = bookmark.entityTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bookmark.entityType.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.BookmarkRemove,
                    contentDescription = "Remove bookmark for ${bookmark.entityTitle}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(name = "Bookmarks Empty Light", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun BookmarksScreenEmptyLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        BookmarksScreenContent(
            state = NotesUiState(isLoading = false, bookmarks = emptyList()),
            onOpenDrawer = {},
            onNavigateToItem = { _, _ -> },
            onRemoveBookmark = {}
        )
    }
}

@Preview(name = "Bookmarks Populated Dark", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun BookmarksScreenPopulatedDarkPreview() {
    val sampleBookmarks = listOf(
        UserBookmark(
            id = 1L,
            entityType = BookmarkEntityType.EQUIPMENT,
            entityId = "EQ-A01",
            entityTitle = "Fire-Tube Steam Boiler (Gas-Fired)"
        ),
        UserBookmark(
            id = 2L,
            entityType = BookmarkEntityType.CALCULATION,
            entityId = "CALC-06",
            entityTitle = "Blowdown Flash Steam & Heat Recovery"
        )
    )
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        BookmarksScreenContent(
            state = NotesUiState(isLoading = false, bookmarks = sampleBookmarks),
            onOpenDrawer = {},
            onNavigateToItem = { _, _ -> },
            onRemoveBookmark = {}
        )
    }
}

