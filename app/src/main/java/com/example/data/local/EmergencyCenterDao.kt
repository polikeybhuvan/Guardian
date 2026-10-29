package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local caching of hospitals, police stations, and relief facilities.
 */
@Dao
interface EmergencyCenterDao {

    @Query("SELECT * FROM cached_emergency_centers ORDER BY name ASC")
    fun getAllCachedCentersFlow(): Flow<List<EmergencyCenterEntity>>

    @Query("SELECT * FROM cached_emergency_centers ORDER BY name ASC")
    suspend fun getAllCachedCenters(): List<EmergencyCenterEntity>

    @Query("SELECT * FROM cached_emergency_centers WHERE type = :type ORDER BY name ASC")
    fun getCentersByTypeFlow(type: String): Flow<List<EmergencyCenterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCenters(centers: List<EmergencyCenterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCenter(center: EmergencyCenterEntity)

    @Query("SELECT COUNT(*) FROM cached_emergency_centers")
    fun getCachedCentersCountFlow(): Flow<Int>

    @Query("DELETE FROM cached_emergency_centers WHERE id = :id")
    suspend fun deleteCenter(id: String)

    @Query("DELETE FROM cached_emergency_centers")
    suspend fun deleteAllCenters()
}
