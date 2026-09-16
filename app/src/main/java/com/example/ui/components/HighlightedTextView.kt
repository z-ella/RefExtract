package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExtractedItem
import com.example.model.ExtractionCategory
import com.example.ui.theme.CatCustomColor
import com.example.ui.theme.CatEntityColor
import com.example.ui.theme.CatMetricColor
import com.example.ui.theme.CatQuotationColor
import com.example.ui.theme.CatReferenceColor
import com.example.ui.theme.CatTemporalColor

fun getCategoryColor(category: ExtractionCategory): Color {
    return when (category) {
        ExtractionCategory.REFERENCE -> CatReferenceColor
        ExtractionCategory.QUOTATION -> CatQuotationColor
        ExtractionCategory.NAMED_ENTITY -> CatEntityColor
        ExtractionCategory.METRIC_STAT -> CatMetricColor
        ExtractionCategory.TEMPORAL_DATE -> CatTemporalColor
        ExtractionCategory.CUSTOM_RULE -> CatCustomColor
    }
}

@Composable
fun HighlightedTextView(
    text: String,
    items: List<ExtractedItem>,
    selectedItemId: String?,
    onItemClick: (ExtractedItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Build non-overlapping span annotations for ClickableText
    val annotatedString = remember(text, items, selectedItemId) {
        buildAnnotatedString {
            append(text)

            // Sort items by start index ascending
            val validSpans = items
                .filter { it.startIndex >= 0 && it.endIndex <= text.length && it.startIndex < it.endIndex }
                .sortedBy { it.startIndex }

            // Apply non-overlapping spans
            var lastEnd = 0
            for (item in validSpans) {
                if (item.startIndex >= lastEnd) {
                    val color = getCategoryColor(item.category)
                    val isSelected = item.id == selectedItemId

                    addStyle(
                        style = SpanStyle(
                            background = if (isSelected) color.copy(alpha = 0.45f) else color.copy(alpha = 0.20f),
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None
                        ),
                        start = item.startIndex,
                        end = item.endIndex
                    )
                    addStringAnnotation(
                        tag = "ITEM_ID",
                        annotation = item.id,
                        start = item.startIndex,
                        end = item.endIndex
                    )
                    lastEnd = item.endIndex
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("highlighted_text_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interactive Document Inspector",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Tap highlighted spans",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category color legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExtractionCategory.entries.forEach { cat ->
                    val color = getCategoryColor(cat)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.displayName.substringBefore("&").substringBefore(" ").trim(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Annotated Clickable Text
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                ClickableText(
                    text = annotatedString,
                    modifier = Modifier
                        .padding(14.dp)
                        .testTag("interactive_annotated_text"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Default
                    ),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "ITEM_ID", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                items.find { it.id == annotation.item }?.let { matchedItem ->
                                    onItemClick(matchedItem)
                                }
                            }
                    }
                )
            }
        }
    }
}
