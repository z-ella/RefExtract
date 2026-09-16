package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extraction_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val inputText: String,
    val resultJson: String,
    val timestamp: Long,
    val itemsCount: Int,
    val topCategories: String
)
