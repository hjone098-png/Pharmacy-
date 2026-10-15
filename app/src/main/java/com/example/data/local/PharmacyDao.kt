package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmacyDao {
    @Query("SELECT * FROM pharmacy_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<PharmacyProfile?>

    @Query("SELECT * FROM pharmacy_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): PharmacyProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: PharmacyProfile)

    @Query("DELETE FROM pharmacy_profile")
    suspend fun clearProfile()
}
