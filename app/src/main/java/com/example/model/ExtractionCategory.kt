package com.example.model

enum class ExtractionCategory(
    val displayName: String,
    val hexColor: Long,
    val iconName: String,
    val description: String
) {
    REFERENCE(
        displayName = "References & Citations",
        hexColor = 0xFF7C4DFF, // Purple / Violet
        iconName = "MenuBook",
        description = "Academic citations (APA/IEEE), DOIs, arXiv IDs, URLs, and standard codes"
    ),
    QUOTATION(
        displayName = "Quotations",
        hexColor = 0xFFFFAB00, // Amber
        iconName = "FormatQuote",
        description = "Direct quotes with speaker attribution, declarations, and citations"
    ),
    NAMED_ENTITY(
        displayName = "Named Entities",
        hexColor = 0xFF00B0FF, // Blue
        iconName = "Badge",
        description = "Persons, organizations, geographic locations, and technology frameworks"
    ),
    METRIC_STAT(
        displayName = "Metrics & Quantities",
        hexColor = 0xFF00E676, // Green / Emerald
        iconName = "Analytics",
        description = "Percentages, currencies, statistics, and measurements with units"
    ),
    TEMPORAL_DATE(
        displayName = "Dates & Temporal",
        hexColor = 0xFF00B4D8, // Teal / Cyan
        iconName = "Event",
        description = "Calendar dates, timestamps, quarters, and historical eras"
    ),
    CUSTOM_RULE(
        displayName = "Custom Extracted",
        hexColor = 0xFFFF4081, // Pink / Rose
        iconName = "Rule",
        description = "User-defined regex pattern matchers and domain-specific entities"
    )
}
