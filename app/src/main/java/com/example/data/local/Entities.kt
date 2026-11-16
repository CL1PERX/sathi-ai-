package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val feedbackRating: Int? = null, // null = none, 1 = thumbs up, -1 = thumbs down
    val feedbackComment: String? = null
)
