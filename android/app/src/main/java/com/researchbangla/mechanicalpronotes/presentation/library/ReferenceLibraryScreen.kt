package com.researchbangla.mechanicalpronotes.presentation.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.catalog.EngineeringReferenceItem
import com.researchbangla.mechanicalpronotes.domain.catalog.TechnicalReferenceCatalog
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceLibraryScreen(
    onOpenDrawer: () -> Unit,
    initialFocusRefId: String? = null,
    bookmarkedRefIds: Set<String> = emptySet(),
    onToggleBookmark: (EngineeringReferenceItem) -> Unit = {}
) {
    val references = remember { TechnicalReferenceCatalog.ALL_REFERENCES }
    var selectedCategory by rememberSaveable(initialFocusRefId) {
        val targetCat = initialFocusRefId?.let { id ->
            TechnicalReferenceCatalog.ALL_REFERENCES.firstOrNull { it.id == id }?.category
        }
        mutableStateOf(targetCat)
    }

    val filteredList = remember(selectedCategory, references) {
        if (selectedCategory == null) {
            references
        } else {
            references.filter { it.category == selectedCategory }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(initialFocusRefId, filteredList) {
        if (!initialFocusRefId.isNullOrBlank()) {
            val targetIdx = filteredList.indexOfFirst { it.id == initialFocusRefId }
            if (targetIdx >= 0) {
                listState.animateScrollToItem(targetIdx)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Technical Reference Library",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All References", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
                FilterChip(
                    selected = selectedCategory == "Bangladesh Statutory",
                    onClick = { selectedCategory = "Bangladesh Statutory" },
                    label = { Text("Bangladesh Statutory", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
                FilterChip(
                    selected = selectedCategory == "International Standard",
                    onClick = { selectedCategory = "International Standard" },
                    label = { Text("International Standards", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
                FilterChip(
                    selected = selectedCategory == "Thermal Engineering",
                    onClick = { selectedCategory = "Thermal Engineering" },
                    label = { Text("Thermal Engineering", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
                FilterChip(
                    selected = selectedCategory == "Maintenance & Reliability",
                    onClick = { selectedCategory = "Maintenance & Reliability" },
                    label = { Text("Maintenance & Reliability", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(filteredList, key = { it.id }) { ref ->
                    val isHighlighted = ref.id == initialFocusRefId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(
                            width = if (isHighlighted) 1.5.dp else 1.dp,
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val isBookmarked = bookmarkedRefIds.any { it.equals(ref.id, ignoreCase = true) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = ref.id,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = ref.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onToggleBookmark(ref) },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = if (isBookmarked) {
                                            "Remove bookmark for ${ref.title}"
                                        } else {
                                            "Bookmark ${ref.title}"
                                        },
                                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ref.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ref.bengaliTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Issuing Authority: ${ref.authority}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ref.summary,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ref.mandatoryRequirements,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "ReferenceLibrary - Light", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun ReferenceLibraryLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        ReferenceLibraryScreen(onOpenDrawer = {})
    }
}

@Preview(name = "ReferenceLibrary - Dark Focused Ref", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun ReferenceLibraryDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        ReferenceLibraryScreen(
            onOpenDrawer = {},
            initialFocusRefId = "REF-01"
        )
    }
}
