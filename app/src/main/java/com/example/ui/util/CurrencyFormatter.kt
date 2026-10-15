package com.example.ui.util

import java.util.Locale

/**
 * Utility functions for formatting prices in Iraqi Dinar (د.ع / دينار عراقي)
 */
fun formatIraqiDinar(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f د.ع", amount)
    } else {
        String.format(Locale.US, "%,.2f د.ع", amount)
    }
}

fun formatPrice(amount: Double): String {
    return formatIraqiDinar(amount)
}
