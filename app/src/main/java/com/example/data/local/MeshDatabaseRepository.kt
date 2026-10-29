package com.example.data.local

import com.example.data.model.EmergencyCenter
import com.example.data.model.MeshMessage
import com.example.domain.mesh.MeshPeerNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository pattern implementation for Room persistence of distress signals, mesh node statuses,
 * and emergency facilities (hospitals, police, relief camps).
 * Guarantees zero-data-loss and full offline functionality during total connectivity blackouts.
 */
class MeshDatabaseRepository(
    private val alertDao: AlertDao,
    private val meshNodeDao: MeshNodeDao,
    private val emergencyCenterDao: EmergencyCenterDao,
    private val familyMemberDao: FamilyMemberDao? = null,
    private val supplyExchangeDao: SupplyExchangeDao? = null,
    private val medicalProfileDao: MedicalProfileDao? = null
) {

    // Reactive streams of medical emergency profile
    val medicalProfileFlow: Flow<MedicalProfileEntity?> =
        medicalProfileDao?.getMedicalProfileFlow() ?: kotlinx.coroutines.flow.flowOf(null)

    // Reactive streams of cached family members (Family Circle & Proximity Alert)
    val familyMembersFlow: Flow<List<FamilyMemberEntity>> =
        familyMemberDao?.getAllFamilyMembersFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // Reactive streams of decentralized supply & resource exchange items
    val supplyExchangeFlow: Flow<List<SupplyExchangeEntity>> =
        supplyExchangeDao?.getActiveSuppliesFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    // Reactive streams of cached emergency centers (hospitals, police, relief camps)
    val cachedEmergencyCentersFlow: Flow<List<EmergencyCenter>> = emergencyCenterDao.getAllCachedCentersFlow()
        .map { list -> list.map { it.toDomain() } }

    val cachedCentersCountFlow: Flow<Int> = emergencyCenterDao.getCachedCentersCountFlow()

    // Reactive streams of cached distress signals & messages
    val cachedMessagesFlow: Flow<List<MeshMessage>> = alertDao.getAllMessagesFlow()
        .map { list -> list.map { it.toDomain() } }

    val cachedDistressSignalsFlow: Flow<List<MeshMessage>> = alertDao.getDistressSignalsFlow()
        .map { list -> list.map { it.toDomain() } }

    val latestLocalAlertFlow: Flow<MeshMessage?> = alertDao.getLatestLocalAlertFlow()
        .map { it?.toDomain() }

    val cachedSignalsCountFlow: Flow<Int> = alertDao.getCachedSignalsCountFlow()

    // Reactive streams of cached mesh peer nodes
    val cachedMeshNodesFlow: Flow<List<MeshPeerNode>> = meshNodeDao.getAllCachedNodesFlow()
        .map { list -> list.map { it.toDomain() } }

    val cachedNodesCountFlow: Flow<Int> = meshNodeDao.getCachedNodesCountFlow()

    val activeNodesCountFlow: Flow<Int> = meshNodeDao.getActiveNodesCountFlow()

    // --- Write Operations (Suspend) ---

    suspend fun cacheDistressSignal(message: MeshMessage, isLocalOrigin: Boolean = false) {
        alertDao.insertMessage(MeshMessageEntity.fromDomain(message, isLocalOrigin))
    }

    suspend fun cacheMeshNode(node: MeshPeerNode) {
        meshNodeDao.insertOrUpdateNode(MeshNodeStatusEntity.fromDomain(node))
    }

    suspend fun cacheMeshNodes(nodes: List<MeshPeerNode>) {
        val entities = nodes.map { MeshNodeStatusEntity.fromDomain(it) }
        meshNodeDao.insertOrUpdateNodes(entities)
    }

    suspend fun updateNodeTelemetry(nodeId: String, rssi: Int, batteryPct: Int) {
        meshNodeDao.updateTelemetry(nodeId, rssi, batteryPct, System.currentTimeMillis())
    }

    suspend fun updateNodeConnectivity(nodeId: String, isConnected: Boolean) {
        meshNodeDao.updateConnectivity(nodeId, isConnected)
    }

    suspend fun markAlertAcknowledged(msgId: String, responder: String) {
        alertDao.markAcknowledged(msgId, responder)
    }

    suspend fun markAlertGatewayUploaded(msgId: String) {
        alertDao.markGatewayUploaded(msgId)
    }

    suspend fun getAllCachedNodes(): List<MeshPeerNode> {
        return meshNodeDao.getAllCachedNodes().map { it.toDomain() }
    }

    suspend fun getAllCachedMessages(): List<MeshMessage> {
        return alertDao.getAllMessages().map { it.toDomain() }
    }

    suspend fun seedInitialNodesIfEmpty(initialNodes: List<MeshPeerNode>) {
        // No fake node seeding - keep mesh network genuine
    }

    suspend fun cacheEmergencyCenter(center: EmergencyCenter) {
        emergencyCenterDao.insertOrUpdateCenter(EmergencyCenterEntity.fromDomain(center))
    }

    suspend fun cacheEmergencyCenters(centers: List<EmergencyCenter>) {
        val entities = centers.map { EmergencyCenterEntity.fromDomain(it) }
        emergencyCenterDao.insertOrUpdateCenters(entities)
    }

    suspend fun getAllCachedCenters(): List<EmergencyCenter> {
        return emergencyCenterDao.getAllCachedCenters().map { it.toDomain() }
    }

    suspend fun seedInitialCentersIfEmpty(initialCenters: List<EmergencyCenter>) {
        val existing = emergencyCenterDao.getAllCachedCenters()
        if (existing.isEmpty() && initialCenters.isNotEmpty()) {
            cacheEmergencyCenters(initialCenters)
        }
    }

    // --- Family Circle Operations ---

    suspend fun addFamilyMember(member: FamilyMemberEntity) {
        familyMemberDao?.insertFamilyMember(member)
    }

    suspend fun removeFamilyMember(id: String) {
        familyMemberDao?.deleteFamilyMember(id)
    }

    suspend fun updateFamilyProximity(deviceId: String, isNearby: Boolean, distanceMeters: Float, lat: Double?, lng: Double?) {
        familyMemberDao?.updateProximity(
            deviceId = deviceId,
            isNearby = isNearby,
            distanceMeters = distanceMeters,
            timestamp = System.currentTimeMillis(),
            lat = lat,
            lng = lng
        )
    }

    suspend fun seedInitialFamilyIfEmpty() {
        // No fake family seeding - genuine user data only
    }

    // --- Supply Exchange Operations ---

    suspend fun addSupplyItem(item: SupplyExchangeEntity) {
        supplyExchangeDao?.insertSupply(item)
    }

    suspend fun markSupplyResolved(id: String) {
        supplyExchangeDao?.markResolved(id)
    }

    suspend fun resolveSupplyItem(id: String) {
        supplyExchangeDao?.markResolved(id)
    }

    suspend fun seedInitialSuppliesIfEmpty() {
        // No fake supplies seeding - genuine user data only
    }

    // --- Emergency Medical Profile Operations ---

    suspend fun getMedicalProfile(): MedicalProfileEntity? {
        return medicalProfileDao?.getMedicalProfile()
    }

    suspend fun saveMedicalProfile(profile: MedicalProfileEntity) {
        medicalProfileDao?.insertOrUpdateProfile(profile)
    }

    suspend fun clearMedicalProfile() {
        medicalProfileDao?.clearProfile()
    }
}
