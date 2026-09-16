package com.example.engine

import com.example.model.ExtractedItem
import com.example.model.ExtractionOptions
import com.example.model.ExtractionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ExtractionManager {

    suspend fun analyzeText(
        text: String,
        options: ExtractionOptions
    ): ExtractionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        var engineUsed = "Local High-Precision Engine"
        var items: List<ExtractedItem>? = null

        if (options.useAiEngineIfAvailable && GeminiExtractionService.isApiKeyAvailable()) {
            val aiItems = GeminiExtractionService.extractWithGemini(text, options)
            if (aiItems != null && aiItems.isNotEmpty()) {
                items = aiItems
                engineUsed = "Gemini 3.5 Flash Neural Engine"
            }
        }

        if (items == null) {
            items = LocalExtractionEngine.extract(text, options)
        }

        val elapsed = System.currentTimeMillis() - startTime

        ExtractionResult(
            id = UUID.randomUUID().toString(),
            inputText = text,
            timestamp = System.currentTimeMillis(),
            processingTimeMs = elapsed,
            engineName = engineUsed,
            items = items,
            metadata = mapOf(
                "charCount" to text.length.toString(),
                "wordCount" to text.split(Regex("\\s+")).filter { it.isNotBlank() }.size.toString(),
                "confidenceThreshold" to options.minConfidence.toString()
            )
        )
    }

    fun resultToJson(result: ExtractionResult, indent: Int = 2): String {
        val root = JSONObject()
        root.put("status", "success")
        root.put("id", result.id)
        root.put("timestamp", result.timestamp)
        root.put("processingTimeMs", result.processingTimeMs)
        root.put("engine", result.engineName)
        root.put("totalExtracted", result.totalFound)

        val metaObj = JSONObject()
        result.metadata.forEach { (k, v) -> metaObj.put(k, v) }
        root.put("metadata", metaObj)

        val itemsArray = JSONArray()
        for (item in result.items) {
            val itemObj = JSONObject()
            itemObj.put("id", item.id)
            itemObj.put("detectedText", item.text)
            itemObj.put("category", item.category.name)
            itemObj.put("subCategory", item.subCategory)
            itemObj.put("startIndex", item.startIndex)
            itemObj.put("endIndex", item.endIndex)
            itemObj.put("confidenceScore", item.confidenceScore)

            val itemMeta = JSONObject()
            item.metadata.forEach { (k, v) -> itemMeta.put(k, v) }
            itemObj.put("metadata", itemMeta)
            itemObj.put("contextSnippet", item.contextSnippet)

            itemsArray.put(itemObj)
        }
        root.put("results", itemsArray)

        return if (indent > 0) root.toString(indent) else root.toString()
    }

    fun generateOpenApiSpec(): String {
        return """
openapi: 3.0.3
info:
  title: Reference and Information Extraction API
  version: 1.0.0
  description: High-precision NLP and semantic module for analyzing unstructured text to extract citations, quotations with attributions, named entities, and metadata.
paths:
  /api/v1/extract:
    post:
      summary: Extract references, quotes, entities, and metrics from text
      operationId: extractInformation
      requestBody:
        required: true
        content:
          application/json:
            schema:
              ${'$'}ref: '#/components/schemas/ExtractionRequest'
      responses:
        '200':
          description: Structured extraction successful
          content:
            application/json:
              schema:
                ${'$'}ref: '#/components/schemas/ExtractionResponse'
        '400':
          description: Invalid request payload or empty text
components:
  schemas:
    ExtractionRequest:
      type: object
      required:
        - text
      properties:
        text:
          type: string
          description: Unstructured document or text block to analyze
          example: "In breakthroughs published by Vaswani et al. (2017), transformer architectures achieved 94.2% accuracy."
        options:
          type: object
          properties:
            categories:
              type: array
              items:
                type: string
                enum: [REFERENCE, QUOTATION, NAMED_ENTITY, METRIC_STAT, TEMPORAL_DATE]
            minConfidence:
              type: number
              format: float
              default: 0.6
    ExtractionResponse:
      type: object
      properties:
        status:
          type: string
          example: success
        id:
          type: string
        processingTimeMs:
          type: integer
        totalExtracted:
          type: integer
        results:
          type: array
          items:
            ${'$'}ref: '#/components/schemas/ExtractedItem'
    ExtractedItem:
      type: object
      properties:
        detectedText:
          type: string
        category:
          type: string
        subCategory:
          type: string
        startIndex:
          type: integer
        endIndex:
          type: integer
        confidenceScore:
          type: number
        metadata:
          type: object
          additionalProperties:
            type: string
        """.trimIndent()
    }
}
