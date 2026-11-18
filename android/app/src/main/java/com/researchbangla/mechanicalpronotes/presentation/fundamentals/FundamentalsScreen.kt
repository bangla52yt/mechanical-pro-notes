package com.researchbangla.mechanicalpronotes.presentation.fundamentals

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalTopic
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalTopicStatus
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalsCatalog
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticPositiveColors
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticWarningColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundamentalsScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onNavigateToCalculation: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    initialFocusTopicId: String? = null,
    bookmarkedTopicIds: Set<String> = emptySet(),
    onToggleBookmark: (FundamentalTopic) -> Unit = {}
) {
    val allTopics = remember { FundamentalsCatalog.ALL_TOPICS }
    val disciplineGroups = remember {
        listOf("All") + allTopics.map { it.disciplineGroup }.distinct()
    }
    var selectedGroup by remember(initialFocusTopicId) {
        val targetGroup = initialFocusTopicId?.let { id ->
            allTopics.firstOrNull { it.id.equals(id, ignoreCase = true) }?.disciplineGroup
        } ?: "All"
        mutableStateOf(targetGroup)
    }
    var showOnlyVerified by remember(initialFocusTopicId) { mutableStateOf(false) }

    val filteredTopics = remember(selectedGroup, showOnlyVerified) {
        allTopics.filter { topic ->
            val groupMatch = selectedGroup == "All" || topic.disciplineGroup == selectedGroup
            val statusMatch = !showOnlyVerified || topic.status == FundamentalTopicStatus.VERIFIED_BASELINE
            groupMatch && statusMatch
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(initialFocusTopicId, filteredTopics) {
        if (!initialFocusTopicId.isNullOrBlank()) {
            val targetIdx = filteredTopics.indexOfFirst { it.id.equals(initialFocusTopicId, ignoreCase = true) }
            if (targetIdx >= 0) {
                listState.animateScrollToItem(targetIdx + 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mechanical Fundamentals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val verifiedCount = allTopics.count { it.status == FundamentalTopicStatus.VERIFIED_BASELINE }
                        val gapCount = allTopics.count { it.status == FundamentalTopicStatus.CONTENT_GAP }
                        val subtitleText = if (gapCount == 0) {
                            "মেকানিক্যাল ইঞ্জিনিয়ারিং বেসিকস ($verifiedCount Verified Baselines)"
                        } else {
                            "মেকানিক্যাল ইঞ্জিনিয়ারিং বেসিকস ($verifiedCount Verified / $gapCount Gaps)"
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLibrary) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Open Technical Reference Library")
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                disciplineGroups.forEach { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group) }
                    )
                }
                FilterChip(
                    selected = showOnlyVerified,
                    onClick = { showOnlyVerified = !showOnlyVerified },
                    label = { Text("Verified Only") }
                )
            }

            if (filteredTopics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        title = "No Topics Match Filter",
                        message = "Try switching back to All discipline groups or disabling Verified Only."
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        SafetyWarningCard(
                            title = "Field Practice & Evidence Transparency",
                            message = "FUND-01 to FUND-15 reflect verified General Mechanical Knowledge Map baselines (DOC-MPN-009 & DOC-MPN-010). Confirm machine-specific OEM tolerances, statutory limits, and calibrated plant measurements before execution."
                        )
                    }

                    items(filteredTopics, key = { it.id }) { topic ->
                        val isBookmarked = bookmarkedTopicIds.any { it.equals(topic.id, ignoreCase = true) }
                        FundamentalTopicCard(
                            topic = topic,
                            isHighlighted = initialFocusTopicId != null && topic.id.equals(initialFocusTopicId, ignoreCase = true),
                            isBookmarked = isBookmarked,
                            onToggleBookmark = { onToggleBookmark(topic) },
                            onNavigateToEquipmentDetail = onNavigateToEquipmentDetail,
                            onNavigateToCalculation = onNavigateToCalculation
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FundamentalTopicCard(
    topic: FundamentalTopic,
    isHighlighted: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onNavigateToCalculation: (String) -> Unit
) {
    val isGap = topic.status == FundamentalTopicStatus.CONTENT_GAP
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val statusColors = if (isGap) semanticWarningColors(isDark) else semanticPositiveColors(isDark)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.dp,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${topic.id} • ${topic.disciplineGroup}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColors.container,
                        modifier = Modifier.semantics(mergeDescendants = true) {}
                    ) {
                        Text(
                            text = if (isGap) "CONTENT GAP" else "VERIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColors.content,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) {
                                "Remove bookmark for ${topic.title}"
                            } else {
                                "Bookmark ${topic.title}"
                            },
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = topic.bengaliTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Foundations: ${topic.theoreticalFoundations}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Plant Applications: ${topic.washingPlantApplications}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (topic.governingFormulas.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Key Field Relationships",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        topic.governingFormulas.forEach { formula ->
                            Text(
                                text = formula,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Text(
                text = "Failure Mechanisms: ${topic.failureMechanisms}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

            if (topic.linkedEquipmentIds.isNotEmpty() || topic.linkedCalcIds.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    topic.linkedEquipmentIds.forEach { eqId ->
                        OutlinedButton(
                            onClick = { onNavigateToEquipmentDetail(eqId) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(eqId, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    topic.linkedCalcIds.forEach { calcId ->
                        FilledTonalButton(
                            onClick = { onNavigateToCalculation(calcId) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(calcId, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Text(
                text = "Source: ${topic.sourceCitation}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "FundamentalsScreen - Light", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun FundamentalsLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        FundamentalsScreen(
            onOpenDrawer = {},
            onNavigateToEquipmentDetail = {},
            onNavigateToCalculation = {},
            onNavigateToLibrary = {}
        )
    }
}

@Preview(name = "FundamentalsScreen - Dark Focused Topic", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun FundamentalsDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        FundamentalsScreen(
            onOpenDrawer = {},
            onNavigateToEquipmentDetail = {},
            onNavigateToCalculation = {},
            onNavigateToLibrary = {},
            initialFocusTopicId = "FUND-01",
            bookmarkedTopicIds = setOf("FUND-01")
        )
    }
}
