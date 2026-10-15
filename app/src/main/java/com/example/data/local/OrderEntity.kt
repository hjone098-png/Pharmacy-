package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val createdAt: Long = System.currentTimeMillis(),
    val pharmacyName: String,
    val pharmacistName: String,
    val phoneNumber: String,
    val fullAddress: String,
    val locationCoordinatesOrUrl: String,
    val itemsSummary: String, // Clean textual list of items e.g. "1. Panadol (10 علب)..."
    val totalItemsCount: Int,
    val totalUnitsCount: Int,
    val totalEstimatedPrice: Double,
    val urgencyLevel: String = "عادي", // عادي، عاجل، طارئ
    val supplierName: String = "",
    val notes: String = "",
    val formattedCopyText: String = "" // Ready-to-copy complete formatted message
)
