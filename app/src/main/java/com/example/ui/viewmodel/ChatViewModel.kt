package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    data class ModelOption(
        val id: String,
        val displayName: String,
        val badge: String,
        val description: String
    )

    val availableModels = listOf(
        ModelOption(
            id = "nepali-oss-7b-hybrid",
            displayName = "Nepali OSS-7B (Hybrid)",
            badge = "नेपाली मूल (Default)",
            description = "नेपाली र बहुभाषिक संवादका लागि अनुकूलित द्रुत हाइब्रिड मोडल (अफलाइन क्यास सहित)"
        ),
        ModelOption(
            id = "gemini-3.5-flash",
            displayName = "Gemini 3.5 Flash",
            badge = "द्रुत गति (Fast)",
            description = "द्रुत गति, बुद्धिमान तथा सन्तुलित नतिजा"
        ),
        ModelOption(
            id = "gemini-3.1-flash-lite-preview",
            displayName = "Gemini 3.1 Lite",
            badge = "अति द्रुत (Lite)",
            description = "न्यूनतम समयमा तत्काल जवाफ दिने हल्का मोडल"
        ),
        ModelOption(
            id = "gemini-3.1-pro-preview",
            displayName = "Gemini 3.1 Pro",
            badge = "गहिरो तर्क (Pro)",
            description = "जटिल तर्क, कोडिङ र गणितीय विश्लेषणका लागि"
        ),
        ModelOption(
            id = "offline-nepali-local",
            displayName = "On-Device Nepali (Offline)",
            badge = "अफलाइन (0ms)",
            description = "बिना इन्टरनेट चल्ने अन-डिभाइस नेपाली ज्ञानकोश र लोकल स्मृति क्यास"
        )
    )

    private val _selectedModel = MutableStateFlow("nepali-oss-7b-hybrid")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    fun setModel(modelId: String) {
        _selectedModel.value = modelId
        _isOfflineMode.value = (modelId == "offline-nepali-local")
    }

    fun toggleOfflineMode() {
        val next = !_isOfflineMode.value
        _isOfflineMode.value = next
        if (next && _selectedModel.value != "offline-nepali-local") {
            _selectedModel.value = "offline-nepali-local"
        } else if (!next && _selectedModel.value == "offline-nepali-local") {
            _selectedModel.value = "nepali-oss-7b-hybrid"
        }
    }

    private val _temperature = MutableStateFlow(0.6f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    fun setTemperature(temp: Float) {
        _temperature.value = temp
    }

    val feedbackList: StateFlow<List<ChatMessage>> = repository.allFeedbackMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val systemInstruction = """
        You are Sathi AI, a premium, highly helpful, and polite Nepal-based AI companion.
        
        CRITICAL RULES:
        - STRICT RESPONSE LENGTH BUDGET: Keep all responses short, scannable, and extremely punchy. Never exceed 120 words or 2-3 short paragraphs.
        - ZERO FILLER: Eliminate unnecessary introductory pleasantries, redundant welcoming phrases, or closing remarks. Start delivering value from the very first word.
        - HIGH SCANNABILITY: Always use bullet points, bold key terms, and short 2-3 sentence paragraphs so the user can read and comprehend the entire output in under 15 seconds.
        - Always use bolding (**text**) on key terms, figures, and names to make answers easy to scan.
        - Mirror the user's language preference seamlessly (Nepali, English, or Romanized Nepali).
        - Maintain extreme politeness with high-level honorifics (e.g., 'Hajur', 'Tapaia', 'Tapainko') when replying in Nepali or Romanized Nepali.
        
        Your core attributes:
        1. Fast, Direct & Respectful:
           - Start with the direct answer first. Keep it simple, natural, and action-focused.
        2. Creator:
           - If asked about who built or created you, state politely and briefly that you were created by a talented Class 12 student from Nepal who is dedicated to learning and building technology, and wishes to remain anonymous.
        3. Localized Context & Fintech:
           - Natively understand Nepal's digital fintech ecosystem (eSewa, Khalti, IME Pay, Fonepay, QR payments, bank transfers) and administrative geography.
        4. Transparent Boundaries:
           - If a query requires restricted live database tracking, politely state your boundaries.
    """.trimIndent()

    private val _activeStreamedText = MutableStateFlow<String?>(null)
    val activeStreamedText: StateFlow<String?> = _activeStreamedText.asStateFlow()

    val sessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    private val _inputMessage = MutableStateFlow("")
    val inputMessage: StateFlow<String> = _inputMessage.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 1. Language Mode selection state (Nepali or English)
    private val _selectedLanguageMode = MutableStateFlow("Nepali")
    val selectedLanguageMode: StateFlow<String> = _selectedLanguageMode.asStateFlow()

    // Observe messages dynamically for the current session ID
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForSession(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Automatically select or create the latest session on startup
        viewModelScope.launch {
            val latest = repository.getLatestSession()
            if (latest != null) {
                _currentSessionId.value = latest.id
            } else {
                createNewSession()
            }
        }
    }

    fun onInputChanged(text: String) {
        _inputMessage.value = text
    }

    fun selectSession(sessionId: Int) {
        _currentSessionId.value = sessionId
        _error.value = null
    }

    fun createNewSession() {
        viewModelScope.launch {
            val newId = repository.createNewSession("नयाँ च्याट (New Chat)")
            _currentSessionId.value = newId
            _error.value = null
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                val latest = repository.getLatestSession()
                if (latest != null) {
                    _currentSessionId.value = latest.id
                } else {
                    createNewSession()
                }
            }
        }
    }

    fun clearCurrentChat() {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            repository.deleteMessagesForSession(sessionId)
            repository.updateSessionTitle(sessionId, "नयाँ च्याट (New Chat)")
        }
    }

    fun setLanguageMode(mode: String) {
        _selectedLanguageMode.value = mode
    }

    fun submitFeedback(messageId: Int, rating: Int?, comment: String?) {
        viewModelScope.launch {
            repository.updateMessageFeedback(messageId, rating, comment)
        }
    }

    fun clearAllFeedback() {
        viewModelScope.launch {
            repository.clearAllFeedback()
        }
    }

    fun regenerateLastResponse(modelMessageId: Int) {
        if (_isGenerating.value) return
        val currentMsgs = messages.value
        val targetIndex = currentMsgs.indexOfFirst { it.id == modelMessageId }
        val userPromptMessage = if (targetIndex >= 0) {
            currentMsgs.subList(0, targetIndex).lastOrNull { it.role == "user" }
        } else {
            currentMsgs.lastOrNull { it.role == "user" }
        } ?: return

        sendMessage(userPromptMessage.text)
    }

    fun getSessionTranscript(): String {
        val currentMsgs = messages.value
        val sb = StringBuilder()
        sb.append("Sathi AI Chat Transcript\n")
        sb.append("Date: ").append(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())).append("\n\n")
        currentMsgs.forEach { msg ->
            val sender = if (msg.role == "user") "तपाईं (User)" else "Sathi AI"
            sb.append("[$sender]:\n").append(msg.text).append("\n\n")
        }
        return sb.toString()
    }

    fun exportFeedbackData(): String {
        val list = feedbackList.value
        val sb = StringBuilder()
        sb.append("--- Sathi AI User Feedback Report ---\n")
        sb.append("Total Responses Evaluated: ").append(list.size).append("\n")
        val positive = list.count { (it.feedbackRating ?: 0) > 0 }
        val negative = list.count { (it.feedbackRating ?: 0) < 0 }
        sb.append("Positive (👍/Stars): ").append(positive).append("\n")
        sb.append("Negative (👎): ").append(negative).append("\n\n")
        list.forEachIndexed { i, msg ->
            val ratingStr = when {
                msg.feedbackRating == 1 -> "👍 Thumbs Up"
                msg.feedbackRating == -1 -> "👎 Thumbs Down"
                (msg.feedbackRating ?: 0) in 2..5 -> "${msg.feedbackRating} ⭐ Stars"
                else -> "Unrated"
            }
            sb.append("${i + 1}. Rating: $ratingStr\n")
            if (!msg.feedbackComment.isNullOrBlank()) {
                sb.append("   User Comment: ${msg.feedbackComment}\n")
            }
            sb.append("   Response Preview: ${msg.text.take(90).replace("\n", " ")}...\n\n")
        }
        return sb.toString()
    }

    // Privacy screening layer
    private fun screenSensitiveData(prompt: String): String {
        var screened = prompt
        
        // Redact credit/debit card numbers (16-digits with spaces or hyphens)
        val cardRegex = Regex("\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b")
        screened = cardRegex.replace(screened, "[CARD NUMBER REDACTED FOR PRIVACY]")
        
        // Redact PINs, OTPs, recovery codes, bank passwords
        val pinKeywords = listOf("pin", "पिन", "password", "पासवर्ड", "otp", "ओटिपी")
        for (kw in pinKeywords) {
            val pinRegex = Regex("(?i)($kw\\s*[:=]?\\s*)(\\b\\d{4,6}\\b)")
            screened = pinRegex.replace(screened) { matchResult ->
                matchResult.groupValues[1] + "[REDACTED FOR SECURITY]"
            }
        }
        
        // Redact potential government ID numbers (citizenship numbers)
        val govtIdRegex = Regex("\\b\\d{2}-\\d{2}-\\d{2}-\\d{4,6}\\b")
        screened = govtIdRegex.replace(screened, "[ID REDACTED]")

        return screened
    }

    // Local structural abbreviation and slang mapping
    private fun resolveSlangAndShortcuts(prompt: String): String {
        var resolved = prompt
        val shortcuts = mapOf(
            "\\bktm\\b" to "Kathmandu",
            "\\bKTM\\b" to "Kathmandu",
            "\\bpkr\\b" to "Pokhara",
            "\\bPKR\\b" to "Pokhara",
            "\\bltp\\b" to "Lalitpur",
            "\\bLTP\\b" to "Lalitpur",
            "\\bbkt\\b" to "Bhaktapur",
            "\\bBKT\\b" to "Bhaktapur",
            "\\besewa\\b" to "eSewa digital wallet",
            "\\bEsewa\\b" to "eSewa digital wallet",
            "\\bkhalti\\b" to "Khalti digital wallet",
            "\\bKhalti\\b" to "Khalti digital wallet",
            "\\bimepay\\b" to "IME Pay digital wallet",
            "\\bIMEPay\\b" to "IME Pay digital wallet"
        )
        for ((pattern, replacement) in shortcuts) {
            resolved = resolved.replace(Regex(pattern), replacement)
        }
        return resolved
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isGenerating.value) return

        val sessionId = _currentSessionId.value ?: return

        viewModelScope.launch {
            _isGenerating.value = true
            _inputMessage.value = ""
            _error.value = null

            // 1. Local abbreviation and slang resolution
            val resolvedText = resolveSlangAndShortcuts(trimmed)

            // 3. Privacy screening (Redact bank info, password, IDs)
            val screenedText = screenSensitiveData(resolvedText)

            // 4. Save user's message locally
            repository.saveMessage(sessionId, "user", trimmed)

            // If it is the default first message, rename session title to a smart title based on query
            val currentMsgs = messages.value
            if (currentMsgs.isEmpty() || currentMsgs.all { it.role != "user" }) {
                val shortTitle = if (trimmed.length > 25) trimmed.take(22) + "..." else trimmed
                repository.updateSessionTitle(sessionId, shortTitle)
            }

            // Apply selected language override instructions if any
            val dynamicSystemInstruction = when (_selectedLanguageMode.value) {
                "Nepali" -> """
                    $systemInstruction
                    
                    CRITICAL LANGUAGE CONSTRAINT:
                    - You are strictly operating in NEPALI MEDIUM (primary focus on natural, idiomatic Nepali).
                    - The user can type their question in Nepali, English, or a mix of both.
                    - However, you MUST reply EXCLUSIVELY in Nepali (using standard Devanagari script). Do NOT reply in English or Romanized Nepali. Every word of your explanation, greetings, and structure must be in pure, polite Nepali.
                """.trimIndent()
                "English" -> """
                    $systemInstruction
                    
                    CRITICAL LANGUAGE CONSTRAINT:
                    - You are strictly operating in ENGLISH MEDIUM.
                    - The user can type their question in English, Nepali, or a mix of both.
                    - However, you MUST reply EXCLUSIVELY in English. Do NOT reply in Nepali or Romanized Nepali. Every word of your explanation, greetings, and structure must be in clean, natural English.
                """.trimIndent()
                else -> """
                    $systemInstruction
                    
                    MULTILINGUAL & NEPALI-FIRST CONSTRAINT:
                    - Automatically detect and mirror the user's language (Nepali Devanagari, Romanized Nepali, English, Hindi, or mixed).
                    - When the prompt is bilingual or ambiguous, prioritize clear Nepali with helpful English technical terms in parentheses.
                """.trimIndent()
            }

            val processedPromptForApi = screenedText

            _activeStreamedText.value = null
            val accumulatedText = java.lang.StringBuilder()

            try {
                repository.getApiResponseStream(
                    modelName = _selectedModel.value,
                    systemInstruction = dynamicSystemInstruction,
                    history = messages.value,
                    currentPrompt = processedPromptForApi,
                    temperature = _temperature.value,
                    languageMode = _selectedLanguageMode.value,
                    forceOffline = _isOfflineMode.value
                ).collect { chunk ->
                    accumulatedText.append(chunk)
                    _activeStreamedText.value = accumulatedText.toString()
                }

                val finalResponse = accumulatedText.toString().trim()
                if (finalResponse.isNotEmpty()) {
                    repository.saveMessage(sessionId, "model", finalResponse)
                } else {
                    _error.value = "No response received"
                    repository.saveMessage(sessionId, "model", "Error: No response. कृपया पुनः प्रयास गर्नुहोस्।", isError = true)
                }
            } catch (e: Exception) {
                val errText = e.message ?: "An unexpected error occurred"
                _error.value = errText
                repository.saveMessage(sessionId, "model", "Error: $errText. कृपया पुनः प्रयास गर्नुहोस्।", isError = true)
            } finally {
                _activeStreamedText.value = null
                _isGenerating.value = false
            }
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
