package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.DiceFeedbackManager
import com.example.model.DiceTheme
import com.example.model.DiceType
import com.example.model.RollHistoryEntry
import com.example.model.ShakeSensitivity
import com.example.model.SingleDieState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class DiceUiState(
    val diceCount: Int = 2,
    val diceType: DiceType = DiceType.D6,
    val diceList: List<SingleDieState> = emptyList(),
    val isRolling: Boolean = false,
    val shakeSensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
    val shakeForce: Float = 0f,
    val isShakeSensorActive: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isShakeEnabled: Boolean = true,
    val diceTheme: DiceTheme = DiceTheme.CLASSIC,
    val modifierBonus: Int = 0,
    val history: List<RollHistoryEntry> = emptyList(),
    val showHistorySheet: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val lastRollTotal: Int = 0,
    val lastRollBreakdown: String = ""
)

class DiceViewModel(application: Application) : AndroidViewModel(application) {

    private val feedbackManager = DiceFeedbackManager(application.applicationContext)

    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    private var rollJob: Job? = null

    init {
        // Initialize with 2 D6 dice
        setupInitialDice(2, DiceType.D6)
    }

    private fun setupInitialDice(count: Int, type: DiceType) {
        val initialDice = (0 until count).map { index ->
            val initialVal = type.roll()
            SingleDieState(
                id = index,
                type = type,
                currentValue = initialVal,
                displayValue = initialVal
            )
        }
        val total = initialDice.sumOf { it.currentValue }
        _uiState.update { current ->
            current.copy(
                diceCount = count,
                diceType = type,
                diceList = initialDice,
                lastRollTotal = total,
                lastRollBreakdown = initialDice.joinToString(" + ") { it.currentValue.toString() }
            )
        }
    }

    fun setDiceCount(newCount: Int) {
        val safeCount = newCount.coerceIn(1, 6)
        if (safeCount == _uiState.value.diceCount) return

        val currentType = _uiState.value.diceType
        val currentList = _uiState.value.diceList.toMutableList()

        val updatedList = if (safeCount > currentList.size) {
            val toAdd = (currentList.size until safeCount).map { idx ->
                val v = currentType.roll()
                SingleDieState(id = idx, type = currentType, currentValue = v, displayValue = v)
            }
            currentList + toAdd
        } else {
            currentList.take(safeCount)
        }

        val total = updatedList.sumOf { it.currentValue } + _uiState.value.modifierBonus
        _uiState.update {
            it.copy(
                diceCount = safeCount,
                diceList = updatedList,
                lastRollTotal = total,
                lastRollBreakdown = updatedList.joinToString(" + ") { d -> d.currentValue.toString() }
            )
        }
    }

    fun setDiceType(newType: DiceType) {
        if (newType == _uiState.value.diceType) return
        val count = _uiState.value.diceCount
        val updated = (0 until count).map { idx ->
            val v = newType.roll()
            SingleDieState(id = idx, type = newType, currentValue = v, displayValue = v)
        }
        val total = updated.sumOf { it.currentValue } + _uiState.value.modifierBonus
        _uiState.update {
            it.copy(
                diceType = newType,
                diceList = updated,
                lastRollTotal = total,
                lastRollBreakdown = updated.joinToString(" + ") { d -> d.currentValue.toString() }
            )
        }
    }

    fun setSensitivity(sensitivity: ShakeSensitivity) {
        _uiState.update { it.copy(shakeSensitivity = sensitivity) }
    }

    fun setDiceTheme(theme: DiceTheme) {
        _uiState.update { it.copy(diceTheme = theme) }
    }

    fun setModifier(bonus: Int) {
        val clamped = bonus.coerceIn(-20, 20)
        _uiState.update {
            val baseSum = it.diceList.sumOf { d -> d.currentValue }
            it.copy(
                modifierBonus = clamped,
                lastRollTotal = baseSum + clamped
            )
        }
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(isHapticsEnabled = !it.isHapticsEnabled) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
    }

    fun toggleShake() {
        _uiState.update { it.copy(isShakeEnabled = !it.isShakeEnabled) }
    }

    fun setShowHistorySheet(show: Boolean) {
        _uiState.update { it.copy(showHistorySheet = show) }
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun updateLiveAcceleration(magnitude: Float) {
        _uiState.update { it.copy(shakeForce = magnitude) }
    }

    fun updateSensorActive(active: Boolean) {
        _uiState.update { it.copy(isShakeSensorActive = active) }
    }

    /**
     * Rolls all dice with tumbling animation, random face cycling, haptics, and audio cues.
     */
    fun rollDice(triggeredByShake: Boolean = false) {
        val state = _uiState.value
        if (state.isRolling) return
        if (triggeredByShake && !state.isShakeEnabled) return

        rollJob?.cancel()
        rollJob = viewModelScope.launch {
            _uiState.update { it.copy(isRolling = true) }

            val totalFrames = 10
            val frameDelay = 65L
            val diceType = state.diceType
            val count = state.diceCount

            // Fast tumbling loop
            for (step in 0 until totalFrames) {
                // Haptic and audio tick on alternate frames
                if (step % 2 == 0) {
                    feedbackManager.performTumbleTick(state.isHapticsEnabled)
                    feedbackManager.playTumbleSound(state.isSoundEnabled)
                }

                _uiState.update { current ->
                    val tumbledDice = current.diceList.map { die ->
                        val randomFace = (1..diceType.sides).random()
                        die.copy(
                            displayValue = randomFace,
                            rotationX = Random.nextFloat() * 60f - 30f,
                            rotationY = Random.nextFloat() * 60f - 30f,
                            rotationZ = Random.nextFloat() * 70f - 35f,
                            scale = 0.94f + Random.nextFloat() * 0.16f,
                            offsetX = Random.nextFloat() * 24f - 12f,
                            offsetY = Random.nextFloat() * 24f - 12f
                        )
                    }
                    current.copy(diceList = tumbledDice)
                }
                delay(frameDelay)
            }

            // Settle on final values
            val finalValues = List(count) { diceType.roll() }
            val settledDice = List(count) { index ->
                SingleDieState(
                    id = index,
                    type = diceType,
                    currentValue = finalValues[index],
                    displayValue = finalValues[index],
                    rotationX = 0f,
                    rotationY = 0f,
                    rotationZ = 0f,
                    scale = 1f,
                    offsetX = 0f,
                    offsetY = 0f
                )
            }

            val total = finalValues.sum() + _uiState.value.modifierBonus
            val breakdown = finalValues.joinToString(" + ")

            // Final tactile impact & sound
            feedbackManager.performSettleImpact(state.isHapticsEnabled)
            feedbackManager.playImpactSound(state.isSoundEnabled)

            val newHistoryEntry = RollHistoryEntry(
                diceType = diceType,
                values = finalValues,
                modifier = _uiState.value.modifierBonus,
                isShakeTriggered = triggeredByShake
            )

            _uiState.update { current ->
                val newHistory = (listOf(newHistoryEntry) + current.history).take(50)
                current.copy(
                    isRolling = false,
                    diceList = settledDice,
                    lastRollTotal = total,
                    lastRollBreakdown = breakdown,
                    history = newHistory
                )
            }
        }
    }
}
