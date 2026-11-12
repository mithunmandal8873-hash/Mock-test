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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AdmissionUnit
import com.example.model.AppLanguage
import com.example.model.QuestionDifficulty
import com.example.model.SubjectSpec
import com.example.model.UniversityCategory
import com.example.model.UniversityConfig
import com.example.model.VerifiedQuestion
import com.example.ui.AdminActionState
import com.example.ui.components.MockTestTopBar
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BengaliFontFamily
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

@Composable
fun AdminPanelScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    isAdmin: Boolean,
    universities: List<UniversityConfig>,
    verifiedQuestions: List<VerifiedQuestion>,
    adminActionState: AdminActionState,
    onSeedDefaults: () -> Unit,
    onSaveUniversity: (UniversityConfig) -> Unit,
    onDeleteUniversity: (String) -> Unit,
    onSaveQuestion: (VerifiedQuestion) -> Unit,
    onDeleteQuestion: (String) -> Unit,
    onBack: () -> Unit
) {
    val strings = stringsFor(language)
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingUniversity by remember { mutableStateOf<UniversityConfig?>(null) }
    var editingQuestion by remember { mutableStateOf<VerifiedQuestion?>(null) }

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            MockTestTopBar(
                title = strings.adminPanel,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .navigationBarsPadding()
            ) {
                if (!isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AmberWarning.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = AmberWarning)
                            Text(
                                text = strings.adminRestrictedNotice,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = NavySurfaceElevated,
                        contentColor = SkyAccent
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(strings.adminUniversitiesTab) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(strings.adminVerifiedQuestionsTab) }
                        )
                    }

                    when (adminActionState) {
                        is AdminActionState.Success -> {
                            Surface(color = CorrectGreen.copy(alpha = 0.18f), modifier = Modifier.fillMaxWidth()) {
                                Text(adminActionState.message, color = CorrectGreen, modifier = Modifier.padding(10.dp))
                            }
                        }
                        is AdminActionState.Error -> {
                            Surface(color = WrongRed.copy(alpha = 0.18f), modifier = Modifier.fillMaxWidth()) {
                                Text(adminActionState.message, color = WrongRed, modifier = Modifier.padding(10.dp))
                            }
                        }
                        else -> Unit
                    }

                    if (selectedTab == 0) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            editingUniversity = UniversityConfig(
                                                id = "",
                                                nameEn = "",
                                                nameBn = "",
                                                abbreviation = "",
                                                category = UniversityCategory.PUBLIC.key,
                                                sessionYear = "2024-25",
                                                isPatternVerified = false,
                                                units = listOf(
                                                    AdmissionUnit(
                                                        id = "unit_a",
                                                        nameEn = "A Unit",
                                                        nameBn = "এ ইউনিট",
                                                        patternVerified = false,
                                                        totalQuestions = 60,
                                                        durationMinutes = 45,
                                                        positiveMark = 1.0,
                                                        negativeMark = 0.25,
                                                        subjects = listOf(
                                                            SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 15, true),
                                                            SubjectSpec("chemistry", "Chemistry", "রসায়ন", 15, true)
                                                        )
                                                    )
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                                        modifier = Modifier.weight(1f).testTag("admin_add_uni_button")
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.addUniversity)
                                    }
                                    OutlinedButton(
                                        onClick = onSeedDefaults,
                                        modifier = Modifier.weight(1f).testTag("admin_seed_cloud_button")
                                    ) {
                                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.syncDefaultsToCloud, maxLines = 1)
                                    }
                                }
                            }
                            items(universities, key = { it.id }) { uni ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = NavySurfaceElevated,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${uni.abbreviation} • ${uni.nameEn}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                            Text("${uni.nameBn} • ${uni.sessionYear} • ${uni.units.size} units", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                        }
                                        IconButton(onClick = { editingUniversity = uni }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = SkyAccent)
                                        }
                                        IconButton(onClick = { onDeleteUniversity(uni.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = WrongRed)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Button(
                                    onClick = {
                                        editingQuestion = VerifiedQuestion(
                                            universityId = universities.firstOrNull()?.id ?: "du",
                                            unitId = universities.firstOrNull()?.units?.firstOrNull()?.id ?: "du_a",
                                            examYear = "2023-24",
                                            sourceLabel = "Verified Admission Circular",
                                            subject = "Physics",
                                            topic = "ভেক্টর",
                                            difficulty = QuestionDifficulty.ADMISSION_STANDARD.key,
                                            correctOption = "A"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_add_question_button")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.addVerifiedQuestion)
                                }
                            }
                            items(verifiedQuestions, key = { it.id }) { q ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = NavySurfaceElevated,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${q.universityId.uppercase()} (${q.examYear}) • ${q.subject}", style = MaterialTheme.typography.labelLarge, color = SkyAccent)
                                            Text(q.questionBn, style = MaterialTheme.typography.bodyMedium, fontFamily = BengaliFontFamily, color = TextPrimary, maxLines = 2)
                                        }
                                        IconButton(onClick = { editingQuestion = q }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = SkyAccent)
                                        }
                                        IconButton(onClick = { onDeleteQuestion(q.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = WrongRed)
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

    editingUniversity?.let { draft ->
        UniversityEditorDialog(
            initial = draft,
            onDismiss = { editingUniversity = null },
            onSave = { updated ->
                onSaveUniversity(updated)
                editingUniversity = null
            }
        )
    }

    editingQuestion?.let { draft ->
        VerifiedQuestionEditorDialog(
            initial = draft,
            onDismiss = { editingQuestion = null },
            onSave = { updated ->
                onSaveQuestion(updated)
                editingQuestion = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UniversityEditorDialog(
    initial: UniversityConfig,
    onDismiss: () -> Unit,
    onSave: (UniversityConfig) -> Unit
) {
    var abbr by remember { mutableStateOf(initial.abbreviation) }
    var nameEn by remember { mutableStateOf(initial.nameEn) }
    var nameBn by remember { mutableStateOf(initial.nameBn) }
    var category by remember { mutableStateOf(initial.category) }
    var sessionYear by remember { mutableStateOf(initial.sessionYear) }
    var sourceRef by remember { mutableStateOf(initial.sourceReference) }
    var verifDate by remember { mutableStateOf(initial.verificationDate.ifBlank { "2025-02-01" }) }
    var verified by remember { mutableStateOf(initial.isPatternVerified) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = { Text("University Configuration", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = abbr, onValueChange = { abbr = it }, label = { Text("Abbreviation (e.g. DU)") }, singleLine = true)
                OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Name (English)") }, singleLine = true)
                OutlinedTextField(value = nameBn, onValueChange = { nameBn = it }, label = { Text("Name (Bangla)") }, singleLine = true)
                OutlinedTextField(value = sessionYear, onValueChange = { sessionYear = it }, label = { Text("Session Year") }, singleLine = true)
                OutlinedTextField(value = sourceRef, onValueChange = { sourceRef = it }, label = { Text("Source Reference URL/Circular") }, singleLine = true)
                OutlinedTextField(value = verifDate, onValueChange = { verifDate = it }, label = { Text("Verification Date (YYYY-MM-DD)") }, singleLine = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    UniversityCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat.key,
                            onClick = { category = cat.key },
                            label = { Text(cat.key) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SapphirePrimary)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = verified, onCheckedChange = { verified = it })
                    Text("Official Pattern Verified", color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (abbr.isNotBlank() && nameEn.isNotBlank()) {
                        val updatedUnits = initial.units.map { it.copy(patternVerified = verified, sessionYear = sessionYear, sourceReference = sourceRef, verificationDate = verifDate) }
                        onSave(
                            initial.copy(
                                abbreviation = abbr.trim(),
                                nameEn = nameEn.trim(),
                                nameBn = nameBn.trim().ifBlank { nameEn.trim() },
                                category = category,
                                sessionYear = sessionYear.trim().ifBlank { "2024-25" },
                                sourceReference = sourceRef.trim(),
                                verificationDate = verifDate.trim(),
                                isPatternVerified = verified,
                                units = updatedUnits
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun VerifiedQuestionEditorDialog(
    initial: VerifiedQuestion,
    onDismiss: () -> Unit,
    onSave: (VerifiedQuestion) -> Unit
) {
    var universityId by remember { mutableStateOf(initial.universityId) }
    var unitId by remember { mutableStateOf(initial.unitId) }
    var examYear by remember { mutableStateOf(initial.examYear) }
    var sourceLabel by remember { mutableStateOf(initial.sourceLabel) }
    var subject by remember { mutableStateOf(initial.subject) }
    var topic by remember { mutableStateOf(initial.topic) }
    var questionBn by remember { mutableStateOf(initial.questionBn) }
    var optA by remember { mutableStateOf(initial.optionA) }
    var optB by remember { mutableStateOf(initial.optionB) }
    var optC by remember { mutableStateOf(initial.optionC) }
    var optD by remember { mutableStateOf(initial.optionD) }
    var correct by remember { mutableStateOf(initial.correctOption) }
    var explanationBn by remember { mutableStateOf(initial.explanationBn) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = { Text("Verified Past Question (Bangla)", color = TextPrimary) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = universityId, onValueChange = { universityId = it }, label = { Text("University ID (e.g. du, medical_mbbs)") }, singleLine = true) }
                item { OutlinedTextField(value = unitId, onValueChange = { unitId = it }, label = { Text("Unit ID (e.g. du_a, mbbs_official)") }, singleLine = true) }
                item { OutlinedTextField(value = examYear, onValueChange = { examYear = it }, label = { Text("Exam Year (e.g. 2023-24)") }, singleLine = true) }
                item { OutlinedTextField(value = sourceLabel, onValueChange = { sourceLabel = it }, label = { Text("Source Label") }, singleLine = true) }
                item { OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject (e.g. Physics, Biology)") }, singleLine = true) }
                item { OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic (Bangla)") }, singleLine = true) }
                item { OutlinedTextField(value = questionBn, onValueChange = { questionBn = it }, label = { Text("Question (Bangla)") }) }
                item { OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A (Bangla)") }) }
                item { OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B (Bangla)") }) }
                item { OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C (Bangla)") }) }
                item { OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D (Bangla)") }) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("A", "B", "C", "D").forEach { letter ->
                            FilterChip(
                                selected = correct == letter,
                                onClick = { correct = letter },
                                label = { Text("Correct: $letter") }
                            )
                        }
                    }
                }
                item { OutlinedTextField(value = explanationBn, onValueChange = { explanationBn = it }, label = { Text("Explanation (Bangla)") }) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionBn.isNotBlank() && optA.isNotBlank() && optB.isNotBlank() && optC.isNotBlank() && optD.isNotBlank() && explanationBn.isNotBlank()) {
                        onSave(
                            initial.copy(
                                universityId = universityId.trim(),
                                unitId = unitId.trim(),
                                examYear = examYear.trim(),
                                sourceLabel = sourceLabel.trim().ifBlank { "Verified Archive" },
                                subject = subject.trim(),
                                topic = topic.trim().ifBlank { subject.trim() },
                                questionBn = questionBn.trim(),
                                optionA = optA.trim(),
                                optionB = optB.trim(),
                                optionC = optC.trim(),
                                optionD = optD.trim(),
                                correctOption = correct,
                                explanationBn = explanationBn.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
