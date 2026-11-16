package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class PromptChipItem(
    val title: String,
    val promptText: String
)

val PRESET_PROMPTS = listOf(
    PromptChipItem(
        title = "🇳🇵 Translate (अनुवाद)",
        promptText = "Translate the following English sentence to pure, polite, and natural Nepali: \"Artificial Intelligence is transforming how we learn, work, and connect with people globally.\""
    ),
    PromptChipItem(
        title = "💻 Code Debugger (कोडिङ)",
        promptText = "Create a simple, highly optimized Kotlin function to find if a string is a palindrome. Explain it briefly."
    ),
    PromptChipItem(
        title = "🏔️ Travel Plan (भ्रमण)",
        promptText = "Suggest a direct, optimized 3-day travel itinerary for visiting Pokhara, Nepal. List must-visit places and local activities."
    ),
    PromptChipItem(
        title = "📝 Email Writer (पत्र)",
        promptText = "Write a direct, professional sick leave email to my manager for 2 days. Keep it short and polite."
    ),
    PromptChipItem(
        title = "💡 Quick Facts (तथ्य)",
        promptText = "Give me 5 direct, fascinating, and lesser-known historical facts about Nepal."
    )
)

@Composable
fun PromptChipsRow(
    onChipClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PRESET_PROMPTS.forEachIndexed { idx, item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(PremiumSurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .clickable { onChipClicked(item.promptText) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("prompt_chip_$idx")
            ) {
                Text(
                    text = item.title,
                    color = PrimaryNeonBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
