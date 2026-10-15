package com.example.data.repository

import com.example.data.local.PharmacyDao
import com.example.data.local.PharmacyProfile
import kotlinx.coroutines.flow.Flow

class PharmacyRepository(private val pharmacyDao: PharmacyDao) {
    val profileFlow: Flow<PharmacyProfile?> = pharmacyDao.getProfileFlow()

    suspend fun getProfile(): PharmacyProfile? = pharmacyDao.getProfile()

    suspend fun saveProfile(profile: PharmacyProfile) {
        pharmacyDao.saveProfile(profile.copy(id = 1, isConfigured = true, updatedAt = System.currentTimeMillis()))
    }
}
