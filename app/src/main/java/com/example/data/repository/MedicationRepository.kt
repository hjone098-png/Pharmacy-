package com.example.data.repository

import com.example.data.local.Medication
import com.example.data.local.MedicationDao
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val medicationDao: MedicationDao) {
    val allMedications: Flow<List<Medication>> = medicationDao.getAllMedicationsFlow()

    fun searchMedications(query: String): Flow<List<Medication>> {
        return if (query.isBlank()) {
            medicationDao.getAllMedicationsFlow()
        } else {
            medicationDao.searchMedications(query.trim())
        }
    }

    suspend fun getMedicationById(id: Long): Medication? = medicationDao.getMedicationById(id)

    suspend fun insert(medication: Medication): Long = medicationDao.insertMedication(medication)

    suspend fun update(medication: Medication) = medicationDao.updateMedication(medication)

    suspend fun updateStock(id: Long, newStock: Int) = medicationDao.updateStock(id, newStock)

    suspend fun delete(medication: Medication) = medicationDao.deleteMedication(medication)

    suspend fun deleteById(id: Long) = medicationDao.deleteMedicationById(id)

    suspend fun clearAll() = medicationDao.clearAll()
    suspend fun getCount(): Int = medicationDao.getCount()
}
