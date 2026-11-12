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
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.AdmissionCatalogDefaults
import com.example.model.AppLanguage
import com.example.model.QuestionDifficulty
import com.example.model.QuestionSourceType
import com.example.model.TestConfig
import com.example.model.TestMode
import com.example.ui.GenerationUiState
import com.example.ui.components.MockTestTopBar
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CyanMedical
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalConfigScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    generationState: GenerationUiState,
    onStartTest: (TestConfig) -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit
) {
    val strings = stringsFor(language)
    val officialUnit = AdmissionCatalogDefaults.officialMedicalPatternUnit
    val allSubjects = AdmissionCatalogDefaults.medicalSubjects

    var useOfficialPattern by remember { mutableStateOf(true) }
    var usePercentageMode by remember { mutableStateOf(false) }
    var sourceType by remember { mutableStateOf(QuestionSourceType.AI_PRACTICE) }
    var difficulty by remember { mutableStateOf(QuestionDifficulty.ADMISSION_STANDARD) }
    var durationInput by remember { mutableStateOf("60") }
    var positiveMarkInput by remember { mutableStateOf("1.0") }
    var negativeMarkInput by remember { mutableStateOf("0.25") }
    var unansweredMarkInput by remember { mutableStateOf("0.0") }
    var customTotalInput by remember { mutableStateOf("100") }

    val subjectCounts = remember {
        mutableStateMapOf<String, Int>().apply {
            put("Biology", 30)
            put("Chemistry", 25)
            put("Physics", 20)
            put("English", 15)
            put("General Knowledge", 10)
        }
    }

    val subjectPercentages = remember {
        mutableStateMapOf<String, Int>().apply {
            put("Biology", 30)
            put("Chemistry", 25)
            put("Physics", 20)
            put("English", 15)
            put("General Knowledge", 10)
        }
    }

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            MockTestTopBar(
                title = strings.medicalAdmission,
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
                // Pattern mode switcher: Official DGME vs Custom Practice
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = useOfficialPattern,
                            onClick = {
                                useOfficialPattern = true
                                durationInput = officialUnit.durationMinutes.toString()
                            },
                            label = { Text(strings.officialDgmePatternLabel) },
                            leadingIcon = { Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CorrectGreen,
                                selectedLabelColor = NavyBackground,
                                containerColor = NavySurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("medical_official_mode_chip")
                        )
                        FilterChip(
                            selected = !useOfficialPattern,
                            onClick = { useOfficialPattern = false },
                            label = { Text(strings.customPracticeModeLabel) },
                            leadingIcon = { Icon(Icons.Filled.LocalHospital, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanMedical,
                                selectedLabelColor = NavyBackground,
                                containerColor = NavySurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("medical_custom_mode_chip")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (useOfficialPattern) CorrectGreen.copy(alpha = 0.12f) else CyanMedical.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (useOfficialPattern) CorrectGreen.copy(alpha = 0.45f) else CyanMedical.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (useOfficialPattern) strings.officialPatternWarning else strings.aiNoticeBanner,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Source Selection
                item {
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
                }

                if (useOfficialPattern) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = NavySurfaceElevated,
                            border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${officialUnit.sourceReference} (${officialUnit.sessionYear})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CorrectGreen
                                )
                                officialUnit.subjects.forEach { sub ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BN) sub.nameBn else sub.nameEn,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${sub.defaultCount} Q",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = SkyAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 7-Subject Custom Medical Mode
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
                            allSubjects.forEach { sub ->
                                val isChecked = subjectCounts.containsKey(sub.nameEn)
                                FilterChip(
                                    selected = isChecked,
                                    onClick = {
                                        if (isChecked && subjectCounts.size > 1) {
                                            subjectCounts.remove(sub.nameEn)
                                            subjectPercentages.remove(sub.nameEn)
                                        } else if (!isChecked) {
                                            subjectCounts[sub.nameEn] = 10
                                            subjectPercentages[sub.nameEn] = 10
                                        }
                                        if (!usePercentageMode) {
                                            customTotalInput = subjectCounts.values.sum().toString()
                                        }
                                    },
                                    label = { Text(if (language == AppLanguage.BN) sub.nameBn else sub.nameEn) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanMedical,
                                        selectedLabelColor = NavyBackground,
                                        containerColor = NavySurfaceElevated,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    // Distribution mode: Question Count vs Percentage (%)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = !usePercentageMode,
                                onClick = { usePercentageMode = false },
                                label = { Text(strings.distributionModeCount) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SapphirePrimary,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = NavySurfaceElevated,
                                    labelColor = TextSecondary
                                )
                            )
                            FilterChip(
                                selected = usePercentageMode,
                                onClick = { usePercentageMode = true },
                                label = { Text(strings.distributionModePercentage) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SapphirePrimary,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = NavySurfaceElevated,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // Total Question Count Presets & Custom
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
                                val activeTotal = if (usePercentageMode) {
                                    customTotalInput.toIntOrNull() ?: 100
                                } else {
                                    subjectCounts.values.sum()
                                }
                                FilterChip(
                                    selected = activeTotal == preset,
                                    onClick = {
                                        customTotalInput = preset.toString()
                                        if (!usePercentageMode) {
                                            distributeCountAcrossSubjects(subjectCounts, preset)
                                        }
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
                        OutlinedTextField(
                            value = customTotalInput,
                            onValueChange = { raw ->
                                val digits = raw.filter { it.isDigit() }.take(3)
                                customTotalInput = digits
                                val num = digits.toIntOrNull()
                                if (!usePercentageMode && num != null && num in 1..150 && subjectCounts.isNotEmpty()) {
                                    distributeCountAcrossSubjects(subjectCounts, num)
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
                            modifier = Modifier.width(180.dp)
                        )
                    }

                    // Per-subject Count OR Percentage Sliders/Steppers
                    item {
                        val activeMap = if (usePercentageMode) subjectPercentages else subjectCounts
                        val unitLabel = if (usePercentageMode) "%" else "Q"
                        val totalPct = subjectPercentages.values.sum()

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (usePercentageMode) {
                                Text(
                                    text = "Total: $totalPct% (${customTotalInput.toIntOrNull() ?: 100} Q)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (totalPct == 100) CorrectGreen else CyanMedical
                                )
                            }
                            activeMap.keys.toList().forEach { subName ->
                                val spec = allSubjects.firstOrNull { it.nameEn == subName }
                                val label = if (language == AppLanguage.BN && spec != null) spec.nameBn else subName
                                val currentVal = activeMap[subName] ?: 0
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
                                                    val step = if (usePercentageMode) 5 else 1
                                                    if (currentVal > step) {
                                                        activeMap[subName] = currentVal - step
                                                        if (!usePercentageMode) customTotalInput = subjectCounts.values.sum().toString()
                                                    }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = TextSecondary)
                                            }
                                            Text(
                                                text = "$currentVal$unitLabel",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = SkyAccent,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val step = if (usePercentageMode) 5 else 1
                                                    val limit = if (usePercentageMode) 100 else 150
                                                    if (currentVal + step <= limit) {
                                                        activeMap[subName] = currentVal + step
                                                        if (!usePercentageMode) customTotalInput = subjectCounts.values.sum().toString()
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

                    // Configurable Marking Scheme in Custom Mode
                    item {
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

                // Difficulty & Duration
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
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = { durationInput = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text(strings.durationMinutesLabel) },
                        enabled = !useOfficialPattern,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphirePrimary,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.width(200.dp)
                    )
                }

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

                item {
                    val isLoading = generationState is GenerationUiState.Loading
                    Button(
                        onClick = {
                            val finalSubjectCounts: Map<String, Int> = if (useOfficialPattern) {
                                officialUnit.subjects.associate { it.nameEn to it.defaultCount }
                            } else if (usePercentageMode) {
                                val targetTotal = (customTotalInput.toIntOrNull() ?: 100).coerceIn(1, 150)
                                val pctSum = subjectPercentages.values.sum().coerceAtLeast(1)
                                val keys = subjectPercentages.keys.toList()
                                val mapped = linkedMapOf<String, Int>()
                                var running = 0
                                keys.forEachIndexed { idx, key ->
                                    val count = if (idx == keys.lastIndex) {
                                        (targetTotal - running).coerceAtLeast(0)
                                    } else {
                                        val share = ((subjectPercentages[key] ?: 0).toDouble() / pctSum) * targetTotal
                                        share.roundToInt().coerceAtMost(targetTotal - running)
                                    }
                                    mapped[key] = count
                                    running += count
                                }
                                mapped
                            } else {
                                subjectCounts.toMap()
                            }

                            val totalQ = finalSubjectCounts.values.sum().coerceAtLeast(1)
                            val durMins = if (useOfficialPattern) officialUnit.durationMinutes else (durationInput.toIntOrNull() ?: 60).coerceIn(1, 300)
                            val pos = if (useOfficialPattern) officialUnit.positiveMark else (positiveMarkInput.toDoubleOrNull() ?: 1.0)
                            val neg = if (useOfficialPattern) officialUnit.negativeMark else (negativeMarkInput.toDoubleOrNull() ?: 0.25)
                            val unans = if (useOfficialPattern) officialUnit.unansweredMark else (unansweredMarkInput.toDoubleOrNull() ?: 0.0)

                            onStartTest(
                                TestConfig(
                                    mode = TestMode.MEDICAL,
                                    titleEn = if (useOfficialPattern) "Medical MBBS/BDS Official Pattern" else "Medical Custom Practice",
                                    titleBn = if (useOfficialPattern) "মেডিকেল MBBS/BDS অফিসিয়াল প্যাটার্ন" else "মেডিকেল কাস্টম অনুশীলন",
                                    universityId = "medical_mbbs",
                                    unitId = if (useOfficialPattern) officialUnit.id else "medical_custom",
                                    sessionYear = officialUnit.sessionYear,
                                    sourceType = sourceType,
                                    difficulty = difficulty,
                                    subjectCounts = finalSubjectCounts,
                                    totalQuestions = totalQ,
                                    durationMinutes = durMins,
                                    positiveMark = pos,
                                    negativeMark = neg,
                                    unansweredMark = unans,
                                    isOfficialPattern = useOfficialPattern,
                                    patternReference = if (useOfficialPattern) officialUnit.sourceReference else "Custom Practice"
                                )
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanMedical, contentColor = NavyBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_medical_test_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = NavyBackground, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(strings.generatingQuestionsTitle, fontWeight = FontWeight.Bold)
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
