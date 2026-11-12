package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.AdmissionUnit
import com.example.model.AppLanguage
import com.example.model.QuestionDifficulty
import com.example.model.QuestionSourceType
import com.example.model.TestConfig
import com.example.model.TestMode
import com.example.model.UniversityConfig
import com.example.ui.GenerationUiState
import com.example.ui.components.MockTestTopBar
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WrongRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VarsityConfigScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    university: UniversityConfig,
    unit: AdmissionUnit,
    initialMode: TestMode,
    generationState: GenerationUiState,
    onSwitchToQuestionBank: () -> Unit,
    onStartTest: (TestConfig) -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit
) {
    val strings = stringsFor(language)
    val isModelTest = initialMode == TestMode.VARSITY_MODEL_TEST

    var sourceType by remember { mutableStateOf(QuestionSourceType.AI_PRACTICE) }
    var difficulty by remember { mutableStateOf(QuestionDifficulty.ADMISSION_STANDARD) }
    var durationInput by remember(unit.id, isModelTest) {
        mutableStateOf(if (isModelTest && unit.patternVerified) unit.durationMinutes.toString() else "45")
    }
    var positiveMarkInput by remember { mutableStateOf("1.0") }
    var negativeMarkInput by remember { mutableStateOf("0.25") }
    var unansweredMarkInput by remember { mutableStateOf("0.0") }

    val selectedSubjects = remember(unit.id) {
        mutableStateMapOf<String, Int>().apply {
            unit.subjects.forEach { sub ->
                if (sub.isCompulsory || this.size < 4) {
                    this[sub.nameEn] = sub.defaultCount.coerceAtLeast(5)
                }
            }
            if (isEmpty() && unit.subjects.isNotEmpty()) {
                val first = unit.subjects.first()
                this[first.nameEn] = first.defaultCount.coerceAtLeast(10)
            }
        }
    }
    var customTotalInput by remember(unit.id) {
        mutableStateOf(selectedSubjects.values.sum().coerceAtLeast(20).toString())
    }

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            MockTestTopBar(
                title = "${university.abbreviation} • ${if (isModelTest) strings.modelTestTitle else strings.questionBankTitle}",
                language = language,
                onToggleLanguage = onToggleLanguage,
                showBack = true,
                onBack = onBack,
                backContentDescription = strings.back
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
                    .widthIn(max = 720.dp)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NavySurfaceElevated,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (language == AppLanguage.BN) "${university.nameBn} — ${unit.nameBn}" else "${university.nameEn} — ${unit.nameEn}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${strings.sessionLabel}: ${unit.sessionYear}" +
                                    if (unit.sourceReference.isNotBlank()) " • ${unit.sourceReference}" else "",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyAccent
                            )
                        }
                    }
                }

                // If Model Test and pattern NOT verified -> block and explain honestly
                if (isModelTest && !unit.patternVerified) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = AmberWarning.copy(alpha = 0.14f),
                            border = BorderStroke(1.5.dp, AmberWarning.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = AmberWarning)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.unverifiedPatternTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberWarning
                                    )
                                }
                                Text(
                                    text = strings.unverifiedPatternMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Button(
                                    onClick = onSwitchToQuestionBank,
                                    colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("switch_to_qb_button")
                                ) {
                                    Text(strings.questionBankTitle, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Source Selection (Verified Past Bank vs AI Practice Bank)
                    item {
                        Text(
                            text = if (language == AppLanguage.BN) "প্রশ্নের উৎস" else "Question Source",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = sourceType == QuestionSourceType.AI_PRACTICE,
                                onClick = {
                                    sourceType = QuestionSourceType.AI_PRACTICE
                                    onClearError()
                                },
                                label = { Text(strings.sourceAiPractice) },
                                leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SapphirePrimary,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = NavySurfaceElevated,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = sourceType == QuestionSourceType.VERIFIED_BANK,
                                onClick = {
                                    sourceType = QuestionSourceType.VERIFIED_BANK
                                    onClearError()
                                },
                                label = { Text(strings.sourceVerifiedBank) },
                                leadingIcon = { Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CorrectGreen,
                                    selectedLabelColor = NavyBackground,
                                    containerColor = NavySurfaceElevated,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (sourceType == QuestionSourceType.AI_PRACTICE) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
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

                    if (isModelTest) {
                        // Locked Official Pattern Breakdown
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CorrectGreen.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${strings.verifiedPatternBadge} • ${unit.totalQuestions} MCQs • ${unit.durationMinutes} min",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CorrectGreen
                                    )
                                    Text(
                                        text = "+${unit.positiveMark} / -${unit.negativeMark} • ${unit.notesEn.ifBlank { unit.sourceReference }}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    unit.subjects.filter { it.isCompulsory }.forEach { sub ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) sub.nameBn else sub.nameEn,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${sub.defaultCount} Q",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = SkyAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Question Bank Mode: Customizable Subjects + Per-Subject Count + Total Presets
                        item {
                            Text(
                                text = strings.selectSubjects,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                unit.subjects.forEach { sub ->
                                    val isChecked = selectedSubjects.containsKey(sub.nameEn)
                                    FilterChip(
                                        selected = isChecked,
                                        onClick = {
                                            if (isChecked && selectedSubjects.size > 1) {
                                                selectedSubjects.remove(sub.nameEn)
                                            } else if (!isChecked) {
                                                selectedSubjects[sub.nameEn] = sub.defaultCount.coerceAtLeast(5)
                                            }
                                            customTotalInput = selectedSubjects.values.sum().toString()
                                        },
                                        label = {
                                            Text(if (language == AppLanguage.BN) sub.nameBn else sub.nameEn)
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SapphirePrimary,
                                            selectedLabelColor = TextPrimary,
                                            containerColor = NavySurfaceElevated,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }

                        // Total Question Count Presets (20, 30, 50, 75, 100, 150) & Custom
                        item {
                            Text(
                                text = strings.totalQuestions,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(20, 30, 50, 75, 100, 150).forEach { preset ->
                                    val currentSum = selectedSubjects.values.sum()
                                    FilterChip(
                                        selected = currentSum == preset,
                                        onClick = {
                                            distributeCountAcrossSubjects(selectedSubjects, preset)
                                            customTotalInput = preset.toString()
                                        },
                                        label = { Text(preset.toString()) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SapphirePrimary,
                                            selectedLabelColor = TextPrimary,
                                            containerColor = NavySurfaceElevated,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = customTotalInput,
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }.take(3)
                                        customTotalInput = digits
                                        val num = digits.toIntOrNull()
                                        if (num != null && num in 1..150 && selectedSubjects.isNotEmpty()) {
                                            distributeCountAcrossSubjects(selectedSubjects, num)
                                        }
                                    },
                                    label = { Text(strings.customCountHint) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SapphirePrimary,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.width(160.dp)
                                )
                                Text(
                                    text = "= ${selectedSubjects.values.sum()} Q",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SkyAccent
                                )
                            }
                        }

                        // Per-subject fine-tuning
                        item {
                            Text(
                                text = strings.questionsPerSubject,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedSubjects.entries.toList().forEach { (subName, count) ->
                                    val subSpec = unit.subjects.firstOrNull { it.nameEn == subName }
                                    val label = if (language == AppLanguage.BN && subSpec != null) subSpec.nameBn else subName
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = NavySurfaceElevated,
                                        border = BorderStroke(1.dp, NavyBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        if (count > 1) {
                                                            selectedSubjects[subName] = count - 1
                                                            customTotalInput = selectedSubjects.values.sum().toString()
                                                        }
                                                    },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = TextSecondary)
                                                }
                                                Text(
                                                    text = count.toString(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = SkyAccent,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        if (selectedSubjects.values.sum() < 150) {
                                                            selectedSubjects[subName] = count + 1
                                                            customTotalInput = selectedSubjects.values.sum().toString()
                                                        }
                                                    },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Icon(Icons.Filled.Add, contentDescription = "Increase", tint = SkyAccent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Difficulty Selector
                    item {
                        Text(
                            text = strings.difficultyLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuestionDifficulty.entries.forEach { diff ->
                                FilterChip(
                                    selected = difficulty == diff,
                                    onClick = { difficulty = diff },
                                    label = { Text(strings.difficultyLabel(diff)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SapphirePrimary,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = NavySurfaceElevated,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    // Duration & Marking Scheme
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = durationInput,
                                onValueChange = { durationInput = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text(strings.durationMinutesLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SapphirePrimary,
                                    unfocusedBorderColor = NavyBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (!isModelTest) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.markingSchemeLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = positiveMarkInput,
                                    onValueChange = { positiveMarkInput = it },
                                    label = { Text(strings.correctMarkLabel) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = negativeMarkInput,
                                    onValueChange = { negativeMarkInput = it },
                                    label = { Text(strings.wrongDeductionLabel) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = unansweredMarkInput,
                                    onValueChange = { unansweredMarkInput = it },
                                    label = { Text(strings.unansweredMarkLabel) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Generation Status / Errors
                    item {
                        GenerationFeedbackBlock(
                            language = language,
                            generationState = generationState,
                            onSwitchToAi = {
                                sourceType = QuestionSourceType.AI_PRACTICE
                                onClearError()
                            },
                            onRetry = {
                                if (generationState is GenerationUiState.Error) {
                                    onStartTest(generationState.config)
                                }
                            }
                        )
                    }

                    // Start Test Button
                    item {
                        val isLoading = generationState is GenerationUiState.Loading
                        Button(
                            onClick = {
                                val finalCounts = if (isModelTest) {
                                    val compulsory = unit.subjects.filter { it.isCompulsory }
                                    val targetMap = compulsory.associate { it.nameEn to it.defaultCount }.toMutableMap()
                                    val sumNow = targetMap.values.sum()
                                    if (sumNow < unit.totalQuestions) {
                                        val elective = unit.subjects.firstOrNull { !it.isCompulsory }
                                        if (elective != null) {
                                            targetMap[elective.nameEn] = (unit.totalQuestions - sumNow).coerceAtLeast(0)
                                        }
                                    }
                                    targetMap
                                } else {
                                    selectedSubjects.toMap()
                                }
                                val totalQ = finalCounts.values.sum().coerceAtLeast(1)
                                val durMins = (durationInput.toIntOrNull() ?: 45).coerceIn(1, 300)
                                val pos = if (isModelTest) unit.positiveMark else (positiveMarkInput.toDoubleOrNull() ?: 1.0)
                                val neg = if (isModelTest) unit.negativeMark else (negativeMarkInput.toDoubleOrNull() ?: 0.25)
                                val unans = if (isModelTest) unit.unansweredMark else (unansweredMarkInput.toDoubleOrNull() ?: 0.0)

                                onStartTest(
                                    TestConfig(
                                        mode = initialMode,
                                        titleEn = "${university.abbreviation} ${unit.nameEn}",
                                        titleBn = "${university.nameBn} — ${unit.nameBn}",
                                        universityId = university.id,
                                        unitId = unit.id,
                                        sessionYear = unit.sessionYear,
                                        sourceType = sourceType,
                                        difficulty = difficulty,
                                        subjectCounts = finalCounts,
                                        totalQuestions = totalQ,
                                        durationMinutes = durMins,
                                        positiveMark = pos,
                                        negativeMark = neg,
                                        unansweredMark = unans,
                                        isOfficialPattern = isModelTest && unit.patternVerified,
                                        patternReference = unit.sourceReference
                                    )
                                )
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("start_varsity_test_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(strings.generatingQuestionsTitle)
                            } else {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.startTest, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GenerationFeedbackBlock(
    language: AppLanguage,
    generationState: GenerationUiState,
    onSwitchToAi: () -> Unit,
    onRetry: () -> Unit
) {
    val strings = stringsFor(language)
    when (generationState) {
        GenerationUiState.Idle -> Unit
        GenerationUiState.Loading -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SapphirePrimary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, SkyAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = SkyAccent, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    Column {
                        Text(strings.generatingQuestionsTitle, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(strings.generatingQuestionsSubtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }
        }
        is GenerationUiState.VerifiedBankInsufficient -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AmberWarning.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.verifiedBankEmptyTitle, style = MaterialTheme.typography.titleMedium, color = AmberWarning)
                    Text(
                        text = "${strings.verifiedBankEmptyMessage} (${generationState.availableCount}/${generationState.requestedCount})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    OutlinedButton(onClick = onSwitchToAi, shape = RoundedCornerShape(10.dp)) {
                        Text(strings.switchToAiPractice, color = SkyAccent)
                    }
                }
            }
        }
        is GenerationUiState.Error -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = WrongRed.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, WrongRed.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(generationState.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(10.dp)) {
                        Text(strings.retry, color = TextPrimary)
                    }
                }
            }
        }
    }
}

fun distributeCountAcrossSubjects(map: MutableMap<String, Int>, total: Int) {
    val keys = map.keys.toList()
    if (keys.isEmpty()) return
    val base = total / keys.size
    var remainder = total % keys.size
    keys.forEach { key ->
        val extra = if (remainder > 0) {
            remainder--
            1
        } else 0
        map[key] = (base + extra).coerceAtLeast(0)
    }
}
