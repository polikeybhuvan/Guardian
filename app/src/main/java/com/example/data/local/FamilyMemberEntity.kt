package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val relationship: String,
    val deviceId: String,
    val phoneNumber: String,
    val lastSeenLatitude: Double? = null,
    val lastSeenLongitude: Double? = null,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isNearby: Boolean = false,
    val distanceMeters: Float = 0f
)

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY isNearby DESC, lastSeenTimestamp DESC")
    fun getAllFamilyMembersFlow(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members")
    suspend fun getAllFamilyMembers(): List<FamilyMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMembers(members: List<FamilyMemberEntity>)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteFamilyMember(id: String)

    @Query("UPDATE family_members SET isNearby = :isNearby, distanceMeters = :distanceMeters, lastSeenTimestamp = :timestamp, lastSeenLatitude = :lat, lastSeenLongitude = :lng WHERE deviceId = :deviceId")
    suspend fun updateProximity(deviceId: String, isNearby: Boolean, distanceMeters: Float, timestamp: Long, lat: Double?, lng: Double?)
}
