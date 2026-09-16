package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CustomRuleEntity
import com.example.data.HistoryEntity
import com.example.engine.ExtractionManager
import com.example.engine.GeminiExtractionService
import com.example.model.CustomExtractionRule
import com.example.model.ExtractedItem
import com.example.model.ExtractionCategory
import com.example.model.ExtractionOptions
import com.example.model.ExtractionResult
import com.example.model.SamplePreset
import com.example.model.SamplePresets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val historyDao = database.historyDao()
    private val customRuleDao = database.customRuleDao()

    val historyList: StateFlow<List<HistoryEntity>> = historyDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val customRulesEntities = customRuleDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _inputText = MutableStateFlow(SamplePresets.allPresets[0].text)
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentResult = MutableStateFlow<ExtractionResult?>(null)
    val currentResult: StateFlow<ExtractionResult?> = _currentResult.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ExtractionCategory?>(null)
    val selectedCategoryFilter: StateFlow<ExtractionCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _minConfidence = MutableStateFlow(0.60f)
    val minConfidence: StateFlow<Float> = _minConfidence.asStateFlow()

    private val _useAiEngine = MutableStateFlow(false)
    val useAiEngine: StateFlow<Boolean> = _useAiEngine.asStateFlow()

    private val _selectedItemId = MutableStateFlow<String?>(null)
    val selectedItemId: StateFlow<String?> = _selectedItemId.asStateFlow()

    val isGeminiConfigured: Boolean
        get() = GeminiExtractionService.isApiKeyAvailable()

    init {
        // Run initial extraction for the default sample so the user immediately sees rich results!
        analyze()
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
        _selectedItemId.value = null
    }

    fun loadPreset(preset: SamplePreset) {
        _inputText.value = preset.text
        _selectedItemId.value = null
        analyze()
    }

    fun setCategoryFilter(category: ExtractionCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setMinConfidence(confidence: Float) {
        _minConfidence.value = confidence
        analyze()
    }

    fun setUseAiEngine(enabled: Boolean) {
        _useAiEngine.value = enabled
        analyze()
    }

    fun selectItem(itemId: String?) {
        _selectedItemId.value = if (_selectedItemId.value == itemId) null else itemId
    }

    fun analyze() {
        val text = _inputText.value
        if (text.isBlank()) {
            _currentResult.value = null
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val rules = customRulesEntities.value.map { it.toDomain() }
                val options = ExtractionOptions(
                    activeCategories = ExtractionCategory.entries.toSet(),
                    minConfidence = _minConfidence.value,
                    useAiEngineIfAvailable = _useAiEngine.value,
                    customRules = rules
                )
                val result = ExtractionManager.analyzeText(text, options)
                _currentResult.value = result

                // Auto save to history
                if (result.items.isNotEmpty()) {
                    val title = text.take(60).replace("\n", " ").trim() + "..."
                    val topCategories = result.categoryCounts.keys
                        .joinToString(", ") { it.name }

                    historyDao.insert(
                        HistoryEntity(
                            id = result.id,
                            title = title,
                            inputText = text,
                            resultJson = ExtractionManager.resultToJson(result, 0),
                            timestamp = result.timestamp,
                            itemsCount = result.totalFound,
                            topCategories = topCategories
                        )
                    )
                }
            } catch (e: Exception) {
                // If anything fails, fallback smoothly
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun restoreHistoryItem(entity: HistoryEntity) {
        _inputText.value = entity.inputText
        viewModelScope.launch {
            try {
                val json = JSONObject(entity.resultJson)
                val itemsArray = json.optJSONArray("results") ?: JSONArray()
                val items = mutableListOf<ExtractedItem>()
                for (i in 0 until itemsArray.length()) {
                    val obj = itemsArray.getJSONObject(i)
                    val catName = obj.optString("category")
                    val cat = try {
                        ExtractionCategory.valueOf(catName)
                    } catch (_: Exception) {
                        ExtractionCategory.NAMED_ENTITY
                    }

                    val metaMap = mutableMapOf<String, String>()
                    val metaObj = obj.optJSONObject("metadata")
                    if (metaObj != null) {
                        for (k in metaObj.keys()) {
                            metaMap[k] = metaObj.optString(k)
                        }
                    }

                    items.add(
                        ExtractedItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            text = obj.optString("detectedText"),
                            category = cat,
                            subCategory = obj.optString("subCategory"),
                            startIndex = obj.optInt("startIndex"),
                            endIndex = obj.optInt("endIndex"),
                            confidenceScore = obj.optDouble("confidenceScore", 0.9).toFloat(),
                            metadata = metaMap,
                            contextSnippet = obj.optString("contextSnippet")
                        )
                    )
                }

                _currentResult.value = ExtractionResult(
                    id = entity.id,
                    inputText = entity.inputText,
                    timestamp = entity.timestamp,
                    processingTimeMs = json.optLong("processingTimeMs", 25),
                    engineName = json.optString("engine", "Restored Session"),
                    items = items,
                    metadata = mapOf("restored" to "true")
                )
            } catch (e: Exception) {
                // Re-run analysis if parse fails
                analyze()
            }
        }
    }

    fun deleteHistoryItem(entity: HistoryEntity) {
        viewModelScope.launch {
            historyDao.delete(entity)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    fun addCustomRule(
        name: String,
        regexPattern: String,
        categoryLabel: String,
        confidence: Float,
        description: String
    ) {
        viewModelScope.launch {
            val rule = CustomRuleEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                regexPattern = regexPattern,
                categoryLabel = categoryLabel,
                defaultConfidence = confidence,
                description = description,
                isEnabled = true
            )
            customRuleDao.insert(rule)
            analyze()
        }
    }

    fun toggleCustomRule(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            customRuleDao.setRuleEnabled(id, isEnabled)
            analyze()
        }
    }

    fun deleteCustomRule(rule: CustomRuleEntity) {
        viewModelScope.launch {
            customRuleDao.delete(rule)
            analyze()
        }
    }

    fun getResultJson(): String {
        val result = _currentResult.value ?: return "{}"
        return ExtractionManager.resultToJson(result, 2)
    }

    fun getResultCsv(): String {
        val result = _currentResult.value ?: return ""
        val sb = StringBuilder()
        sb.append("ID,Category,SubCategory,Confidence,DetectedText,StartIndex,EndIndex,Context\n")
        for (item in result.items) {
            val escapedText = item.text.replace("\"", "\"\"")
            val escapedContext = item.contextSnippet.replace("\"", "\"\"")
            sb.append("\"${item.id}\",\"${item.category.name}\",\"${item.subCategory}\",${item.confidenceScore},\"$escapedText\",${item.startIndex},${item.endIndex},\"$escapedContext\"\n")
        }
        return sb.toString()
    }

    fun getResultMarkdown(): String {
        val result = _currentResult.value ?: return ""
        val sb = StringBuilder()
        sb.append("# Information & Reference Extraction Report\n\n")
        sb.append("- **Engine:** ${result.engineName}\n")
        sb.append("- **Processing Time:** ${result.processingTimeMs} ms\n")
        sb.append("- **Total Entities Found:** ${result.totalFound}\n\n")

        sb.append("## Category Summary\n")
        result.categoryCounts.forEach { (cat, count) ->
            sb.append("- **${cat.displayName}:** $count\n")
        }
        sb.append("\n## Extracted Items\n\n")

        for (item in result.items) {
            sb.append("### [${item.category.name}] ${item.text}\n")
            sb.append("- **Subcategory:** ${item.subCategory}\n")
            sb.append("- **Confidence:** ${item.confidencePercentage}%\n")
            sb.append("- **Offset Range:** [${item.startIndex}..${item.endIndex}]\n")
            if (item.metadata.isNotEmpty()) {
                sb.append("- **Metadata:** ")
                sb.append(item.metadata.entries.joinToString(", ") { "${it.key}: ${it.value}" })
                sb.append("\n")
            }
            if (item.contextSnippet.isNotBlank()) {
                sb.append("- **Context:** *\"${item.contextSnippet}\"*\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
