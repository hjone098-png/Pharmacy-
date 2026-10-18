package com.example.data.local

import androidx.compose.runtime.Immutable

@Immutable
data class OrderItem(
    val medicationId: Long,
    val tradeName: String,
    val scientificName: String = "",
    val category: String = "",
    val dosageForm: String = "أقراص",
    val packageUnit: String = "علبة",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val imageUrl: String = "",
    val notes: String = ""
) {
    val subtotal: Double
        get() = quantity * unitPrice
}
