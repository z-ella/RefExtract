package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CustomExtractionRule

@Entity(tableName = "custom_rules")
data class CustomRuleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val regexPattern: String,
    val categoryLabel: String,
    val defaultConfidence: Float,
    val description: String,
    val isEnabled: Boolean
) {
    fun toDomain(): CustomExtractionRule = CustomExtractionRule(
        id = id,
        name = name,
        regexPattern = regexPattern,
        categoryLabel = categoryLabel,
        defaultConfidence = defaultConfidence,
        description = description,
        isEnabled = isEnabled
    )

    companion object {
        fun fromDomain(rule: CustomExtractionRule): CustomRuleEntity = CustomRuleEntity(
            id = rule.id,
            name = rule.name,
            regexPattern = rule.regexPattern,
            categoryLabel = rule.categoryLabel,
            defaultConfidence = rule.defaultConfidence,
            description = rule.description,
            isEnabled = rule.isEnabled
        )
    }
}
