package com.example

import com.example.engine.LocalExtractionEngine
import com.example.model.CustomExtractionRule
import com.example.model.ExtractionCategory
import com.example.model.ExtractionOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testReferenceExtraction_doiAndCitation() {
        val sample = "Published in Nature (DOI: 10.1038/s41586-021-03819-2) following Vaswani et al. (2017)."
        val options = ExtractionOptions(
            activeCategories = setOf(ExtractionCategory.REFERENCE),
            minConfidence = 0.5f
        )
        val results = LocalExtractionEngine.extract(sample, options)

        val doiItem = results.find { it.subCategory == "DOI" }
        assertNotNull("DOI item should be extracted", doiItem)
        assertEquals("10.1038/s41586-021-03819-2", doiItem?.text)

        val citeItem = results.find { it.subCategory == "ACADEMIC_CITATION" }
        assertNotNull("Academic citation should be extracted", citeItem)
        assertEquals("Vaswani et al. (2017)", citeItem?.text)
        assertEquals("Vaswani et al.", citeItem?.metadata?.get("author"))
        assertEquals("2017", citeItem?.metadata?.get("year"))
    }

    @Test
    fun testQuotationExtractionWithAttribution() {
        val sample = """Alphabet CEO Sundar Pichai addressed the team. "We are seeing generative AI drive over 35% productivity gains," Pichai declared during the keynote."""
        val options = ExtractionOptions(
            activeCategories = setOf(ExtractionCategory.QUOTATION),
            minConfidence = 0.5f
        )
        val results = LocalExtractionEngine.extract(sample, options)

        val quoteItem = results.find { it.category == ExtractionCategory.QUOTATION }
        assertNotNull("Quotation should be extracted", quoteItem)
        assertTrue(quoteItem!!.text.contains("generative AI drive over 35%"))
        assertEquals("Pichai", quoteItem.metadata["attributed_speaker"])
        assertEquals("declared", quoteItem.metadata["attribution_verb"])
    }

    @Test
    fun testNamedEntityAndMetricExtraction() {
        val sample = "Dr. Demis Hassabis at DeepMind in London reported $9.2 billion in revenue."
        val options = ExtractionOptions(
            activeCategories = setOf(ExtractionCategory.NAMED_ENTITY, ExtractionCategory.METRIC_STAT),
            minConfidence = 0.5f
        )
        val results = LocalExtractionEngine.extract(sample, options)

        val personItem = results.find { it.subCategory == "PERSON" }
        assertNotNull("Person entity should be extracted", personItem)
        assertEquals("Dr. Demis Hassabis", personItem?.text)

        val orgItem = results.find { it.subCategory == "ORGANIZATION" }
        assertNotNull("Organization entity should be extracted", orgItem)
        assertEquals("DeepMind", orgItem?.text)

        val locItem = results.find { it.subCategory == "LOCATION" }
        assertNotNull("Location should be extracted", locItem)
        assertEquals("London", locItem?.text)

        val currItem = results.find { it.subCategory == "FINANCIAL_CURRENCY" }
        assertNotNull("Currency should be extracted", currItem)
        assertTrue(currItem!!.text.contains("$9.2 billion"))
    }

    @Test
    fun testExtensibleCustomRule() {
        val sample = "Please check incident JIRA-8492 and PROD-102 immediately."
        val customRule = CustomExtractionRule(
            id = "test_jira",
            name = "Jira Ticket",
            regexPattern = "\\b[A-Z]{2,6}-\\d+\\b",
            categoryLabel = "TICKET_ID",
            defaultConfidence = 0.95f
        )
        val options = ExtractionOptions(
            activeCategories = setOf(ExtractionCategory.CUSTOM_RULE),
            minConfidence = 0.5f,
            customRules = listOf(customRule)
        )
        val results = LocalExtractionEngine.extract(sample, options)

        assertEquals(2, results.size)
        assertEquals("JIRA-8492", results[0].text)
        assertEquals("PROD-102", results[1].text)
        assertEquals("TICKET_ID", results[0].subCategory)
    }
}
