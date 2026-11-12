package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ActiveTestState
import com.example.model.AppLanguage
import com.example.model.MockQuestion
import com.example.model.QuestionSourceType
import com.example.ui.components.MockTestTopBar
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BengaliFontFamily
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavySurfaceHighlight
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WrongRed
import java.util.Locale

@Composable
fun TestRunnerScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    activeTest: ActiveTestState,
    isSubmitting: Boolean,
    autoSubmitNotice: Boolean,
    onSelectAnswer: (Int, String) -> Unit,
    onConfirmSubmit: () -> Unit,
    onEnsureTimerRunning: () -> Unit,
    onExitTest: () -> Unit
) {
    val strings = stringsFor(language)
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activeTest.sessionKey) {
        onEnsureTimerRunning()
    }

    BackHandler {
        showExitDialog = true
    }

    val totalCount = activeTest.questions.size
    val answeredCount = activeTest.selectedAnswers.count { it.value.isNotBlank() }
    val progressFraction = if (totalCount > 0) answeredCount.toFloat() / totalCount.toFloat() else 0f

    val minutes = activeTest.remainingSeconds / 60
    val seconds = activeTest.remainingSeconds % 60
    val timerText = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val timerUrgent = activeTest.remainingSeconds <= 60

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            Column {
                MockTestTopBar(
                    title = if (language == AppLanguage.BN) activeTest.titleBn else activeTest.titleEn,
                    language = language,
                    onToggleLanguage = onToggleLanguage,
                    showBack = true,
                    onBack = { showExitDialog = true },
                    backContentDescription = strings.back
                )
                // Sticky Status Strip (Countdown + Answered Progress)
                Surface(
                    color = NavySurfaceElevated,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = strings.timeRemaining,
                                    tint = if (timerUrgent) WrongRed else SkyAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${strings.timeRemaining}: $timerText",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (timerUrgent) WrongRed else TextPrimary,
                                    modifier = Modifier.testTag("countdown_timer_text")
                                )
                            }
                            Text(
                                text = "${strings.answeredProgress}: $answeredCount / $totalCount",
                                style = MaterialTheme.typography.labelLarge,
                                color = SkyAccent,
                                modifier = Modifier.testTag("answered_count_text")
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = SapphirePrimary,
                            trackColor = NavyBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
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
                    .navigationBarsPadding()
                    .testTag("one_page_test_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeTest.sourceType == QuestionSourceType.AI_PRACTICE) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SapphirePrimary.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, SapphirePrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.aiNoticeBanner,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SkyAccent,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (autoSubmitNotice) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberWarning.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, AmberWarning),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.timeUpAutoSubmitting,
                                style = MaterialTheme.typography.titleMedium,
                                color = AmberWarning,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // ALL QUESTIONS ON ONE CONTINUOUS SCROLLABLE PAGE
                items(activeTest.questions, key = { it.index }) { question ->
                    QuestionCardItem(
                        question = question,
                        selectedOption = activeTest.selectedAnswers[question.index],
                        onSelectOption = { opt -> onSelectAnswer(question.index, opt) }
                    )
                }

                // ONE Submit Test button at the bottom
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { showSubmitDialog = true },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("submit_test_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${strings.submitTest} ($answeredCount/$totalCount)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            containerColor = NavySurfaceElevated,
            title = { Text(strings.submitConfirmTitle, color = TextPrimary) },
            text = {
                Text(
                    text = "${strings.submitConfirmMessage}\n\n${strings.answeredProgress}: $answeredCount / $totalCount",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        onConfirmSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                    modifier = Modifier.testTag("confirm_submit_dialog_button")
                ) {
                    Text(strings.confirmSubmit)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = NavySurfaceElevated,
            title = { Text(strings.back, color = TextPrimary) },
            text = {
                Text(
                    text = if (language == AppLanguage.BN) {
                        "আপনার উত্তরগুলো সংরক্ষিত আছে। হোম স্ক্রিনে ফিরে গেলে পরে আবার এই পরীক্ষা চালিয়ে যেতে পারবেন।"
                    } else {
                        "Your selected answers are saved. Returning home lets you resume this test later."
                    },
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onExitTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary)
                ) {
                    Text(strings.backToHome)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun QuestionCardItem(
    question: MockQuestion,
    selectedOption: String?,
    onSelectOption: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NavySurfaceElevated,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.index}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SapphirePrimary.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "#${question.index + 1} • ${question.subject}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SkyAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = question.topic,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = BengaliFontFamily,
                    color = TextSecondary
                )
            }

            Text(
                text = question.questionBn,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = BengaliFontFamily,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            val options = listOf(
                "A" to question.optionA,
                "B" to question.optionB,
                "C" to question.optionC,
                "D" to question.optionD
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (letter, text) ->
                    val isSelected = selectedOption == letter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NavySurfaceHighlight else NavyBackground,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SapphirePrimary else NavyBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectOption(letter) }
                            .testTag("q_${question.index}_option_$letter")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) SapphirePrimary else NavySurfaceElevated,
                                border = BorderStroke(1.dp, if (isSelected) SkyAccent else NavyBorder),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = letter,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = BengaliFontFamily,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
