package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long

    @Update
    suspend fun updateSession(session: ChatSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("UPDATE chat_messages SET feedbackRating = :rating, feedbackComment = :comment WHERE id = :messageId")
    suspend fun updateMessageFeedback(messageId: Int, rating: Int?, comment: String?)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Int)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Int)

    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSession(): ChatSession?

    @Query("SELECT * FROM chat_messages WHERE feedbackRating IS NOT NULL ORDER BY timestamp DESC")
    fun getAllFeedbackMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE isError = 0 ORDER BY timestamp ASC")
    suspend fun getAllValidMessages(): List<ChatMessage>

    @Query("UPDATE chat_messages SET feedbackRating = NULL, feedbackComment = NULL")
    suspend fun clearAllFeedback()
}
