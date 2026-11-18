package com.researchbangla.mechanicalpronotes.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.R
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.presentation.theme.*

/**
 * Visual badge displaying the exact content-depth status.
 *
 * CRITICAL RULE:
 * Explicit color and label prevents inventory-only records from masquerading
 * as complete 21-section engineering manuals.
 */
@Composable
fun ContentDepthBadge(
    depth: ContentDepth,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val (bgColor, textColor) = when (depth) {
        ContentDepth.FULL_BLUEPRINT -> {
            val colors = semanticPositiveColors(isDark)
            colors.container to colors.content
        }
        ContentDepth.PARTIAL_BLUEPRINT -> {
            val content = if (isDark) SteelBlueDarkPrimary else PartialBlueprintBlue
            content.copy(alpha = if (isDark) 0.18f else 0.14f) to content
        }
        ContentDepth.INVENTORY_ONLY -> {
            val content = if (isDark) Color(0xFFB0BEC5) else Color(0xFF4A5568)
            content.copy(alpha = if (isDark) 0.18f else 0.14f) to content
        }
        ContentDepth.RESEARCH_PENDING -> {
            val colors = semanticWarningColors(isDark)
            colors.container to colors.content
        }
    }

    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Content classification: ${depth.displayName}"
        },
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.45f))
    ) {
        Text(
            text = depth.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Safety & Statutory Warning banner.
 */
@Composable
fun SafetyWarningCard(
    title: String,
    message: String,
    isStatutory: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val accentColor = if (isStatutory) {
        if (isDark) SafetyRedDarkHazard else SafetyRedHazard
    } else {
        if (isDark) SafetyAmberDarkWarning else SafetyAmberWarning
    }
    val noticeTypeLabel = if (isStatutory) "STATUTORY MANDATE" else "SAFETY NOTICE"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = false) {
                contentDescription = "$noticeTypeLabel: $title"
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = if (isStatutory) "Statutory mandate warning" else "Safety notice warning",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = noticeTypeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Technical Math Formula Card.
 */
@Composable
fun MathFormulaCard(
    title: String,
    formulaLatex: String,
    engineeringReference: String? = null,
    onOpenCalculator: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formulaLatex,
                    style = EngineeringFormulaStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
            if (engineeringReference != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ref: $engineeringReference",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onOpenCalculator != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .align(Alignment.End)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Open in Calculator")
                }
            }
        }
    }
}

/**
 * Standard Equipment Card for List & Search results.
 */
@Composable
fun EquipmentCardItem(
    equipment: Equipment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBookmarked: Boolean? = null,
    onToggleBookmark: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open ${equipment.name} details",
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = equipment.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ContentDepthBadge(depth = equipment.contentDepth)
                    if (isBookmarked != null && onToggleBookmark != null) {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) {
                                    "Remove bookmark for ${equipment.name}"
                                } else {
                                    "Bookmark ${equipment.name}"
                                },
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = equipment.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = equipment.bengaliName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (equipment.capacityRange != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Capacity: ${equipment.capacityRange}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Standard Empty State View.
 */
@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Interactive offline Vector Schematic viewer with pinch/pan & +/- zoom controls.
 */
@Composable
fun ZoomableSchematicCard(
    title: String,
    subtitle: String,
    @DrawableRes drawableRes: Int,
    legendText: String,
    modifier: Modifier = Modifier
) {
    var scale by remember(drawableRes) { mutableStateOf(1f) }
    var offset by remember(drawableRes) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember(drawableRes) { mutableStateOf(IntSize.Zero) }

    fun clampOffset(rawOffset: Offset, targetScale: Float): Offset {
        if (targetScale <= 1f || viewportSize.width <= 0 || viewportSize.height <= 0) {
            return Offset.Zero
        }
        val maxX = (viewportSize.width * (targetScale - 1f)) / 2f
        val maxY = (viewportSize.height * (targetScale - 1f)) / 2f
        return Offset(
            x = rawOffset.x.coerceIn(-maxX, maxX),
            y = rawOffset.y.coerceIn(-maxY, maxY)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = {
                            val newScale = (scale / 1.25f).coerceAtLeast(1f)
                            scale = newScale
                            offset = clampOffset(offset, newScale)
                        },
                        enabled = scale > 1f,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics { contentDescription = "Zoom out schematic" }
                    ) { Text("-") }
                    OutlinedButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        enabled = scale > 1f || offset != Offset.Zero,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
                            .semantics {
                                contentDescription = "Reset schematic zoom to 100 percent, currently ${(scale * 100).toInt()} percent"
                            }
                    ) { Text("${(scale * 100).toInt()}%") }
                    OutlinedButton(
                        onClick = {
                            val newScale = (scale * 1.25f).coerceAtMost(4f)
                            scale = newScale
                            offset = clampOffset(offset, newScale)
                        },
                        enabled = scale < 4f,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics { contentDescription = "Zoom in schematic" }
                    ) { Text("+") }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(SchematicDraftingSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clipToBounds()
                    .onSizeChanged { viewportSize = it }
                    .pointerInput(drawableRes) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val pressedCount = event.changes.count { it.pressed }
                                // Preserve parent vertical scrolling at 1x zoom for single-finger swipes;
                                // intercept only multi-finger pinch gestures or panning while zoomed in.
                                if (pressedCount >= 2 || scale > 1.01f) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val newScale = (scale * zoomChange).coerceIn(1f, 4f)
                                    scale = newScale
                                    offset = if (newScale > 1.01f) {
                                        clampOffset(offset + panChange, newScale)
                                    } else {
                                        Offset.Zero
                                    }
                                    event.changes.forEach { change ->
                                        if (change.positionChanged()) {
                                            change.consume()
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = "$title schematic diagram. $legendText",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = legendText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Lightweight zero-dependency Markdown renderer supporting **bold** emphasis and bullets.
 */
@Composable
fun SimpleMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val lines = markdown.lines()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        lines.forEach { rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isNotEmpty()) {
                val isHeading = trimmed.startsWith("### ") || trimmed.startsWith("## ")
                val cleaned = when {
                    trimmed.startsWith("### ") -> trimmed.removePrefix("### ")
                    trimmed.startsWith("## ") -> trimmed.removePrefix("## ")
                    trimmed.startsWith("- ") -> "• " + trimmed.removePrefix("- ")
                    trimmed.startsWith("* ") -> "• " + trimmed.removePrefix("* ")
                    else -> trimmed
                }
                Text(
                    text = parseInlineBold(cleaned),
                    style = if (isHeading) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isHeading) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHeading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun parseInlineBold(text: String): AnnotatedString {
    return buildAnnotatedString {
        var idx = 0
        while (idx < text.length) {
            val start = text.indexOf("**", idx)
            if (start == -1) {
                append(text.substring(idx))
                break
            }
            if (start > idx) {
                append(text.substring(idx, start))
            }
            val end = text.indexOf("**", start + 2)
            if (end == -1) {
                append(text.substring(start))
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(text.substring(start + 2, end))
            }
            idx = end + 2
        }
    }
}

private object CommonComponentsPreviewData {
    val sampleBoiler = Equipment(
        id = "EQ-G01",
        categoryId = "CAT-G1",
        familyId = "FAM-G",
        name = "Natural Gas 3-Pass Wet-Back Firetube Boiler",
        bengaliName = "ন্যাচারাল গ্যাস থ্রি-পাস ফায়ারটিউব বয়লার",
        contentDepth = ContentDepth.FULL_BLUEPRINT,
        criticalityTier = CriticalityTier.SAFETY_CRITICAL,
        capacityRange = "4,000 – 12,000 kg/h @ 10.54 bar(g)"
    )
}

@Preview(name = "Common Components - Light", showBackground = true, widthDp = 380)
@Composable
private fun CommonComponentsLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContentDepthBadge(ContentDepth.FULL_BLUEPRINT)
                    ContentDepthBadge(ContentDepth.PARTIAL_BLUEPRINT)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContentDepthBadge(ContentDepth.INVENTORY_ONLY)
                    ContentDepthBadge(ContentDepth.RESEARCH_PENDING)
                }
                SafetyWarningCard(
                    title = "The Boilers Act, 2022 Compliance",
                    message = "Annual statutory renewal & live safety valve popping test mandatory under OCIB inspection.",
                    isStatutory = true
                )
                MathFormulaCard(
                    title = "Direct Thermal Efficiency",
                    formulaLatex = "eta = [ m_s * (h_s - h_w) ] / [ m_f * NCV ] * 100",
                    engineeringReference = "ASME PTC 4",
                    onOpenCalculator = {}
                )
                EquipmentCardItem(
                    equipment = CommonComponentsPreviewData.sampleBoiler,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Common Components - Dark High Legibility", showBackground = true, widthDp = 360)
@Composable
private fun CommonComponentsDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContentDepthBadge(ContentDepth.FULL_BLUEPRINT)
                    ContentDepthBadge(ContentDepth.RESEARCH_PENDING)
                }
                SafetyWarningCard(
                    title = "High-Speed Hydro Extractor LOTO",
                    message = "Verify zero basket RPM and electromechanical lid interlock before opening.",
                    isStatutory = false
                )
                EquipmentCardItem(
                    equipment = CommonComponentsPreviewData.sampleBoiler,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "ZoomableSchematicCard - Light", showBackground = true, widthDp = 380)
@Composable
private fun ZoomableSchematicCardLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ZoomableSchematicCard(
                title = "3-Pass Firetube Gas Boiler Schematic",
                subtitle = "Offline Technical Vector Diagram (Pinch or use +/- buttons to zoom)",
                drawableRes = R.drawable.diag_gas_boiler_3pass,
                legendText = "Amber: Morrison Furnace (Pass 1) -> Wet-Back Reversal -> Passes 2 & 3 Smoke Tubes | Red: Dual Safety Valves",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Preview(name = "ZoomableSchematicCard - Dark", showBackground = true, widthDp = 380)
@Composable
private fun ZoomableSchematicCardDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ZoomableSchematicCard(
                title = "3-Pass Firetube Gas Boiler Schematic",
                subtitle = "Offline Technical Vector Diagram (Pinch or use +/- buttons to zoom)",
                drawableRes = R.drawable.diag_gas_boiler_3pass,
                legendText = "Monitor LWCO probes, gas train pressure switch, and dual spring safety valves.",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
