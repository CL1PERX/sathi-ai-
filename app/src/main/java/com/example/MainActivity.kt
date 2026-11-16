package com.example

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import android.content.Context
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.ChatSession
import com.example.data.repository.ChatRepository
import com.example.ui.components.ChatBubble
import com.example.ui.components.SkeletonChatBubble
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize local database and repository
        val database = AppDatabase.getDatabase(this)
        val chatRepository = ChatRepository(database.chatDao())

        setContent {
            MyApplicationTheme {
                // Initialize VM with manual factory inject
                val chatViewModel: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory(chatRepository)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdaptiveLayout(viewModel = chatViewModel)
                }
            }
        }
    }
}

@Composable
fun AdaptiveLayout(viewModel: ChatViewModel) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isDesktopOrTablet = configuration.screenWidthDp >= 600

    if (isDesktopOrTablet) {
        // Desktop / Tablet side-by-side view (Split Columns)
        Row(modifier = Modifier.fillMaxSize()) {
            // Left sidebar: Session list
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(PremiumSurface)
            ) {
                SidebarContent(
                    sessions = sessions,
                    currentSessionId = currentSessionId,
                    onSessionSelected = { viewModel.selectSession(it) },
                    onNewSession = { viewModel.createNewSession() },
                    onDeleteSession = { viewModel.deleteSession(it) }
                )
            }

            // Elegant Vertical Divider
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(BorderColor)
            )

            // Right content: Conversation View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ChatScreen(
                    viewModel = viewModel,
                    isDesktop = true,
                    onOpenDrawer = {} // Unused in Desktop
                )
            }
        }
    } else {
        // Mobile layout: Drawer drawer + Main View
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = PremiumSurface,
                    modifier = Modifier.width(280.dp)
                ) {
                    SidebarContent(
                        sessions = sessions,
                        currentSessionId = currentSessionId,
                        onSessionSelected = {
                            viewModel.selectSession(it)
                            scope.launch { drawerState.close() }
                        },
                        onNewSession = {
                            viewModel.createNewSession()
                            scope.launch { drawerState.close() }
                        },
                        onDeleteSession = { viewModel.deleteSession(it) }
                    )
                }
            }
        ) {
            ChatScreen(
                viewModel = viewModel,
                isDesktop = false,
                onOpenDrawer = {
                    scope.launch { drawerState.open() }
                }
            )
        }
    }
}

@Composable
fun SidebarContent(
    sessions: List<ChatSession>,
    currentSessionId: Int?,
    onSessionSelected: (Int) -> Unit,
    onNewSession: () -> Unit,
    onDeleteSession: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // App header inside sidebar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sathi ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimaryDark
                )
                Text(
                    text = "AI",
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp,
                    color = TextSecondaryDark
                )
            }
        }

        // New Chat Button
        Button(
            onClick = onNewSession,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(PrimaryNeonBlue, SecondaryNeonPurple)),
                    shape = RoundedCornerShape(12.dp)
                )
                .testTag("new_chat_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = PrimaryNeonBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "नयाँ च्याट (New Chat)",
                    color = PrimaryNeonBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // List of sessions with Search
        var sessionSearchQuery by remember { mutableStateOf("") }
        val filteredSessions = remember(sessions, sessionSearchQuery) {
            if (sessionSearchQuery.isBlank()) sessions
            else sessions.filter { it.title.contains(sessionSearchQuery, ignoreCase = true) }
        }

        if (sessions.size > 2) {
            TextField(
                value = sessionSearchQuery,
                onValueChange = { sessionSearchQuery = it },
                placeholder = { Text("च्याट खोज्नुहोस्...", fontSize = 11.sp, color = TextSecondaryDark) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PremiumSurface,
                    unfocusedContainerColor = PremiumSurface,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("session_search_field")
            )
        }

        Text(
            text = "हालका च्याटहरू (Recent Chats)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondaryDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredSessions) { session ->
                val isSelected = session.id == currentSessionId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BorderColor else Color.Transparent)
                        .clickable { onSessionSelected(session.id) }
                        .padding(horizontal = 10.dp, vertical = 12.dp)
                        .testTag("chat_session_item_${session.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = if (isSelected) PrimaryNeonBlue else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = session.title,
                            color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Delete Session Button (Only show delete option for items)
                    IconButton(
                        onClick = { onDeleteSession(session.id) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Session",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Sidebar Footer
        Text(
            text = "Sathi AI v1.0.0 • Offline Persistence",
            fontSize = 10.sp,
            color = TextSecondaryDark.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    isDesktop: Boolean,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputMessage by viewModel.inputMessage.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val selectedLanguageMode by viewModel.selectedLanguageMode.collectAsStateWithLifecycle()
    val activeStreamedText by viewModel.activeStreamedText.collectAsStateWithLifecycle()

    // Tactile Haptic Engine Reactive Integrator
    var hasTriggeredFirstTokenByStream by remember { mutableStateOf(false) }
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            hasTriggeredFirstTokenByStream = false
            com.example.ui.haptic.SathiHapticEngine.trigger(context, com.example.ui.haptic.HapticHook.SATHI_THINKING_START)
        } else {
            if (messages.isNotEmpty()) {
                if (error != null) {
                    com.example.ui.haptic.SathiHapticEngine.trigger(context, com.example.ui.haptic.HapticHook.NETWORK_LATENCY_ERROR)
                } else {
                    if (!hasTriggeredFirstTokenByStream) {
                        com.example.ui.haptic.SathiHapticEngine.trigger(context, com.example.ui.haptic.HapticHook.FIRST_TOKEN_DELIVERY)
                        kotlinx.coroutines.delay(120)
                    }
                    com.example.ui.haptic.SathiHapticEngine.trigger(context, com.example.ui.haptic.HapticHook.STREAM_COMPLETE)
                }
            }
        }
    }

    LaunchedEffect(activeStreamedText) {
        if (isGenerating && !activeStreamedText.isNullOrEmpty() && !hasTriggeredFirstTokenByStream) {
            hasTriggeredFirstTokenByStream = true
            com.example.ui.haptic.SathiHapticEngine.trigger(context, com.example.ui.haptic.HapticHook.FIRST_TOKEN_DELIVERY)
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0]
                viewModel.onInputChanged(spokenText)
            }
        }
    }

    val startSpeechRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val langLocale = if (selectedLanguageMode == "Nepali") "ne-NP" else "en-US"
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langLocale)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langLocale)
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (selectedLanguageMode == "Nepali") "कृपया बोल्नुहोस्..." else "Speak now...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    // Sathi AI "Good Talking" Text-to-Speech playback manager
    val tts = remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var currentlySpeakingMessageId by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(context) {
        val ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        ttsInstance.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (utteranceId != null) {
                    val parts = utteranceId.split("_chunk_")
                    if (parts.size == 2) {
                        val msgIdStr = parts[0]
                        val indexParts = parts[1].split("_of_")
                        if (indexParts.size == 2) {
                            val currentIndex = indexParts[0].toIntOrNull() ?: 0
                            val totalSize = indexParts[1].toIntOrNull() ?: 1
                            if (currentIndex == totalSize - 1) {
                                currentlySpeakingMessageId = null
                            }
                        }
                    }
                }
            }
            override fun onError(utteranceId: String?) {
                currentlySpeakingMessageId = null
            }
        })
        tts.value = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val onSpeakMessage: (com.example.data.local.ChatMessage) -> Unit = { msg ->
        val engine = tts.value
        if (engine != null && isTtsReady) {
            if (currentlySpeakingMessageId == msg.id) {
                engine.stop()
                currentlySpeakingMessageId = null
            } else {
                engine.stop()
                val rawText = msg.text
                val cleanedText = rawText
                    .replace(Regex("\\*\\*|\\*|_|`|#"), "")
                    .replace(Regex("<[^>]*>"), "")
                
                val sentenceRegex = Regex("[.!?।\n]+")
                val chunks = cleanedText.split(sentenceRegex)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (chunks.isNotEmpty()) {
                    val isNepali = cleanedText.any { it.code in 0x0900..0x097F }
                    if (isNepali) {
                        engine.language = java.util.Locale("ne", "NP")
                    } else {
                        engine.language = java.util.Locale.US
                    }
                    
                    currentlySpeakingMessageId = msg.id
                    chunks.forEachIndexed { index, chunk ->
                        val queueMode = if (index == 0) {
                            android.speech.tts.TextToSpeech.QUEUE_FLUSH
                        } else {
                            android.speech.tts.TextToSpeech.QUEUE_ADD
                        }
                        val utteranceId = "${msg.id}_chunk_${index}_of_${chunks.size}"
                        engine.speak(chunk, queueMode, null, utteranceId)
                    }
                }
            }
        } else {
            android.widget.Toast.makeText(context, "आवाज प्रणाली तयार हुँदैछ...", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showModelSettings by remember { mutableStateOf(false) }
    var showFeedbackInsights by remember { mutableStateOf(false) }
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()

    // Smooth show/hide state for the floating "scroll to bottom" arrow button
    val showScrollToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1 || 
            (listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset > 100)
        }
    }

    // Automatically scroll to the bottom when messages load, change, or are updated (e.g., word-by-word streaming)
    LaunchedEffect(messages.size, messages.lastOrNull()?.text, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showModelSettings) {
        ModelSettingsDialog(
            availableModels = viewModel.availableModels,
            selectedModelId = selectedModel,
            currentTemperature = temperature,
            onModelSelected = { viewModel.setModel(it) },
            onTemperatureChanged = { viewModel.setTemperature(it) },
            onDismiss = { showModelSettings = false }
        )
    }

    if (showFeedbackInsights) {
        FeedbackInsightsDialog(
            feedbackMessages = feedbackList,
            onExportData = {
                val exportText = viewModel.exportFeedbackData()
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, exportText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Export Feedback"))
            },
            onClearAllFeedback = {
                viewModel.clearAllFeedback()
            },
            onDismiss = { showFeedbackInsights = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumSurfaceHeader,
                    titleContentColor = TextPrimaryDark
                ),
                title = {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sathi ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "AI",
                                fontWeight = FontWeight.Light,
                                fontSize = 18.sp,
                                color = TextSecondaryDark
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .clickable { viewModel.toggleOfflineMode() }
                                .testTag("offline_mode_toggle_btn")
                        ) {
                            val statusColor = if (isOfflineMode) Color(0xFF38BDF8) else Color(0xFF4ADE80)
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isOfflineMode) "अफलाइन मोड (Offline 0ms)" else "अनलाइन • अफलाइन क्यास तयार",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (!isDesktop) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu_drawer_content_desc),
                                tint = TextPrimaryDark
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PremiumSurface,
                        border = BorderStroke(1.dp, PrimaryNeonBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { showModelSettings = true }
                            .testTag("model_selector_topbar_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Model",
                                tint = PrimaryNeonBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val currentModelObj = viewModel.availableModels.find { it.id == selectedModel }
                            Text(
                                text = (currentModelObj?.displayName ?: "Gemini").replace("Gemini ", ""),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        }
                    }
                    IconButton(
                        onClick = { showFeedbackInsights = true },
                        modifier = Modifier.size(32.dp).testTag("feedback_insights_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Feedback Insights",
                            tint = if (feedbackList.isNotEmpty()) PrimaryNeonBlue else TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            val transcript = viewModel.getSessionTranscript()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, transcript)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Chat Transcript"))
                        },
                        modifier = Modifier.size(32.dp).testTag("share_chat_transcript_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Transcript",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    TextButton(
                        onClick = { viewModel.clearCurrentChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Text(
                            text = "खाली",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = PremiumSurfaceHeader,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 650.dp)
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Small premium language selector centered right above the input bar
                        LanguageSelector(
                            selectedLanguage = selectedLanguageMode,
                            onLanguageSelected = { viewModel.setLanguageMode(it) },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Chat inputs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = inputMessage,
                                onValueChange = { viewModel.onInputChanged(it) },
                                placeholder = {
                                    Text(
                                        text = if (selectedLanguageMode == "Nepali") "Sathi AI लाई सोध्नुहोस्..." else "Ask Sathi AI...",
                                        fontSize = 13.sp,
                                        color = TextSecondaryDark
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = PremiumSurface,
                                    unfocusedContainerColor = PremiumSurface,
                                    disabledContainerColor = PremiumSurface,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(24.dp),
                                keyboardOptions = KeyboardOptions(
                                    autoCorrect = false, // Defensive input configuration
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                                    .testTag("chat_input_field"),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Stylish native voice button
                            IconButton(
                                onClick = {
                                    startSpeechRecognition()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(PremiumSurface)
                                    .border(1.dp, BorderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = PrimaryNeonBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            val buttonBrush = Brush.linearGradient(
                                colors = listOf(PrimaryNeonBlue, SecondaryNeonPurple)
                            )

                            IconButton(
                                onClick = {
                                    if (inputMessage.isNotBlank()) {
                                        com.example.ui.haptic.SathiHapticEngine.trigger(context, com.example.ui.haptic.HapticHook.USER_SEND_PRESS)
                                        viewModel.sendMessage(inputMessage)
                                    }
                                },
                                enabled = inputMessage.isNotBlank() && !isGenerating,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .drawBehind {
                                        if (inputMessage.isNotBlank() && !isGenerating) {
                                            drawCircle(brush = buttonBrush)
                                        } else {
                                            drawCircle(color = BorderColor)
                                        }
                                    }
                                    .testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.send_message),
                                    tint = if (inputMessage.isNotBlank() && !isGenerating) OLEDBackground else TextSecondaryDark.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        // Background canvas for abstract modern atmosphere
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OLEDBackground)
                .drawBehind {
                    // Subtle cyber gradient in bottom right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SecondaryNeonPurple.copy(alpha = 0.04f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width,
                        center = androidx.compose.ui.geometry.Offset(size.width, size.height)
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 650.dp)
                    .align(Alignment.TopCenter)
            ) {
                if (messages.isEmpty()) {
                // PREMIUM BOLD TYPOGRAPHY EMPTY STATE - CENTERED & COMPACT
                var startIntroAnim by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    startIntroAnim = true
                }

                // Mixing & Spinning Background Glow States
                val infiniteTransition = rememberInfiniteTransition(label = "mixing_bg")
                val orbitAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(5000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "orbit"
                )
                val orbitDistance by animateDpAsState(
                    targetValue = if (startIntroAnim) 16.dp else 130.dp,
                    animationSpec = tween(1800, easing = EaseOut),
                    label = "distance"
                )

                // Logo Animation States
                val logoScale by animateFloatAsState(
                    targetValue = if (startIntroAnim) 1f else 0.1f,
                    animationSpec = tween(1500, easing = EaseOut),
                    label = "logo_scale"
                )
                val logoRotation by animateFloatAsState(
                    targetValue = if (startIntroAnim) 0f else 720f,
                    animationSpec = tween(1800, easing = EaseOutCubic),
                    label = "logo_rotation"
                )
                val logoAlpha by animateFloatAsState(
                    targetValue = if (startIntroAnim) 1f else 0f,
                    animationSpec = tween(1400, easing = EaseInOutQuad),
                    label = "logo_alpha"
                )

                // Content Reveal States
                val contentAlpha by animateFloatAsState(
                    targetValue = if (startIntroAnim) 1f else 0f,
                    animationSpec = tween(1200, delayMillis = 400, easing = EaseInOutQuad),
                    label = "content_alpha"
                )
                val contentOffset by animateDpAsState(
                    targetValue = if (startIntroAnim) 0.dp else 30.dp,
                    animationSpec = tween(1200, delayMillis = 400, easing = EaseOutCubic),
                    label = "content_offset"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Pulsing precision Sathi AI Logo with custom spinning and mixing background
                    Box(
                        modifier = Modifier
                            .padding(bottom = 28.dp)
                            .size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // The Mixing Glows (Blue and Purple orbiting, then fusing)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val r = orbitDistance.toPx()
                                    val angleRad = Math.toRadians(orbitAngle.toDouble())
                                    
                                    // Primary Neon Blue Orb
                                    val x1 = (center.x + r * Math.cos(angleRad)).toFloat()
                                    val y1 = (center.y + r * Math.sin(angleRad)).toFloat()
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(PrimaryNeonBlue.copy(alpha = 0.45f), Color.Transparent),
                                            center = androidx.compose.ui.geometry.Offset(x1, y1),
                                            radius = size.width * 0.45f
                                        ),
                                        radius = size.width * 0.45f,
                                        center = androidx.compose.ui.geometry.Offset(x1, y1)
                                    )
                                    
                                    // Secondary Neon Purple Orb
                                    val x2 = (center.x + r * Math.cos(angleRad + Math.PI)).toFloat()
                                    val y2 = (center.y + r * Math.sin(angleRad + Math.PI)).toFloat()
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(SecondaryNeonPurple.copy(alpha = 0.45f), Color.Transparent),
                                            center = androidx.compose.ui.geometry.Offset(x2, y2),
                                            radius = size.width * 0.45f
                                        ),
                                        radius = size.width * 0.45f,
                                        center = androidx.compose.ui.geometry.Offset(x2, y2)
                                    )
                                }
                        )

                        SathiLogo(
                            modifier = Modifier
                                .size(130.dp)
                                .graphicsLayer(
                                    scaleX = logoScale,
                                    scaleY = logoScale,
                                    rotationZ = logoRotation,
                                    alpha = logoAlpha
                                ),
                            pulse = true
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer(alpha = contentAlpha)
                            .offset(y = contentOffset)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "नमस्ते",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 38.sp,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🙏",
                                fontSize = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "How can I assist today?",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryNeonBlue,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "नेपाली मूल • बहुभाषिक • अफलाइन स्मृति क्यास (Offline Ready)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val quickPrompts = listOf(
                            "⚡ अफलाइन क्षमता (Offline Info)" to "के Sathi AI लाई अफलाइन (Offline Capable) बनाउन सकिन्छ र यसले कसरी काम गर्छ?",
                            "📐 रोपनी र आना हिसाब" to "१ रोपनी र १ बिघामा कति आना र वर्ग फिट हुन्छ?",
                            "💳 eSewa र Fonepay गाइड" to "नेपालमा eSewa, Khalti र Fonepay सुरक्षित रूपमा कसरी प्रयोग गर्ने?",
                            "🌐 नेपाली र बहुभाषिक अनुवाद" to "नेपालको भौगोलिक र सांस्कृतिक विविधताको संक्षिप्त परिचय दिनुहोस्।"
                        )

                        Column(
                            modifier = Modifier.widthIn(max = 520.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            quickPrompts.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { (label, promptText) ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = PremiumSurface,
                                            border = BorderStroke(1.dp, BorderColor),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.sendMessage(promptText) }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimaryDark,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE CHAT VIEW with modern scroll-to-bottom hover button
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp), // Minimal horizontal padding for beautiful edge-to-edge bubbles
                        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp)
                    ) {
                        items(messages) { message ->
                            ChatBubble(
                                message = message,
                                currentlySpeakingId = currentlySpeakingMessageId,
                                onSpeakClick = onSpeakMessage,
                                onFeedbackSubmitted = { msgId, rating, comment ->
                                    viewModel.submitFeedback(msgId, rating, comment)
                                },
                                onRegenerateClick = { msgId ->
                                    viewModel.regenerateLastResponse(msgId)
                                }
                            )
                        }
                        if (isGenerating && activeStreamedText != null) {
                            item {
                                ChatBubble(
                                    message = com.example.data.local.ChatMessage(
                                        id = -99,
                                        sessionId = viewModel.currentSessionId.value ?: 0,
                                        role = "model",
                                        text = activeStreamedText ?: "",
                                        isError = false
                                    ),
                                    currentlySpeakingId = null,
                                    onSpeakClick = null
                                )
                            }
                        } else if (isGenerating) {
                            item {
                                SkeletonChatBubble()
                            }
                        }
                    }

                    // Floating action button to scroll back to bottom with a soft pop-up animation
                    AnimatedVisibility(
                        visible = showScrollToBottom,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PremiumSurface)
                                .border(1.2.dp, PrimaryNeonBlue, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll to Bottom",
                                tint = PrimaryNeonBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun EverestVectorArt(modifier: Modifier = Modifier) {
    // Elegant drawing of stylized mountain range + glowing digital sun
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. Digital Sun/Moon (Glowing circle)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    PrimaryNeonBlue.copy(alpha = 0.2f),
                    Color.Transparent
                )
            ),
            radius = width * 0.35f,
            center = androidx.compose.ui.geometry.Offset(width * 0.5f, height * 0.45f)
        )

        drawCircle(
            color = PrimaryNeonBlue.copy(alpha = 0.05f),
            radius = width * 0.2f,
            center = androidx.compose.ui.geometry.Offset(width * 0.5f, height * 0.45f)
        )

        // 2. Stylized Mountains (Paths)
        val pathLeft = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.1f, height * 0.8f)
            lineTo(width * 0.4f, height * 0.35f)
            lineTo(width * 0.65f, height * 0.8f)
            close()
        }
        drawPath(
            path = pathLeft,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF131A35),
                    Color(0xFF0C0E1A)
                )
            )
        )

        val pathRight = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.45f, height * 0.8f)
            lineTo(width * 0.7f, height * 0.45f)
            lineTo(width * 0.9f, height * 0.8f)
            close()
        }
        drawPath(
            path = pathRight,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1A2142),
                    Color(0xFF0F111F)
                )
            )
        )

        // Main Peak (Everest central style)
        val pathCenter = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.25f, height * 0.8f)
            lineTo(width * 0.55f, height * 0.25f)
            lineTo(width * 0.8f, height * 0.8f)
            close()
        }
        drawPath(
            path = pathCenter,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF232B54),
                    Color(0xFF0F122B)
                )
            )
        )

        // Peak Snow cap lines (Stylized neon highlights)
        val peakSnow = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.55f, height * 0.25f)
            lineTo(width * 0.51f, height * 0.35f)
            lineTo(width * 0.55f, height * 0.38f)
            lineTo(width * 0.59f, height * 0.33f)
            close()
        }
        drawPath(
            path = peakSnow,
            color = PrimaryNeonBlue.copy(alpha = 0.6f)
        )

        // Ground/Base horizon line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    BorderColor,
                    Color.Transparent
                )
            ),
            start = androidx.compose.ui.geometry.Offset(width * 0.05f, height * 0.8f),
            end = androidx.compose.ui.geometry.Offset(width * 0.95f, height * 0.8f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun SathiLogo(
    modifier: Modifier = Modifier,
    pulse: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val scale by if (pulse) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Soft background glow
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryNeonBlue.copy(alpha = 0.15f),
                                SecondaryNeonPurple.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f
            val R = minOf(width, height) / 2f * 0.95f

            val strokeWidthPx = R * 0.22f
            val outlineWidthPx = strokeWidthPx + 4.dp.toPx()

            // Draw three interlocking loops
            for (i in 0..2) {
                val angleDegrees = i * 120f
                
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx, cy + R * 0.15f)
                    cubicTo(
                        cx - R * 0.52f, cy + R * 0.15f,
                        cx - R * 0.32f, cy - R * 0.55f,
                        cx, cy - R * 0.65f
                    )
                    cubicTo(
                        cx + R * 0.32f, cy - R * 0.55f,
                        cx + R * 0.52f, cy + R * 0.15f,
                        cx, cy + R * 0.15f
                    )
                }

                // Shading brushes based on which loop it is
                val mainBrush = when (i) {
                    2 -> { // Bottom-Left (highlighted silver-grey metallic loop matching uploaded image)
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFECEFF1), // Bright Silver
                                Color(0xFFCFD8DC), // Medium Silver
                                Color(0xFF78909C), // Slate Silver-Grey
                                Color(0xFF37474F)  // Dark Shadow
                            ),
                            start = androidx.compose.ui.geometry.Offset(cx - R * 0.4f, cy + R * 0.4f),
                            end = androidx.compose.ui.geometry.Offset(cx, cy)
                        )
                    }
                    else -> { // Top & Bottom-Right loops (Premium deep charcoal / purple metallic)
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1F1D2B), // Deep Charcoal
                                Color(0xFF12121A), // Pure Dark Slate
                                SecondaryNeonPurple.copy(alpha = 0.6f), // Rich purple undertone
                                Color(0xFF0F111A)  // Shadows
                            ),
                            start = androidx.compose.ui.geometry.Offset(cx, cy - R * 0.5f),
                            end = androidx.compose.ui.geometry.Offset(cx, cy + R * 0.5f)
                        )
                    }
                }

                val borderBrush = when (i) {
                    2 -> Brush.linearGradient(listOf(Color(0xFFB0BEC5), Color(0xFF37474F)))
                    else -> Brush.linearGradient(listOf(PrimaryNeonBlue.copy(alpha = 0.8f), SecondaryNeonPurple.copy(alpha = 0.4f)))
                }

                rotate(degrees = angleDegrees, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) {
                    // 1. Draw outer black shadow/outline
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(width = outlineWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // 2. Draw outer color border highlight
                    drawPath(
                        path = path,
                        brush = borderBrush,
                        style = Stroke(width = strokeWidthPx + 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // 3. Draw inner fill band
                    drawPath(
                        path = path,
                        brush = mainBrush,
                        style = Stroke(width = strokeWidthPx - 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // 4. Draw high-end metallic highlight reflection streak for bottom-left highlighted loop
                    if (i == 2) {
                        val highlightPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx - R * 0.2f, cy - R * 0.1f)
                            cubicTo(
                                cx - R * 0.35f, cy + R * 0.05f,
                                cx - R * 0.25f, cy + R * 0.35f,
                                cx, cy + R * 0.15f
                            )
                        }
                        drawPath(
                            path = highlightPath,
                            brush = Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)
                            ),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PremiumSurface)
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf("Nepali", "Multilingual", "English")
        options.forEach { option ->
            val isSelected = selectedLanguage == option
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else TextSecondaryDark,
                animationSpec = tween(250),
                label = "text_color"
            )
            val bgBrush = Brush.linearGradient(
                colors = listOf(PrimaryNeonBlue, SecondaryNeonPurple)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLanguageSelected(option) }
                    .drawBehind {
                        if (isSelected) {
                            drawRoundRect(
                                brush = bgBrush,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                            )
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (option) {
                        "Nepali" -> "नेपाली"
                        "Multilingual" -> "बहुभाषिक (Auto)"
                        else -> "English"
                    },
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ModelSettingsDialog(
    availableModels: List<ChatViewModel.ModelOption>,
    selectedModelId: String,
    currentTemperature: Float,
    onModelSelected: (String) -> Unit,
    onTemperatureChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumSurfaceHeader,
        titleContentColor = TextPrimaryDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = PrimaryNeonBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "मोडल तथा इन्जिन सेटिङ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "मोडल छान्नुहोस् (Select Model):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                availableModels.forEach { model ->
                    val isSelected = model.id == selectedModelId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryNeonBlue.copy(alpha = 0.12f) else PremiumSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryNeonBlue else BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onModelSelected(model.id) }
                            .testTag("select_model_${model.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onModelSelected(model.id) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryNeonBlue,
                                    unselectedColor = TextSecondaryDark
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = model.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PrimaryNeonBlue.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Text(
                                            text = model.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNeonBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = model.description,
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "रचनात्मकता / Creativity (Temperature):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val tempOptions = listOf(
                    Triple(0.2f, "सटीक (Precise)", "तथ्यपरक र छोटो"),
                    Triple(0.6f, "सन्तुलित (Balanced)", "सिफारिस गरिएको"),
                    Triple(0.9f, "रचनात्मक (Creative)", "कल्पनाशील र विस्तृत")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tempOptions.forEach { (tempVal, label, subtitle) ->
                        val isTempSelected = kotlin.math.abs(currentTemperature - tempVal) < 0.1f
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isTempSelected) SecondaryNeonPurple.copy(alpha = 0.2f) else PremiumSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isTempSelected) SecondaryNeonPurple else BorderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTemperatureChanged(tempVal) }
                                .testTag("temp_option_${tempVal}")
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTempSelected) Color.White else TextPrimaryDark,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = subtitle,
                                    fontSize = 9.sp,
                                    color = TextSecondaryDark,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("सम्पन्न (Done)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun FeedbackInsightsDialog(
    feedbackMessages: List<com.example.data.local.ChatMessage>,
    onExportData: () -> Unit,
    onClearAllFeedback: () -> Unit,
    onDismiss: () -> Unit
) {
    val totalCount = feedbackMessages.size
    val positiveCount = feedbackMessages.count { (it.feedbackRating ?: 0) > 0 }
    val negativeCount = feedbackMessages.count { (it.feedbackRating ?: 0) < 0 }
    val satisfactionRate = if (totalCount > 0) (positiveCount * 100) / totalCount else 100

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumSurfaceHeader,
        titleContentColor = TextPrimaryDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = PrimaryNeonBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "फिडब्याक तथा गुणस्तर विश्लेषण",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // High-level statistics banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PremiumSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$satisfactionRate%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "सन्तुष्टि (Positive)",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(BorderColor)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$positiveCount 👍",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "सकारात्मक",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(BorderColor)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$negativeCount 👎",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171)
                        )
                        Text(
                            text = "सुधार आवश्यक",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "प्रयोगकर्ताका हालका फिडब्याकहरू (${totalCount}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (feedbackMessages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "अहिले सम्म कुनै फिडब्याक प्राप्त भएको छैन।\nच्याट प्रतिक्रियाहरूमा 👍/👎 दिएर मूल्याङ्कन गर्नुहोस्।",
                            fontSize = 12.sp,
                            color = TextSecondaryDark,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    feedbackMessages.take(15).forEach { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PremiumSurface,
                            border = BorderStroke(0.5.dp, BorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val isPositive = (msg.feedbackRating ?: 0) > 0
                                    Text(
                                        text = if (isPositive) "👍 सकारात्मक (Positive)" else "👎 सुधार आवश्यक (Negative)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPositive) Color(0xFF4ADE80) else Color(0xFFF87171)
                                    )
                                    val timeStr = java.text.SimpleDateFormat("HH:mm, dd MMM", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                                    Text(
                                        text = timeStr,
                                        fontSize = 9.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                                if (!msg.feedbackComment.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "टिप्पणी: ${msg.feedbackComment}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimaryDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text.take(80) + if (msg.text.length > 80) "..." else "",
                                    fontSize = 10.sp,
                                    color = TextSecondaryDark,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Export & Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportData,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("export_feedback_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("डेटा शेयर", fontSize = 11.sp)
                    }

                    if (feedbackMessages.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClearAllFeedback,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                            modifier = Modifier.testTag("clear_feedback_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("मेटाउनुहोस्", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("बन्द गर्नुहोस् (Close)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}


