package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "supply_exchange")
data class SupplyExchangeEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "REQUEST" (Need) or "OFFER" (Available)
    val category: String, // "WATER", "FOOD", "MEDICINE", "SHELTER", "POWER", "TOOLS"
    val title: String,
    val description: String,
    val quantity: String,
    val locationLabel: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contactInfo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

@Dao
interface SupplyExchangeDao {
    @Query("SELECT * FROM supply_exchange WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getActiveSuppliesFlow(): Flow<List<SupplyExchangeEntity>>

    @Query("SELECT * FROM supply_exchange ORDER BY timestamp DESC")
    suspend fun getAllSupplies(): List<SupplyExchangeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupply(item: SupplyExchangeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplies(items: List<SupplyExchangeEntity>)

    @Query("UPDATE supply_exchange SET isResolved = 1 WHERE id = :id")
    suspend fun markResolved(id: String)

    @Query("DELETE FROM supply_exchange WHERE id = :id")
    suspend fun deleteSupply(id: String)
}
