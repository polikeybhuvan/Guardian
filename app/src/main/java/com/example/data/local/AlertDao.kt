package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM mesh_messages ORDER BY createdAt DESC")
    fun getAllMessagesFlow(): Flow<List<MeshMessageEntity>>

    @Query("SELECT * FROM mesh_messages WHERE isLocalOrigin = 1 ORDER BY createdAt DESC LIMIT 1")
    fun getLatestLocalAlertFlow(): Flow<MeshMessageEntity?>

    @Query("SELECT * FROM mesh_messages WHERE msgId = :msgId LIMIT 1")
    suspend fun getMessageById(msgId: String): MeshMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MeshMessageEntity)

    @Update
    suspend fun updateMessage(message: MeshMessageEntity)

    @Query("UPDATE mesh_messages SET isGatewayUploaded = 1 WHERE msgId = :msgId")
    suspend fun markGatewayUploaded(msgId: String)

    @Query("UPDATE mesh_messages SET isAcknowledged = 1, ackByResponder = :responder WHERE msgId = :msgId")
    suspend fun markAcknowledged(msgId: String, responder: String)

    @Query("SELECT EXISTS(SELECT 1 FROM seen_messages WHERE msgId = :msgId)")
    suspend fun hasSeenMessage(msgId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markMessageSeen(seen: SeenMessageEntity)

    @Query("SELECT * FROM mesh_messages WHERE type = 'SOS' ORDER BY createdAt DESC")
    fun getDistressSignalsFlow(): Flow<List<MeshMessageEntity>>

    @Query("SELECT * FROM mesh_messages ORDER BY createdAt DESC")
    suspend fun getAllMessages(): List<MeshMessageEntity>

    @Query("SELECT COUNT(*) FROM mesh_messages WHERE type = 'SOS'")
    fun getCachedSignalsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mesh_messages")
    suspend fun getMessageCount(): Int

    @Query("DELETE FROM mesh_messages WHERE createdAt < :expiryTimestamp AND isLocalOrigin = 0")
    suspend fun deleteStaleMessages(expiryTimestamp: Long)
}
