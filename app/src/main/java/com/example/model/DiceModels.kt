package com.example.model

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DiceType(val sides: Int, val label: String, val description: String) {
    D4(4, "D4", "4-Sided Tetrahedral Die"),
    D6(6, "D6", "Standard 6-Sided Cubic Die"),
    D8(8, "D8", "8-Sided Octahedral Die"),
    D10(10, "D10", "10-Sided Decahedral Die"),
    D12(12, "D12", "12-Sided Dodecahedral Die"),
    D20(20, "D20", "20-Sided Icosahedral RPG Die");

    fun roll(): Int = (1..sides).random()
}

enum class DiceTheme(
    val title: String,
    val bodyColor: Color,
    val pipColor: Color,
    val borderColor: Color,
    val isDarkDie: Boolean
) {
    CLASSIC(
        title = "Classic Ivory",
        bodyColor = Color(0xFFFBFBFB),
        pipColor = Color(0xFF1E293B),
        borderColor = Color(0xFFD1D5DB),
        isDarkDie = false
    ),
    CRIMSON(
        title = "Ruby Flame",
        bodyColor = Color(0xFFDC2626),
        pipColor = Color(0xFFFFFFFF),
        borderColor = Color(0xFF991B1B),
        isDarkDie = true
    ),
    OBSIDIAN(
        title = "Midnight Gold",
        bodyColor = Color(0xFF18181B),
        pipColor = Color(0xFFFBBF24),
        borderColor = Color(0xFF3F3F46),
        isDarkDie = true
    ),
    EMERALD(
        title = "Emerald Jade",
        bodyColor = Color(0xFF059669),
        pipColor = Color(0xFFFFFFFF),
        borderColor = Color(0xFF047857),
        isDarkDie = true
    ),
    AMETHYST(
        title = "Royal Amethyst",
        bodyColor = Color(0xFF7C3AED),
        pipColor = Color(0xFFFDE047),
        borderColor = Color(0xFF5B21B6),
        isDarkDie = true
    ),
    CYBER(
        title = "Cyber Neon",
        bodyColor = Color(0xFF0F172A),
        pipColor = Color(0xFF22D3EE),
        borderColor = Color(0xFF06B6D4),
        isDarkDie = true
    )
}

enum class ShakeSensitivity(
    val label: String,
    val threshold: Float,
    val description: String
) {
    LOW("Low", 14.0f, "Requires a firm, deliberate shake"),
    MEDIUM("Medium", 9.5f, "Balanced for everyday handheld play"),
    HIGH("High", 6.0f, "Sensitive, responds to lighter flicks")
}

data class SingleDieState(
    val id: Int,
    val type: DiceType = DiceType.D6,
    val currentValue: Int = 1,
    val displayValue: Int = 1,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

data class RollHistoryEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val diceType: DiceType,
    val values: List<Int>,
    val modifier: Int = 0,
    val isShakeTriggered: Boolean = false
) {
    val total: Int get() = values.sum() + modifier

    val formattedTime: String
        get() = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(timestamp))
}
