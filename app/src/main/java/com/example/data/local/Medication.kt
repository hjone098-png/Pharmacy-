package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tradeName: String,
    val scientificName: String = "",
    val category: String = "عام",
    val dosageForm: String = "أقراص",
    val packageUnit: String = "علبة",
    val currentStock: Int = 0,
    val minStockAlert: Int = 10,
    val unitPrice: Double = 0.0,
    val manufacturer: String = "",
    val imageUrl: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentStock <= minStockAlert && currentStock > 0

    val isOutOfStock: Boolean
        get() = currentStock <= 0
}
