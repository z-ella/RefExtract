package com.example.model

data class ExtractionOptions(
    val activeCategories: Set<ExtractionCategory> = ExtractionCategory.entries.toSet(),
    val minConfidence: Float = 0.60f,
    val useAiEngineIfAvailable: Boolean = false,
    val customRules: List<CustomExtractionRule> = emptyList()
)

data class ExtractionResult(
    val id: String,
    val inputText: String,
    val timestamp: Long,
    val processingTimeMs: Long,
    val engineName: String,
    val items: List<ExtractedItem>,
    val metadata: Map<String, String> = emptyMap()
) {
    val totalFound: Int
        get() = items.size

    val categoryCounts: Map<ExtractionCategory, Int>
        get() = items.groupBy { it.category }.mapValues { it.value.size }
}
