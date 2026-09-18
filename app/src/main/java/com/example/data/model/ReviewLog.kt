package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "review_logs")
data class ReviewLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val wordId: Int,
    val rating: Int, // 1: Again, 2: Hard, 3: Good, 4: Easy
    val exerciseType: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
