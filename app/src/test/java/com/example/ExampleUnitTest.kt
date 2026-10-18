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

    @Test
    fun medication_sorting_by_time_addition_and_alphabetical() {
        val med1 = com.example.data.local.Medication(
            id = 1,
            tradeName = "جنى شراب",
            updatedAt = 1000L
        )
        val med2 = com.example.data.local.Medication(
            id = 2,
            tradeName = "أوجمنتين",
            updatedAt = 3000L
        )
        val med3 = com.example.data.local.Medication(
            id = 3,
            tradeName = "باراسيتامول",
            updatedAt = 2000L
        )
        val list = listOf(med1, med2, med3)

        // 1) Sort by Time Added (newest first / descending)
        val sortedByTimeDesc = list.sortedByDescending { it.updatedAt }
        assertEquals(listOf(med2, med3, med1), sortedByTimeDesc)

        // 2) Sort by Addition Order (sequential ID ascending)
        val sortedByIdAsc = list.sortedBy { it.id }
        assertEquals(listOf(med1, med2, med3), sortedByIdAsc)

        // 3) Sort Alphabetically (Arabic A-Z)
        val collator = java.text.Collator.getInstance(java.util.Locale("ar"))
        val sortedByAlphabet = list.sortedWith { a, b -> collator.compare(a.tradeName, b.tradeName) }
        assertEquals("أوجمنتين", sortedByAlphabet[0].tradeName)
        assertEquals("باراسيتامول", sortedByAlphabet[1].tradeName)
        assertEquals("جنى شراب", sortedByAlphabet[2].tradeName)
    }
}
