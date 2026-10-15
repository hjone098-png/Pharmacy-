package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY tradeName ASC")
    fun getAllMedicationsFlow(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): Medication?

    @Query("""
        SELECT * FROM medications 
        WHERE tradeName LIKE '%' || :query || '%' 
           OR scientificName LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
        ORDER BY tradeName ASC
    """)
    fun searchMedications(query: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE category = :category ORDER BY tradeName ASC")
    fun getMedicationsByCategory(category: String): Flow<List<Medication>>

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<Medication>)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Query("UPDATE medications SET currentStock = :newStock, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    @Query("DELETE FROM medications")
    suspend fun clearAll()
}
