package com.example.engine

import com.example.model.ExtractionCategory
import java.util.regex.Pattern

object RegexPatternRegistry {
    // DOIs: e.g., 10.1038/s41586-021-03819-2
    val DOI_PATTERN: Pattern = Pattern.compile(
        "\\b10\\.\\d{4,9}/[-._;()/:A-Za-z0-9]+\\b",
        Pattern.CASE_INSENSITIVE
    )

    // arXiv identifiers: e.g., arXiv:2106.09685
    val ARXIV_PATTERN: Pattern = Pattern.compile(
        "\\barXiv:\\d{4}\\.\\d{4,5}(?:v\\d+)?\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Academic author-year citations: e.g., Vaswani et al. (2017), Kaplan et al. (2020)
    val ACADEMIC_CITATION_PATTERN: Pattern = Pattern.compile(
        "\\b([A-Z][a-z]+(?:\\s+(?:et\\s+al\\.|and\\s+[A-Z][a-z]+))?)\\s*\\((19\\d{2}|20\\d{2})[a-z]?\\)"
    )

    // Bracketed citations: e.g., [1], [12, 15], [3-5]
    val BRACKETED_CITATION_PATTERN: Pattern = Pattern.compile(
        "\\[(\\d{1,3}(?:[-,\\s]+\\d{1,3})*)\\]"
    )

    // Standards & Regulatory codes: e.g., ISO 27001, IEEE Standard 754-2019, NIST AI Risk Management Framework 1.0, Directive 95/46/EC
    val STANDARD_PATTERN: Pattern = Pattern.compile(
        "\\b(ISO(?:/IEC)?|IEEE(?:\\s+Standard)?|RFC|NIST|GDPR|HIPAA|Directive|Case)\\s*[-:]?\\s*([A-Za-z0-9-./:]+(?:\\s+[A-Za-z0-9-.]+)*)\\b"
    )

    // URLs & Web references
    val URL_PATTERN: Pattern = Pattern.compile(
        "https?://[A-Za-z0-9._~:/?#\\[\\]@!$&'()*+,;=%-]+",
        Pattern.CASE_INSENSITIVE
    )

    // Direct Quotations: e.g. "...", “...”, ‘...’
    val QUOTATION_PATTERN: Pattern = Pattern.compile(
        "[\"“‘]([^\"”’]{6,400})[\"”’]"
    )

    // Currency values: e.g., $9.2 billion, €20 million, £500k
    val CURRENCY_PATTERN: Pattern = Pattern.compile(
        "(?:\\$|€|£|¥)\\s*\\d+(?:[.,]\\d+)?(?:\\s*(?:billion|million|trillion|k|B|M))?",
        Pattern.CASE_INSENSITIVE
    )

    // Percentages: e.g., 94.2%, 28.5%, 4%
    val PERCENTAGE_PATTERN: Pattern = Pattern.compile(
        "\\b\\d+(?:\\.\\d+)?%"
    )

    // Numeric metrics and physical / scientific units
    val METRIC_PATTERN: Pattern = Pattern.compile(
        "\\b\\d+(?:\\.\\d+)?\\s*(?:x|parameters|mg|hours?|days?|months?|years?|patients?|km|miles|kg|GB|MB|ms)\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Explicit Full Dates: e.g., October 24, 2024; 20 July 1969
    val FULL_DATE_PATTERN: Pattern = Pattern.compile(
        "\\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+\\d{1,2}(?:st|nd|rd|th)?,?\\s+\\d{4}\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Quarters & Fiscal Eras: e.g. Q3 2023, Q4 2024
    val QUARTER_DATE_PATTERN: Pattern = Pattern.compile(
        "\\bQ[1-4]\\s+\\d{4}\\b"
    )

    // ISO dates: 2024-10-24
    val ISO_DATE_PATTERN: Pattern = Pattern.compile(
        "\\b\\d{4}-\\d{2}-\\d{2}\\b"
    )

    // Known high-profile Organizations
    val KNOWN_ORGANIZATIONS = listOf(
        "Alphabet", "Google", "DeepMind", "OpenAI", "Microsoft", "Stanford University",
        "Royal Society", "European Union", "ClinicalTrials.gov", "NIST", "IEEE",
        "The Lancet", "Nature", "CERN", "NASA", "MIT", "World Health Organization"
    )

    // Known Locations
    val KNOWN_LOCATIONS = listOf(
        "Mountain View", "California", "Geneva", "Tokyo", "New York", "London",
        "San Francisco", "Paris", "Berlin", "Boston", "Cambridge", "Zurich", "Washington"
    )

    // Known Technology Concepts
    val KNOWN_TECHNOLOGIES = listOf(
        "transformer", "graph neural networks", "generative AI", "cloud infrastructure",
        "monoclonal antibody", "machine translation", "neural algorithmic reasoning",
        "numerical stability", "scaling laws", "symbolic systems", "connectionist models"
    )
}
