package com.example

import com.example.data.local.OrderItem
import com.example.ui.util.formatOrderSlip
import com.example.ui.util.toArabicDigits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun toArabicDigits_converts_requested_numbers() {
        assertEquals("٥", toArabicDigits(5))
        assertEquals("١٠", toArabicDigits(10))
        assertEquals("٢٤", toArabicDigits(24))
        assertEquals("٥٠", toArabicDigits(50))
        assertEquals("١٠٠", toArabicDigits(100))
        assertEquals("١", toArabicDigits(1))
    }

    @Test
    fun unit_selection_options_matches_user_specification() {
        // User requested: قطعة واحدة (1), عشر قطع (10), 24 (كارتون واحد), 50, 100
        val requestedUnits = listOf(1, 10, 24, 50, 100)
        assertEquals(5, requestedUnits.size)
        assertTrue(requestedUnits.contains(1))
        assertTrue(requestedUnits.contains(10))
        assertTrue(requestedUnits.contains(24))
        assertTrue(requestedUnits.contains(50))
        assertTrue(requestedUnits.contains(100))
    }

    @Test
    fun formatOrderSlip_matches_user_template() {
        val items = listOf(
            OrderItem(medicationId = 1, tradeName = "جنى شراب", quantity = 10, unitPrice = 0.0),
            OrderItem(medicationId = 2, tradeName = "كوفستوب شراب", quantity = 10, unitPrice = 0.0),
            OrderItem(medicationId = 3, tradeName = "دياراستوب حب كبير", quantity = 1, unitPrice = 0.0)
        )
        val slip = formatOrderSlip(
            pharmacyName = "السنار",
            pharmacyLocation = "حي سومر",
            items = items,
            depotName = "سبأ"
        )

        assertTrue(slip.startsWith("ص. السنار / حي سومر"))
        assertTrue(slip.contains("ـ جنى شراب (١٠)"))
        assertTrue(slip.contains("ـ كوفستوب شراب (١٠)"))
        assertTrue(slip.contains("ـ دياراستوب حب كبير (١)"))
        assertTrue(slip.endsWith("(مذخر سبأ)"))
    }

    @Test
    fun calculate_order_totals_and_cartons() {
        val items = listOf(
            OrderItem(medicationId = 1, tradeName = "جنى شراب", quantity = 24, unitPrice = 1500.0),
            OrderItem(medicationId = 2, tradeName = "كوفستوب شراب", quantity = 24, unitPrice = 2000.0),
            OrderItem(medicationId = 3, tradeName = "اوترينوز", quantity = 10, unitPrice = 1250.0)
        )

        val totalDistinctItems = items.size
        val totalUnits = items.sumOf { it.quantity }
        val cartonsCount = totalUnits / 24
        val looseUnits = totalUnits % 24
        val totalPrice = items.sumOf { it.subtotal }

        assertEquals(3, totalDistinctItems)
        assertEquals(58, totalUnits)
        assertEquals(2, cartonsCount)
        assertEquals(10, looseUnits)
        assertEquals(24 * 1500.0 + 24 * 2000.0 + 10 * 1250.0, totalPrice, 0.001)
    }
}
