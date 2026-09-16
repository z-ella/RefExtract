package com.example.engine

import android.util.Log
import com.example.BuildConfig
import com.example.model.ExtractedItem
import com.example.model.ExtractionCategory
import com.example.model.ExtractionOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object GeminiExtractionService {
    private const val TAG = "GeminiExtraction"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun isApiKeyAvailable(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun extractWithGemini(
        text: String,
        options: ExtractionOptions
    ): List<ExtractedItem>? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyAvailable()) {
            return@withContext null
        }

        try {
            val prompt = """
                Analyze the following text and extract all:
                1. References and Citations (DOIs, academic citations, standards, arXiv, URLs)
                2. Quotations (with detected speaker attribution)
                3. Named Entities (People, Organizations, Locations, Technologies)
                4. Metrics & Quantities (Currencies, percentages, measurements)
                5. Dates and Temporal indicators

                Input Text:
                \"\"\"$text\"\"\"

                Return ONLY a JSON array of objects with these exact keys:
                - "text": string (the exact detected snippet from the input text)
                - "category": string (MUST be one of: "REFERENCE", "QUOTATION", "NAMED_ENTITY", "METRIC_STAT", "TEMPORAL_DATE")
                - "subCategory": string (e.g. "DOI", "SPEAKER_QUOTE", "PERSON", "ORGANIZATION", "CURRENCY", "PERCENTAGE", "STANDARD")
                - "confidence": number (float between 0.0 and 1.0)
                - "metadata": object with key-value string pairs (e.g. {"speaker": "...", "doi": "...", "unit": "..."})
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API error: ${response.code} $responseBody")
                return@withContext null
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null

            val rawOutput = parts.getJSONObject(0).optString("text", "")
            if (rawOutput.isBlank()) return@withContext null

            parseGeminiOutput(rawOutput, text, options)
        } catch (e: Exception) {
            Log.e(TAG, "Extraction failed: ${e.message}", e)
            null
        }
    }

    private fun parseGeminiOutput(
        jsonString: String,
        originalText: String,
        options: ExtractionOptions
    ): List<ExtractedItem> {
        val result = mutableListOf<ExtractedItem>()
        try {
            // Trim any markdown backticks if present
            val cleaned = jsonString.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val jsonArray = JSONArray(cleaned)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val detectedText = obj.optString("text", "").trim()
                if (detectedText.isBlank()) continue

                val categoryStr = obj.optString("category", "NAMED_ENTITY")
                val category = try {
                    ExtractionCategory.valueOf(categoryStr)
                } catch (_: Exception) {
                    when {
                        categoryStr.contains("REF", true) -> ExtractionCategory.REFERENCE
                        categoryStr.contains("QUOTE", true) -> ExtractionCategory.QUOTATION
                        categoryStr.contains("METRIC", true) -> ExtractionCategory.METRIC_STAT
                        categoryStr.contains("DATE", true) -> ExtractionCategory.TEMPORAL_DATE
                        else -> ExtractionCategory.NAMED_ENTITY
                    }
                }

                if (!options.activeCategories.contains(category)) continue

                val confidence = obj.optDouble("confidence", 0.90).toFloat()
                if (confidence < options.minConfidence) continue

                val subCategory = obj.optString("subCategory", category.name)

                val metaMap = mutableMapOf<String, String>()
                val metaObj = obj.optJSONObject("metadata")
                if (metaObj != null) {
                    for (key in metaObj.keys()) {
                        metaMap[key] = metaObj.optString(key, "")
                    }
                }

                // Locate offsets in original text
                var startIndex = originalText.indexOf(detectedText)
                var endIndex = if (startIndex >= 0) startIndex + detectedText.length else -1

                if (startIndex < 0) {
                    // Try case-insensitive matching
                    startIndex = originalText.indexOf(detectedText, ignoreCase = true)
                    endIndex = if (startIndex >= 0) startIndex + detectedText.length else -1
                }

                val snippet = if (startIndex >= 0) {
                    val s = (startIndex - 40).coerceAtLeast(0)
                    val e = (endIndex + 40).coerceAtMost(originalText.length)
                    "..." + originalText.substring(s, e) + "..."
                } else {
                    detectedText
                }

                result.add(
                    ExtractedItem(
                        id = UUID.randomUUID().toString(),
                        text = detectedText,
                        category = category,
                        subCategory = subCategory,
                        startIndex = startIndex.coerceAtLeast(0),
                        endIndex = if (endIndex >= 0) endIndex else detectedText.length,
                        confidenceScore = confidence,
                        metadata = metaMap,
                        contextSnippet = snippet
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: ${e.message}", e)
        }
        return result
    }
}
