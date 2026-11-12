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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.UniversityCategory
import com.example.model.UniversityConfig
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

@Composable
fun VarsityCatalogueScreen(
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    universities: List<UniversityConfig>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: UniversityCategory?,
    onSelectCategory: (UniversityCategory?) -> Unit,
    onSelectUniversity: (UniversityConfig) -> Unit,
    onBack: () -> Unit
) {
    val strings = stringsFor(language)

    val filtered = remember(universities, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        universities.filter { uni ->
            val catOk = selectedCategory == null || uni.categoryEnum() == selectedCategory
            val queryOk = q.isEmpty() ||
                uni.nameEn.lowercase().contains(q) ||
                uni.nameBn.lowercase().contains(q) ||
                uni.abbreviation.lowercase().contains(q)
            catOk && queryOk
        }
    }

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            MockTestTopBar(
                title = strings.varsityAdmission,
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
                    .widthIn(max = 720.dp)
                    .navigationBarsPadding()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = strings.searchUniversitiesHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = SkyAccent)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphirePrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavySurfaceElevated,
                        unfocusedContainerColor = NavySurfaceElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("university_search_input")
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text(strings.allCategories) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SapphirePrimary,
                                selectedLabelColor = TextPrimary,
                                containerColor = NavySurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    items(UniversityCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onSelectCategory(category) },
                            label = { Text(strings.categoryLabel(category)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SapphirePrimary,
                                selectedLabelColor = TextPrimary,
                                containerColor = NavySurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { uni ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = NavySurfaceElevated,
                            border = BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectUniversity(uni) }
                                .testTag("university_card_${uni.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SapphirePrimary.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, SapphirePrimary.copy(alpha = 0.5f)),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = uni.abbreviation.take(6),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = SkyAccent
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.BN) uni.nameBn else uni.nameEn,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BN) uni.nameEn else uni.nameBn,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NavyBackground
                                        ) {
                                            Text(
                                                text = strings.categoryLabel(uni.categoryEnum()),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SkyAccent,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        val isVerified = uni.isPatternVerified
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isVerified) Icons.Filled.CheckCircle else Icons.Outlined.Info,
                                                contentDescription = null,
                                                tint = if (isVerified) CorrectGreen else AmberWarning,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isVerified) strings.verifiedPatternBadge else strings.unverifiedPatternBadge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isVerified) CorrectGreen else AmberWarning
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
