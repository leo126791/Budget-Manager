package com.example.debit

import com.example.debit.ui.DebitUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebitViewModelTest {

    @Test
    fun testBudgetCalculations_NormalState() {
        val state = DebitUiState(
            currentYearMonth = "2026-09",
            totalExpense = 5000.0,
            totalBudgetLimit = 10000.0
        )

        assertEquals(5000.0, state.remainingBudget, 0.01)
        assertEquals(0.5f, state.totalBudgetProgress, 0.01f)
        assertFalse(state.isOverBudget)
        assertFalse(state.isNearBudgetLimit)
    }

    @Test
    fun testBudgetCalculations_NearLimitState() {
        val state = DebitUiState(
            currentYearMonth = "2026-09",
            totalExpense = 8500.0,
            totalBudgetLimit = 10000.0
        )

        assertEquals(1500.0, state.remainingBudget, 0.01)
        assertEquals(0.85f, state.totalBudgetProgress, 0.01f)
        assertFalse(state.isOverBudget)
        assertTrue(state.isNearBudgetLimit)
    }

    @Test
    fun testBudgetCalculations_OverBudgetState() {
        val state = DebitUiState(
            currentYearMonth = "2026-09",
            totalExpense = 12000.0,
            totalBudgetLimit = 10000.0
        )

        assertEquals(-2000.0, state.remainingBudget, 0.01)
        assertEquals(1.0f, state.totalBudgetProgress, 0.01f)
        assertTrue(state.isOverBudget)
        assertFalse(state.isNearBudgetLimit)
    }
}
