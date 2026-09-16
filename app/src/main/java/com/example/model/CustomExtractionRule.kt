package com.example.model

data class CustomExtractionRule(
    val id: String,
    val name: String,
    val regexPattern: String,
    val categoryLabel: String,
    val defaultConfidence: Float = 0.90f,
    val description: String = "",
    val isEnabled: Boolean = true
)
