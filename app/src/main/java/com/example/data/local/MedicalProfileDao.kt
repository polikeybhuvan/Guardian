package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalProfileDao {
    @Query("SELECT * FROM medical_profile WHERE id = 'primary_user_medical_profile' LIMIT 1")
    fun getMedicalProfileFlow(): Flow<MedicalProfileEntity?>

    @Query("SELECT * FROM medical_profile WHERE id = 'primary_user_medical_profile' LIMIT 1")
    suspend fun getMedicalProfile(): MedicalProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: MedicalProfileEntity)

    @Query("DELETE FROM medical_profile WHERE id = 'primary_user_medical_profile'")
    suspend fun clearProfile()
}
