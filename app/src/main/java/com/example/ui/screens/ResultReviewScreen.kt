package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.SubmittedTestResult
import com.example.model.TestReviewItem
import com.example.ui.components.MockTestTopBar
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.BengaliFontFamily
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WrongRed
import java.util.Locale

private enum class ReviewFilter { ALL, WRONG_ONLY }

@Composable
fun ResultReviewScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    result: SubmittedTestResult,
    onBackToHome: () -> Unit
) {
    val strings = stringsFor(language)
    var filter by remember { mutableStateOf(ReviewFilter.ALL) }

    BackHandler {
        onBackToHome()
    }

    val displayedItems = remember(result.items, filter) {
        when (filter) {
            ReviewFilter.ALL -> result.items
            ReviewFilter.WRONG_ONLY -> result.items.filter {
                it.selectedOption.isNotBlank() && it.selectedOption != it.correctOption
            }
        }
    }

    val record = result.record
    val scoreText = String.format(Locale.US, "%.2f", record.totalScore)
    val maxText = String.format(Locale.US, "%.0f", record.maxPossibleScore)
    val accuracyText = String.format(Locale.US, "%.1f%%", record.accuracyPercent)
    val minsTaken = record.timeTakenSeconds / 60
    val secsTaken = record.timeTakenSeconds % 60
    val timeTakenFormatted = String.format(Locale.US, "%02d:%02d", minsTaken, secsTaken)

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            MockTestTopBar(
                title = strings.resultTitle,
                language = language,
                onToggleLanguage = onToggleLanguage,
                showBack = true,
                onBack = onBackToHome,
                backContentDescription = strings.backToHome
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(NavyBackground, NavySurface, NavyBackground)))
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Score Hero Card
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = NavySurfaceElevated,
                        border = BorderStroke(1.5.dp, SapphirePrimary.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) record.titleBn else record.titleEn,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = strings.totalScoreLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "$scoreText / $maxText",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = SkyAccent,
                                        modifier = Modifier.testTag("total_score_value")
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = strings.accuracyLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = accuracyText,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CorrectGreen
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPill(strings.correctLabel, record.correctCount.toString(), CorrectGreen, Modifier.weight(1f))
                                StatPill(strings.wrongLabel, record.wrongCount.toString(), WrongRed, Modifier.weight(1f))
                                StatPill(strings.unansweredLabel, record.unansweredCount.toString(), NeutralSlate, Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPill(strings.attemptedLabel, "${record.attemptedCount}/${record.totalQuestions}", SapphirePrimary, Modifier.weight(1f))
                                StatPill(strings.timeTakenLabel, timeTakenFormatted, SkyAccent, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Instant Filters: All Questions | Wrong Only
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = strings.answerReviewTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = filter == ReviewFilter.ALL,
                                onClick = { filter = ReviewFilter.ALL },
                                label = { Text("${strings.filterAll} (${result.items.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SapphirePrimary,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = NavySurfaceElevated,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("filter_all_questions")
                            )
                            FilterChip(
                                selected = filter == ReviewFilter.WRONG_ONLY,
                                onClick = { filter = ReviewFilter.WRONG_ONLY },
                                label = { Text("${strings.filterWrongOnly} (${record.wrongCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WrongRed,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = NavySurfaceElevated,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("filter_wrong_only")
                            )
                        }
                    }
                }

                if (displayedItems.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CorrectGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.noWrongAnswers,
                                style = MaterialTheme.typography.titleMedium,
                                color = CorrectGreen,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(displayedItems, key = { it.questionIndex }) { item ->
                        ReviewQuestionCard(item = item, language = language)
                    }
                }

                item {
                    Button(
                        onClick = onBackToHome,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("back_to_home_button")
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.backToHome, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ReviewQuestionCard(
    item: TestReviewItem,
    language: AppLanguage
) {
    val strings = stringsFor(language)
    val isUnanswered = item.selectedOption.isBlank()
    val isCorrect = !isUnanswered && item.selectedOption == item.correctOption

    val statusColor = when {
        isCorrect -> CorrectGreen
        isUnanswered -> NeutralSlate
        else -> WrongRed
    }
    val statusIcon = when {
        isCorrect -> Icons.Filled.CheckCircle
        isUnanswered -> Icons.Filled.RemoveCircleOutline
        else -> Icons.Filled.Cancel
    }
    val statusText = when {
        isCorrect -> strings.correctLabel
        isUnanswered -> strings.unansweredLabel
        else -> strings.wrongLabel
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NavySurfaceElevated,
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review_item_${item.questionIndex}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, contentDescription = statusText, tint = statusColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${item.questionIndex + 1} • $statusText",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NavyBackground
                ) {
                    Text(
                        text = if (item.isAiGenerated) strings.aiGeneratedTag else strings.verifiedPastQuestionTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isAiGenerated) SkyAccent else CorrectGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = item.questionBn,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = BengaliFontFamily,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            val options = listOf(
                "A" to item.optionA,
                "B" to item.optionB,
                "C" to item.optionC,
                "D" to item.optionD
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { (letter, optText) ->
                    val isCorrectOpt = letter == item.correctOption
                    val isUserWrongOpt = !isCorrect && letter == item.selectedOption
                    val rowBg = when {
                        isCorrectOpt -> CorrectGreen.copy(alpha = 0.16f)
                        isUserWrongOpt -> WrongRed.copy(alpha = 0.16f)
                        else -> NavyBackground
                    }
                    val rowBorder = when {
                        isCorrectOpt -> CorrectGreen
                        isUserWrongOpt -> WrongRed
                        else -> NavyBorder
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = rowBg,
                        border = BorderStroke(1.dp, rowBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$letter. $optText",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = BengaliFontFamily,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${strings.yourAnswer}: ${if (isUnanswered) strings.notAnswered else item.selectedOption}",
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
                Text(
                    text = "${strings.correctAnswer}: ${item.correctOption}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CorrectGreen
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NavyBackground,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = strings.explanationLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = SkyAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.explanationBn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = BengaliFontFamily,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
