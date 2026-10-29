package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeshNodeDao {

    @Query("SELECT * FROM mesh_nodes ORDER BY lastSeenTimestamp DESC")
    fun getAllCachedNodesFlow(): Flow<List<MeshNodeStatusEntity>>

    @Query("SELECT * FROM mesh_nodes ORDER BY lastSeenTimestamp DESC")
    suspend fun getAllCachedNodes(): List<MeshNodeStatusEntity>

    @Query("SELECT * FROM mesh_nodes WHERE nodeId = :nodeId LIMIT 1")
    suspend fun getNodeById(nodeId: String): MeshNodeStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNode(node: MeshNodeStatusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNodes(nodes: List<MeshNodeStatusEntity>)

    @Query("UPDATE mesh_nodes SET isConnected = :connected WHERE nodeId = :nodeId")
    suspend fun updateConnectivity(nodeId: String, connected: Boolean)

    @Query("UPDATE mesh_nodes SET rssi = :rssi, batteryPct = :battery, lastSeenTimestamp = :timestamp WHERE nodeId = :nodeId")
    suspend fun updateTelemetry(nodeId: String, rssi: Int, battery: Int, timestamp: Long)

    @Query("DELETE FROM mesh_nodes WHERE nodeId = :nodeId")
    suspend fun deleteNode(nodeId: String)

    @Query("DELETE FROM mesh_nodes")
    suspend fun deleteAllNodes()

    @Query("SELECT COUNT(*) FROM mesh_nodes")
    fun getCachedNodesCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mesh_nodes WHERE isConnected = 1")
    fun getActiveNodesCountFlow(): Flow<Int>
}
