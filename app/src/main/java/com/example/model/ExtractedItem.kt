package com.example.model

data class ExtractedItem(
    val id: String,
    val text: String,
    val category: ExtractionCategory,
    val subCategory: String,
    val startIndex: Int,
    val endIndex: Int,
    val confidenceScore: Float, // 0.0 to 1.0
    val metadata: Map<String, String> = emptyMap(),
    val contextSnippet: String = ""
) {
    val confidencePercentage: Int
        get() = (confidenceScore * 100).toInt().coerceIn(0, 100)
}
