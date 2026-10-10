package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DiceType
import com.example.model.ShakeSensitivity
import com.example.viewmodel.DiceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies ShakeDice app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ShakeDice", appName)
    }

    @Test
    fun `dice roll values are within expected bounds for all dice types`() {
        DiceType.values().forEach { type ->
            repeat(30) {
                val rolled = type.roll()
                assertTrue(
                    "Rolled $rolled for ${type.label} was out of bounds 1..${type.sides}",
                    rolled in 1..type.sides
                )
            }
        }
    }

    @Test
    fun `dice view model updates count and types correctly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = DiceViewModel(app)

        assertEquals(2, viewModel.uiState.value.diceCount)
        assertEquals(DiceType.D6, viewModel.uiState.value.diceType)

        viewModel.setDiceCount(5)
        assertEquals(5, viewModel.uiState.value.diceCount)
        assertEquals(5, viewModel.uiState.value.diceList.size)

        viewModel.setDiceType(DiceType.D20)
        assertEquals(DiceType.D20, viewModel.uiState.value.diceType)
        viewModel.uiState.value.diceList.forEach { die ->
            assertTrue(die.currentValue in 1..20)
        }
    }

    @Test
    fun `sensitivity threshold definitions are valid`() {
        assertTrue(ShakeSensitivity.HIGH.threshold < ShakeSensitivity.MEDIUM.threshold)
        assertTrue(ShakeSensitivity.MEDIUM.threshold < ShakeSensitivity.LOW.threshold)
    }
}
