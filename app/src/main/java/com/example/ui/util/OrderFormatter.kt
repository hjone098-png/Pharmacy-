package com.example.ui.util

import com.example.data.local.OrderItem

/**
 * Converts Western digits (0-9) to Eastern Arabic digits (٠-٩)
 * e.g. 10 -> ١٠, 1 -> ١
 */
fun toArabicDigits(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = number.toString()
    val sb = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(arabicDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

/**
 * Formats the order slip exactly as requested by the user:
 *
 * ص. السنار / حي سومر
 * ـ جنى شراب (١٠)
 * ـ كوفستوب شراب (١٠)
 * ...
 *
 * (مذخر سبأ)
 */
fun formatOrderSlip(
    pharmacyName: String,
    pharmacyLocation: String,
    items: List<OrderItem>,
    depotName: String
): String {
    val trimmedName = pharmacyName.trim()
    val cleanPharmacy = when {
        trimmedName.startsWith("ص.") -> trimmedName
        trimmedName.startsWith("ص /") -> trimmedName.replaceFirst("ص /", "ص.")
        trimmedName.startsWith("صيدلية") -> trimmedName.replaceFirst(Regex("^صيدلية\\s*"), "ص. ")
        trimmedName.isNotBlank() -> "ص. $trimmedName"
        else -> ""
    }

    val trimmedLocation = pharmacyLocation.trim()
    val header = when {
        cleanPharmacy.isNotBlank() && trimmedLocation.isNotBlank() -> "$cleanPharmacy / $trimmedLocation"
        cleanPharmacy.isNotBlank() -> cleanPharmacy
        trimmedLocation.isNotBlank() -> trimmedLocation
        else -> ""
    }

    val itemsBlock = if (items.isNotEmpty()) {
        items.joinToString("\n") { item ->
            val arabicQty = toArabicDigits(item.quantity)
            "ـ ${item.tradeName.trim()} ($arabicQty)"
        }
    } else {
        "ـ لا توجد مواد مختارة (٠)"
    }

    val trimmedDepot = depotName.trim()
    val cleanDepot = when {
        trimmedDepot.startsWith("مذخر") -> trimmedDepot
        trimmedDepot.isNotBlank() -> "مذخر $trimmedDepot"
        else -> "مذخر سبأ"
    }

    return buildString {
        if (header.isNotBlank()) {
            appendLine(header)
        }
        appendLine(itemsBlock)
        appendLine()
        append("($cleanDepot)")
    }
}
