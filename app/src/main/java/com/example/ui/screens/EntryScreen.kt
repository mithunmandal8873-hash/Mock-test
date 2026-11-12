package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ActiveTestState
import com.example.model.AppLanguage
import com.example.model.TestRecord
import com.example.ui.components.MockTestTopBar
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.AmberWarning
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
import java.util.Locale

@Composable
fun EntryScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    savedSession: ActiveTestState?,
    recentHistory: List<TestRecord>,
    onResumeSession: () -> Unit,
    onDiscardSession: () -> Unit,
    onSelectVarsity: () -> Unit,
    onSelectMedical: () -> Unit,
    onOpenRecord: (TestRecord) -> Unit,
    onOpenAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    val strings = stringsFor(language)

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            MockTestTopBar(
                title = strings.appName,
                language = language,
                onToggleLanguage = onToggleLanguage,
                showAdmin = true,
                onOpenAdmin = onOpenAdmin,
                adminContentDescription = strings.adminPanel,
                showSignOut = true,
                onSignOut = onSignOut,
                signOutContentDescription = strings.signOut
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(NavyBackground, NavySurface, NavyBackground))
                )
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
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SapphirePrimary.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, SkyAccent.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = strings.welcomeBadge,
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                        Text(
                            text = strings.entryHeadline,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = strings.entrySubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                if (savedSession != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = AmberWarning.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.resumeTestBanner,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AmberWarning
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BN) savedSession.titleBn else savedSession.titleEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = onDiscardSession,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = strings.discardSessionAction,
                                            tint = TextSecondary
                                        )
                                    }
                                    Button(
                                        onClick = onResumeSession,
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = NavyBackground),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("resume_test_button")
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.resumeTestAction, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. Varsity Admission Large Card
                item {
                    TrackSelectionCard(
                        title = strings.varsityAdmission,
                        subtitle = strings.varsitySubtitle,
                        bannerRes = R.drawable.img_varsity_banner_1794508401564,
                        accentColor = SapphirePrimary,
                        icon = Icons.Filled.School,
                        onClick = onSelectVarsity,
                        modifier = Modifier.testTag("varsity_admission_card")
                    )
                }

                // 2. Medical Admission Large Card
                item {
                    TrackSelectionCard(
                        title = strings.medicalAdmission,
                        subtitle = strings.medicalSubtitle,
                        bannerRes = R.drawable.img_medical_banner_1794508402095,
                        accentColor = CyanMedical,
                        icon = Icons.Filled.LocalHospital,
                        onClick = onSelectMedical,
                        modifier = Modifier.testTag("medical_admission_card")
                    )
                }

                // Recent History Section
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = SkyAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.recentHistory,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }

                if (recentHistory.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = NavySurfaceElevated.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.noHistoryYet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(recentHistory.take(5), key = { it.id }) { record ->
                        HistoryRecordRow(
                            record = record,
                            language = language,
                            onClick = { onOpenRecord(record) }
                        )
                    }
                }

                // Mandatory Footer
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.createdByFooter,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("created_by_footer")
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackSelectionCard(
    title: String,
    subtitle: String,
    bannerRes: Int,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = NavySurfaceElevated,
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.55f)),
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.height(186.dp)) {
            Image(
                painter = painterResource(id = bannerRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                NavyBackground.copy(alpha = 0.92f),
                                NavyBackground.copy(alpha = 0.72f),
                                NavyBackground.copy(alpha = 0.35f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(26.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = title,
                            tint = TextPrimary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordRow(
    record: TestRecord,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val scoreFormatted = String.format(Locale.US, "%.2f", record.totalScore)
    val maxFormatted = String.format(Locale.US, "%.0f", record.maxPossibleScore)
    val accFormatted = String.format(Locale.US, "%.0f%%", record.accuracyPercent)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavySurfaceElevated,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == AppLanguage.BN) record.titleBn else record.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${record.correctCount}/${record.totalQuestions} • $accFormatted",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CorrectGreen.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "$scoreFormatted / $maxFormatted",
                    style = MaterialTheme.typography.labelLarge,
                    color = CorrectGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
