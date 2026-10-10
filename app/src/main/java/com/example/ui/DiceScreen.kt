package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen(
    viewModel: DiceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Infinite rotation for the roll icon when rolling
    val infiniteTransition = rememberInfiniteTransition(label = "rollingSpin")
    val rollAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rollAngle"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "ShakeDice Icon",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ShakeDice",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            color = if (uiState.isShakeEnabled && uiState.isShakeSensorActive) {
                                                Color(0xFF10B981) // Green
                                            } else {
                                                Color(0xFF94A3B8) // Gray
                                            },
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (uiState.isShakeEnabled && uiState.isShakeSensorActive) {
                                        "Shake Ready (${uiState.shakeSensitivity.label})"
                                    } else {
                                        "Tap Only"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowHistorySheet(true) },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View Roll History"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setShowSettingsSheet(true) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Sensitivity"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Summary & Live Shake Gauge
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RollSummaryBanner(
                    total = uiState.lastRollTotal,
                    breakdown = uiState.lastRollBreakdown,
                    diceType = uiState.diceType
                )

                ShakeForceMeter(
                    currentForce = uiState.shakeForce,
                    threshold = uiState.shakeSensitivity.threshold,
                    isEnabled = uiState.isShakeEnabled && uiState.isShakeSensorActive
                )
            }

            // Middle Section: Interactive Dice Tray (fills available space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                DiceTray(
                    diceList = uiState.diceList,
                    theme = uiState.diceTheme,
                    isRolling = uiState.isRolling,
                    onRollTriggered = { viewModel.rollDice(triggeredByShake = false) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Section: Controls & Selectors
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hint text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Smartphone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isShakeEnabled) {
                            "Shake device or tap tray to roll!"
                        } else {
                            "Tap tray or roll button below"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dice count selector (1..6)
                DiceCountSelector(
                    currentCount = uiState.diceCount,
                    onSelectCount = { viewModel.setDiceCount(it) }
                )

                // RPG Dice Type selector
                DiceTypeSelector(
                    selectedType = uiState.diceType,
                    onSelectType = { viewModel.setDiceType(it) }
                )

                // Big Primary Roll Button
                Button(
                    onClick = { viewModel.rollDice(triggeredByShake = false) },
                    enabled = !uiState.isRolling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("roll_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Roll",
                            modifier = Modifier
                                .size(24.dp)
                                .then(
                                    if (uiState.isRolling) Modifier.rotate(rollAngle) else Modifier
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uiState.isRolling) "Rolling..." else "ROLL ${uiState.diceCount} ${uiState.diceType.label}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // Bottom Sheets
    if (uiState.showHistorySheet) {
        RollHistoryBottomSheet(
            history = uiState.history,
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { viewModel.setShowHistorySheet(false) }
        )
    }

    if (uiState.showSettingsSheet) {
        SettingsBottomSheet(
            currentSensitivity = uiState.shakeSensitivity,
            onSensitivityChanged = { viewModel.setSensitivity(it) },
            isShakeEnabled = uiState.isShakeEnabled,
            onToggleShake = { viewModel.toggleShake() },
            isHapticsEnabled = uiState.isHapticsEnabled,
            onToggleHaptics = { viewModel.toggleHaptics() },
            isSoundEnabled = uiState.isSoundEnabled,
            onToggleSound = { viewModel.toggleSound() },
            currentTheme = uiState.diceTheme,
            onThemeChanged = { viewModel.setDiceTheme(it) },
            onDismiss = { viewModel.setShowSettingsSheet(false) }
        )
    }
}
