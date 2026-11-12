package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.NavySurfaceHighlight
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MockTestTopBar(
    title: String,
    language: AppLanguage,
    onToggleLanguage: (AppLanguage) -> Unit,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    backContentDescription: String = "Back",
    showAdmin: Boolean = false,
    onOpenAdmin: () -> Unit = {},
    adminContentDescription: String = "Admin Panel",
    showSignOut: Boolean = false,
    onSignOut: () -> Unit = {},
    signOutContentDescription: String = "Sign Out"
) {
    Surface(
        color = NavySurface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (showBack) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("top_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backContentDescription,
                            tint = TextPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (showAdmin) {
                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("admin_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AdminPanelSettings,
                            contentDescription = adminContentDescription,
                            tint = SkyAccent
                        )
                    }
                }
                if (showSignOut) {
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = signOutContentDescription,
                            tint = TextSecondary
                        )
                    }
                }
                LanguageTogglePill(
                    language = language,
                    onSelect = onToggleLanguage
                )
            }
        }
    }
}

@Composable
fun LanguageTogglePill(
    language: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = NavySurfaceElevated,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("language_toggle_pill")
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageOptionChip(
                text = "বাংলা",
                selected = language == AppLanguage.BN,
                onClick = { onSelect(AppLanguage.BN) },
                modifier = Modifier.testTag("lang_bn_button")
            )
            LanguageOptionChip(
                text = "EN",
                selected = language == AppLanguage.EN,
                onClick = { onSelect(AppLanguage.EN) },
                modifier = Modifier.testTag("lang_en_button")
            )
        }
    }
}

@Composable
private fun LanguageOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) {
        Brush.horizontalGradient(listOf(SapphirePrimary, SkyAccent))
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextSecondary
        )
    }
}
