package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessage
import com.example.ui.theme.*

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    currentlySpeakingId: Int? = null,
    onSpeakClick: ((ChatMessage) -> Unit)? = null,
    onFeedbackSubmitted: ((Int, Int?, String?) -> Unit)? = null,
    onRegenerateClick: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val isUser = message.role == "user"

    var isCopied by remember { mutableStateOf(false) }
    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(1800)
            isCopied = false
        }
    }

    // High-end smooth popup animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // STATE-A / STATE-B Hooke's Law Spring Physics Parameters
    // Sathi (Incoming): Organic Overshoot, soft inertia. Mass=1.2, Stiffness=180, Damping=0.72
    // User (Outgoing): Snappy, rapid target snap. Mass=0.8, Stiffness=400, Damping=0.85
    val targetStiffness = if (isUser) 400f else 180f
    val targetDamping = if (isUser) 0.85f else 0.72f

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.90f,
        animationSpec = spring(dampingRatio = targetDamping, stiffness = targetStiffness),
        label = "bubble_scale"
    )
    val opacity by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = EaseOutCubic),
        label = "bubble_alpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 16.dp,
        animationSpec = spring(dampingRatio = targetDamping + 0.05f, stiffness = targetStiffness - 40f),
        label = "bubble_offset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 0.dp) // Edge-to-edge horizontal bounds
            .offset(y = offsetY)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = opacity)
            .testTag(if (isUser) "user_message_bubble" else "model_message_bubble"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User message bubble with luxury dark violet-plum and subtle border
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                    .background(ChatBubbleUser)
                    .border(1.dp, ChatBubbleBorderUser, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.End)
            ) {
                ParsedMessageText(text = message.text, textColor = TextPrimaryDark)
            }
        } else {
            // Premium Sathi AI conversational row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // AI Avatar with gorgeous linear neon gradient
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryNeonBlue, SecondaryNeonPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Name label
                    Text(
                        text = "Sathi AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Sathi's premium obsidian chat bubble container with thin high-definition boundary borders
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                            .background(ChatBubbleModel)
                            .border(1.dp, ChatBubbleBorderModel, RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        ParsedMessageText(text = message.text, textColor = TextPrimaryDark)
                    }

                    // Action buttons
                    if (!message.isError) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onSpeakClick != null) {
                                val isCurrentlySpeaking = currentlySpeakingId == message.id
                                IconButton(
                                    onClick = { onSpeakClick(message) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentlySpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isCurrentlySpeaking) "Stop speaking" else "Speak text",
                                        tint = if (isCurrentlySpeaking) BrandAccent else TextSecondaryDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(
                                onClick = {
                                    copyToClipboard(context, message.text)
                                    isCopied = true
                                },
                                modifier = Modifier.size(32.dp).testTag("copy_msg_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = if (isCopied) Color(0xFF4ADE80) else TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { shareText(context, message.text) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share text",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (onRegenerateClick != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onRegenerateClick(message.id) },
                                    modifier = Modifier.size(32.dp).testTag("regenerate_msg_${message.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "पुनः जवाफ माग्नुहोस् (Regenerate)",
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Thumbs Up feedback button
                            val isThumbsUp = message.feedbackRating == 1
                            IconButton(
                                onClick = {
                                    val newRating = if (isThumbsUp) null else 1
                                    onFeedbackSubmitted?.invoke(message.id, newRating, message.feedbackComment)
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("thumbs_up_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (isThumbsUp) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Thumbs Up",
                                    tint = if (isThumbsUp) Color(0xFF4ADE80) else TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Thumbs Down feedback button
                            val isThumbsDown = message.feedbackRating == -1
                            IconButton(
                                onClick = {
                                    val newRating = if (isThumbsDown) null else -1
                                    onFeedbackSubmitted?.invoke(message.id, newRating, message.feedbackComment)
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("thumbs_down_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (isThumbsDown) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Thumbs Down",
                                    tint = if (isThumbsDown) Color(0xFFF87171) else TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Compact inline comment section when message is rated
                        if (message.feedbackRating != null) {
                            var commentText by remember(message.id, message.feedbackComment) {
                                mutableStateOf(message.feedbackComment ?: "")
                            }
                            var isEditingComment by remember(message.id, message.feedbackComment) {
                                mutableStateOf(message.feedbackComment.isNullOrBlank())
                            }

                            val quickTags = if ((message.feedbackRating ?: 0) > 0) {
                                listOf("✨ सटीक", "⚡ द्रुत", "📝 उपयोगी", "🇳🇵 राम्रो नेपाली")
                            } else {
                                listOf("❌ गलत", "⏳ धेरै लामो", "🌐 भाषा गडबड", "🔄 अपुरो")
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 4.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickTags.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = PremiumSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                        modifier = Modifier.clickable {
                                            val currentC = commentText.trim()
                                            val newC = if (currentC.isEmpty()) tag else "$currentC, $tag"
                                            commentText = newC
                                            onFeedbackSubmitted?.invoke(message.id, message.feedbackRating, newC)
                                        }
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 10.sp,
                                            color = TextSecondaryDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            if (isEditingComment) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp)
                                        .background(PremiumSurface, RoundedCornerShape(12.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        placeholder = {
                                            Text(
                                                text = "टिप्पणी लेख्नुहोस् (Optional comment)...",
                                                fontSize = 12.sp,
                                                color = TextSecondaryDark
                                            )
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedTextColor = TextPrimaryDark,
                                            unfocusedTextColor = TextPrimaryDark,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("feedback_comment_input_${message.id}"),
                                        maxLines = 2
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    TextButton(
                                        onClick = {
                                            onFeedbackSubmitted?.invoke(
                                                message.id,
                                                message.feedbackRating,
                                                commentText.trim().ifBlank { null }
                                            )
                                            isEditingComment = false
                                        },
                                        modifier = Modifier.testTag("submit_comment_btn_${message.id}")
                                    ) {
                                        Text(
                                            text = "Save",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNeonBlue
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, bottom = 4.dp)
                                        .background(PremiumSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .border(0.5.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Feedback,
                                            contentDescription = "Comment",
                                            tint = PrimaryNeonBlue,
                                            modifier = Modifier.size(13.dp).padding(top = 1.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "तपाईंको टिप्पणी (Your comment):",
                                                fontSize = 10.sp,
                                                color = TextSecondaryDark,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = message.feedbackComment ?: "",
                                                fontSize = 12.sp,
                                                color = TextPrimaryDark
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { isEditingComment = true },
                                            modifier = Modifier.size(28.dp).testTag("edit_comment_${message.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit comment",
                                                tint = TextSecondaryDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                onFeedbackSubmitted?.invoke(message.id, message.feedbackRating, null)
                                            },
                                            modifier = Modifier.size(28.dp).testTag("delete_comment_${message.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete comment",
                                                tint = TextSecondaryDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Flawless, High-Performance Inline Markdown-to-AnnotatedString Parser
fun parseMarkdownToAnnotatedString(
    text: String,
    boldColor: Color = Color(0xFFFACC15), // Brand Accent / Golden Yellow
    codeColor: Color = Color(0xFFA5B4FC)  // Indigo/light purple
): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold: **text**
                text.startsWith("**", i) -> {
                    val endIdx = text.indexOf("**", i + 2)
                    if (endIdx != -1) {
                        withStyle(
                            androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)
                        ) {
                            append(text.substring(i + 2, endIdx))
                        }
                        i = endIdx + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }
                // Italic: *text*
                text.startsWith("*", i) && !text.startsWith("**", i) -> {
                    val endIdx = text.indexOf("*", i + 1)
                    if (endIdx != -1) {
                        withStyle(
                            androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic)
                        ) {
                            append(text.substring(i + 1, endIdx))
                        }
                        i = endIdx + 1
                    } else {
                        append("*")
                        i += 1
                    }
                }
                // Inline Code: `code`
                text.startsWith("`", i) -> {
                    val endIdx = text.indexOf("`", i + 1)
                    if (endIdx != -1) {
                        withStyle(
                            androidx.compose.ui.text.SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = codeColor,
                                background = Color(0x1AFFFFFF)
                            )
                        ) {
                            append(text.substring(i + 1, endIdx))
                        }
                        i = endIdx + 1
                    } else {
                        append("`")
                        i += 1
                    }
                }
                // Plain character
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

@Composable
fun ParsedMessageText(text: String, textColor: Color) {
    val parts = text.split("```")
    Column {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block segment
                val codeLines = part.trim().lines()
                val language = if (codeLines.isNotEmpty() && !codeLines.first().contains(" ")) {
                    codeLines.first()
                } else null

                val actualCode = if (language != null && codeLines.size > 1) {
                    codeLines.drop(1).joinToString("\n")
                } else {
                    part.trim()
                }

                CodeBlock(code = actualCode, language = language)
            } else {
                // Standard text segment with native markdown line processing
                val lines = part.lines()
                lines.forEach { line ->
                    val trimmedLine = line.trim()
                    when {
                        // Headers
                        trimmedLine.startsWith("# ") -> {
                            Text(
                                text = parseMarkdownToAnnotatedString(trimmedLine.removePrefix("# ")),
                                color = BrandAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        trimmedLine.startsWith("## ") -> {
                            Text(
                                text = parseMarkdownToAnnotatedString(trimmedLine.removePrefix("## ")),
                                color = BrandAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                            )
                        }
                        trimmedLine.startsWith("### ") -> {
                            Text(
                                text = parseMarkdownToAnnotatedString(trimmedLine.removePrefix("### ")),
                                color = TextPrimaryDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }
                        // Bullet Lists
                        trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ") -> {
                            val content = if (trimmedLine.startsWith("* ")) {
                                trimmedLine.removePrefix("* ")
                            } else {
                                trimmedLine.removePrefix("- ")
                            }
                            Row(
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    color = BrandAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = parseMarkdownToAnnotatedString(content),
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        // Plain Text Lines
                        else -> {
                            if (trimmedLine.isNotEmpty()) {
                                Text(
                                    text = parseMarkdownToAnnotatedString(trimmedLine),
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
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
fun CodeBlock(code: String, language: String?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0C0C0E))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
    ) {
        // Code Block Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141418))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language?.uppercase() ?: "CODE",
                color = BrandAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = { copyToClipboard(context, code) },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        // Code Scrollable Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFA5B4FC),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// God-Tier Overlapping Phase Wave Typing Indicator with precise Cubic-Bezier easing
@Composable
fun SkeletonChatBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_wave")
    val easingCurve = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

    // Three bouncing dots offset dynamically by exactly 150ms to form a beautiful, fluid wave
    val dot1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 0, easing = easingCurve),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 150, easing = easingCurve),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 300, easing = easingCurve),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Sathi Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryNeonBlue, SecondaryNeonPurple)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "S",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Sathi AI",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Dynamic morphing bubble container for the active thinking dots
            Box(
                modifier = Modifier
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                    .background(ChatBubbleModel)
                    .border(1.dp, ChatBubbleBorderModel, RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dot(offsetY = dot1Y)
                    Dot(offsetY = dot2Y)
                    Dot(offsetY = dot3Y)
                }
            }
        }
    }
}

@Composable
fun Dot(offsetY: Float) {
    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(BrandAccent)
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val intent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
}
