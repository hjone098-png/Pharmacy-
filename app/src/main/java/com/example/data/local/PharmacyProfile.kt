package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pharmacy_profile")
data class PharmacyProfile(
    @PrimaryKey val id: Int = 1,
    val pharmacyName: String = "",
    val pharmacistName: String = "",
    val licenseNumber: String = "",
    val phoneNumber: String = "",
    val city: String = "",
    val neighborhood: String = "",
    val streetAddress: String = "",
    val locationCoordinatesOrUrl: String = "",
    val preferredSupplier: String = "",
    val defaultNotes: String = "",
    val isConfigured: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
