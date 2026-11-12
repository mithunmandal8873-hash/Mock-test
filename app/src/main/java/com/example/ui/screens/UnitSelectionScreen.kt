package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AdmissionUnit
import com.example.model.AppLanguage
import com.example.model.TestMode
import com.example.model.UniversityConfig
import com.example.ui.components.MockTestTopBar
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CyanMedical
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavySurfaceHighlight
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UnitSelectionScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    university: UniversityConfig,
    selectedUnitId: String?,
    onSelectUnit: (AdmissionUnit) -> Unit,
    onProceedMode: (AdmissionUnit, TestMode) -> Unit,
    onBack: () -> Unit
) {
    val strings = stringsFor(language)
    val activeUnit = university.units.firstOrNull { it.id == selectedUnitId }
        ?: university.units.firstOrNull()

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            MockTestTopBar(
                title = university.abbreviation,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (language == AppLanguage.BN) university.nameBn else university.nameEn,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "${strings.sessionLabel}: ${university.sessionYear}" +
                                if (university.sourceReference.isNotBlank()) " • ${university.sourceReference}" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyAccent
                        )
                    }
                }

                item {
                    Text(
                        text = strings.selectUnitTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                items(university.units, key = { it.id }) { unit ->
                    val isSelected = activeUnit?.id == unit.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NavySurfaceHighlight else NavySurfaceElevated,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SapphirePrimary else NavyBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectUnit(unit) }
                            .testTag("unit_card_${unit.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) unit.nameBn else unit.nameEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (unit.patternVerified) CorrectGreen.copy(alpha = 0.16f) else AmberWarning.copy(alpha = 0.16f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (unit.patternVerified) Icons.Filled.Verified else Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = if (unit.patternVerified) CorrectGreen else AmberWarning,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (unit.patternVerified) strings.verifiedPatternBadge else strings.unverifiedPatternBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (unit.patternVerified) CorrectGreen else AmberWarning
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = unit.subjects.joinToString(" • ") {
                                    if (language == AppLanguage.BN) it.nameBn else it.nameEn
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (activeUnit != null) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.chooseModeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    // A. Question Bank Mode
                    item {
                        ModeOptionCard(
                            label = "A. ${strings.questionBankTitle}",
                            subtitle = strings.questionBankSubtitle,
                            icon = Icons.Filled.AutoAwesome,
                            accent = SapphirePrimary,
                            onClick = { onProceedMode(activeUnit, TestMode.VARSITY_QUESTION_BANK) },
                            modifier = Modifier.testTag("mode_question_bank_card")
                        )
                    }

                    // B. Model Test Mode
                    item {
                        ModeOptionCard(
                            label = "B. ${strings.modelTestTitle}",
                            subtitle = strings.modelTestSubtitle,
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            accent = CyanMedical,
                            onClick = { onProceedMode(activeUnit, TestMode.VARSITY_MODEL_TEST) },
                            modifier = Modifier.testTag("mode_model_test_card")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NavySurfaceElevated,
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.18f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accent
            )
        }
    }
}
