package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

class ChatRepository(private val chatDao: ChatDao) {

    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(title: String): Int {
        val session = ChatSession(title = title)
        return chatDao.insertSession(session).toInt()
    }

    suspend fun updateSessionTitle(sessionId: Int, newTitle: String) {
        val session = ChatSession(id = sessionId, title = newTitle)
        chatDao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: Int) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun deleteMessagesForSession(sessionId: Int) {
        chatDao.deleteMessagesForSession(sessionId)
    }

    suspend fun saveMessage(sessionId: Int, role: String, text: String, isError: Boolean = false) {
        val message = ChatMessage(sessionId = sessionId, role = role, text = text, isError = isError)
        chatDao.insertMessage(message)
    }

    val allFeedbackMessages: kotlinx.coroutines.flow.Flow<List<ChatMessage>> = chatDao.getAllFeedbackMessages()

    fun searchMessages(query: String): kotlinx.coroutines.flow.Flow<List<ChatMessage>> {
        return chatDao.searchMessages(query)
    }

    suspend fun clearAllFeedback() {
        chatDao.clearAllFeedback()
    }

    suspend fun updateMessageFeedback(messageId: Int, rating: Int?, comment: String?) {
        chatDao.updateMessageFeedback(messageId, rating, comment)
    }

    suspend fun getLatestSession(): ChatSession? {
        return chatDao.getLatestSession()
    }

    suspend fun getOfflineResponse(prompt: String, languageMode: String): String {
        val cached = try {
            chatDao.getAllValidMessages()
        } catch (e: Exception) {
            emptyList()
        }
        return OfflineNepaliEngine.generateOfflineResponse(
            prompt = prompt,
            languageMode = languageMode,
            cachedMessages = cached
        )
    }

    private fun resolveCloudModelId(modelName: String): String {
        return when (modelName) {
            "nepali-oss-7b-hybrid" -> "gemini-3.5-flash"
            "offline-nepali-local" -> "gemini-3.5-flash"
            else -> modelName
        }
    }

    suspend fun getApiResponse(
        modelName: String,
        systemInstruction: String,
        history: List<ChatMessage>,
        currentPrompt: String,
        temperature: Float = 0.6f,
        languageMode: String = "Nepali",
        forceOffline: Boolean = false
    ): Result<String> {
        if (forceOffline || modelName == "offline-nepali-local") {
            return Result.success(getOfflineResponse(currentPrompt, languageMode))
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return Result.success(getOfflineResponse(currentPrompt, languageMode))
        }

        val contents = mutableListOf<Content>()
        
        // Build valid sequence of conversation contents (keep recent 16 messages for fast response)
        history.takeLast(16).forEach { msg ->
            if (!msg.isError) {
                contents.add(
                    Content(
                        parts = listOf(Part(text = msg.text)),
                        role = if (msg.role == "user") "user" else "model"
                    )
                )
            }
        }

        // Add user current request
        contents.add(
            Content(
                parts = listOf(Part(text = currentPrompt)),
                role = "user"
            )
        )

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = temperature,
                topP = 0.95f,
                maxOutputTokens = 1024
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemInstruction))
            )
        )

        val primaryModel = resolveCloudModelId(modelName)
        return try {
            val response = RetrofitClient.service.generateContent(primaryModel, apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                Result.success(responseText)
            } else {
                Result.success(getOfflineResponse(currentPrompt, languageMode))
            }
        } catch (e: Exception) {
            Result.success(getOfflineResponse(currentPrompt, languageMode))
        }
    }

    fun getApiResponseStream(
        modelName: String,
        systemInstruction: String,
        history: List<ChatMessage>,
        currentPrompt: String,
        temperature: Float = 0.6f,
        languageMode: String = "Nepali",
        forceOffline: Boolean = false
    ): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
        if (forceOffline || modelName == "offline-nepali-local") {
            val offlineText = getOfflineResponse(currentPrompt, languageMode)
            val words = offlineText.split(" ")
            for (i in words.indices) {
                emit(if (i == 0) words[i] else " ${words[i]}")
                kotlinx.coroutines.delay(14)
            }
            return@flow
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val offlineText = getOfflineResponse(currentPrompt, languageMode)
            emit(offlineText)
            return@flow
        }

        val contents = mutableListOf<Content>()
        
        // Build valid sequence of conversation contents (keep last 16 turns for ultra-fast latency)
        history.takeLast(16).forEach { msg ->
            if (!msg.isError) {
                contents.add(
                    Content(
                        parts = listOf(Part(text = msg.text)),
                        role = if (msg.role == "user") "user" else "model"
                    )
                )
            }
        }

        // Add user current request
        contents.add(
            Content(
                parts = listOf(Part(text = currentPrompt)),
                role = "user"
            )
        )

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = temperature,
                topP = 0.95f,
                maxOutputTokens = 1024
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemInstruction))
            )
        )

        val primaryModel = resolveCloudModelId(modelName)
        val fallbackModels = listOf(primaryModel, "gemini-3.1-flash-lite-preview", "gemini-flash-latest").distinct()

        var streamSucceeded = false
        for (candidateModel in fallbackModels) {
            try {
                val responseBody = RetrofitClient.service.generateContentStream(candidateModel, apiKey, request)
                val textRegex = Regex("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
                var emittedAny = false

                responseBody.byteStream().bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val match = textRegex.find(line!!)
                        if (match != null) {
                            val rawValue = match.groupValues[1]
                            val unescaped = unescapeJson(rawValue)
                            emittedAny = true
                            emit(unescaped)
                        }
                    }
                }
                if (emittedAny) {
                    streamSucceeded = true
                    break
                }
            } catch (e: Exception) {
                // Try next fallback model or fall back to on-device OfflineNepaliEngine
            }
        }

        if (!streamSucceeded) {
            val offlineText = getOfflineResponse(currentPrompt, languageMode)
            val words = offlineText.split(" ")
            for (i in words.indices) {
                emit(if (i == 0) words[i] else " ${words[i]}")
                kotlinx.coroutines.delay(14)
            }
        }
    }

    private fun unescapeJson(jsonStr: String): String {
        val sb = java.lang.StringBuilder()
        var i = 0
        while (i < jsonStr.length) {
            val c = jsonStr[i]
            if (c == '\\' && i + 1 < jsonStr.length) {
                val next = jsonStr[i + 1]
                when (next) {
                    'b' -> sb.append('\b')
                    't' -> sb.append('\t')
                    'n' -> sb.append('\n')
                    'f' -> sb.append('\r')
                    'r' -> sb.append('\r')
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/' -> sb.append('/')
                    'u' -> {
                        if (i + 5 < jsonStr.length) {
                            try {
                                val code = jsonStr.substring(i + 2, i + 6).toInt(16)
                                sb.append(code.toChar())
                                i += 4
                            } catch (e: Exception) {
                                sb.append("\\u")
                            }
                        } else {
                            sb.append("\\u")
                        }
                    }
                    else -> sb.append(next)
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}
