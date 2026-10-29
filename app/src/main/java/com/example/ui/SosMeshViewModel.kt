package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SosMeshApp
import com.example.data.location.DeviceCoordinates
import com.example.data.local.MeshMessageEntity
import com.example.data.model.AlertPriority
import com.example.data.model.DeliveryStep
import com.example.data.model.EmergencyCategory
import com.example.data.model.MeshMessage
import com.example.data.model.MessageType
import com.example.data.model.TriageResult
import com.example.domain.mesh.MeshLogEvent
import com.example.domain.mesh.MeshPeerNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.FamilyMemberEntity
import com.example.data.local.SupplyExchangeEntity
import com.example.data.location.SmsDispatchResult
import com.example.data.model.EmergencyCenter
import com.example.data.model.TriageChatMessage
import com.example.domain.ai.OfflineGemmaTriageReport
import com.example.domain.mesh.VoiceMemoItem
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    CITIZEN_SOS("SOS Distress"),
    CONTACTS("Emergency Contacts"),
    FAMILY("Family Radar"),
    NEARBY_HELP("Nearby Help & Map"),
    SURVIVAL_GUIDE("First-Aid & Triage"),
    SUPPLIES("Mutual Aid"),
    MEDICAL_ID("Medical ID"),
    RESPONDER("Responders"),
    DIAGNOSTICS("Diagnostics")
}

class SosMeshViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SosMeshApp
    private val alertDao = app.database.alertDao()
    private val emergencyContactDao = app.database.emergencyContactDao()
    private val routingEngine = app.routingEngine
    private val triageService = app.triageService
    private val meshTransport = app.meshTransport
    private val emergencyCentersRepository = app.emergencyCentersRepository
    val meshDatabaseRepository = app.meshDatabaseRepository
    val smsDispatcher = app.smsDispatcher
    val beaconController = app.beaconController
    val compassManager = app.compassManager

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.CITIZEN_SOS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Citizen SOS Form State
    private val _selectedCategory = MutableStateFlow(EmergencyCategory.FLOOD)
    val selectedCategory: StateFlow<EmergencyCategory> = _selectedCategory.asStateFlow()

    private val _peopleCount = MutableStateFlow(1)
    val peopleCount: StateFlow<Int> = _peopleCount.asStateFlow()

    private val _userNote = MutableStateFlow("")
    val userNote: StateFlow<String> = _userNote.asStateFlow()

    private val _currentCoordinates = MutableStateFlow<DeviceCoordinates?>(null)
    val currentCoordinates: StateFlow<DeviceCoordinates?> = _currentCoordinates.asStateFlow()

    private val _isTriageLoading = MutableStateFlow(false)
    val isTriageLoading: StateFlow<Boolean> = _isTriageLoading.asStateFlow()

    private val _triagePreview = MutableStateFlow<TriageResult?>(null)
    val triagePreview: StateFlow<TriageResult?> = _triagePreview.asStateFlow()

    // Local rule-based triage guidance for the SOS form
    private val _offlineGemmaReport = MutableStateFlow<OfflineGemmaTriageReport?>(null)
    val offlineGemmaReport: StateFlow<OfflineGemmaTriageReport?> = _offlineGemmaReport.asStateFlow()

    // Interactive Symptom Triage Tool (Tab 4)
    private val _customSymptomInput = MutableStateFlow("")
    val customSymptomInput: StateFlow<String> = _customSymptomInput.asStateFlow()

    private val _customGemmaReport = MutableStateFlow<OfflineGemmaTriageReport?>(null)
    val customGemmaReport: StateFlow<OfflineGemmaTriageReport?> = _customGemmaReport.asStateFlow()

    // First-aid guidance chat state (Tab 4)
    private val initialWelcomeMessage = TriageChatMessage(
        isUser = false,
        text = "Tell me what happened, the symptoms or injuries, when they began, and how many people need help. Add your location if you can. Call 112 now for immediate danger; don’t wait for chat."
    )

    private val _triageChatMessages = MutableStateFlow<List<TriageChatMessage>>(listOf(initialWelcomeMessage))
    val triageChatMessages: StateFlow<List<TriageChatMessage>> = _triageChatMessages.asStateFlow()

    private val _isTriageAiTyping = MutableStateFlow(false)
    val isTriageAiTyping: StateFlow<Boolean> = _isTriageAiTyping.asStateFlow()
    val offlineGemmaStatus: StateFlow<String> = triageService.offlineGemmaStatus

    // Countdown state (5s mistake cancel window)
    private val _isCountdownActive = MutableStateFlow(false)
    val isCountdownActive: StateFlow<Boolean> = _isCountdownActive.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(5)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private var countdownJob: Job? = null

    // Room Flow of Latest User-originated Alert
    val latestLocalAlert: StateFlow<MeshMessage?> = alertDao.getLatestLocalAlertFlow()
        .map { it?.toDomain() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All Received Incidents for Responders
    val allIncidents: StateFlow<List<MeshMessage>> = alertDao.getAllMessagesFlow()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real Radio & Network Telemetry
    val connectedPeersCount: StateFlow<Int> = meshTransport.connectedPeersCount
    val isInternetAvailable: StateFlow<Boolean> = meshTransport.isInternetAvailable
    val discoveredPeers: StateFlow<List<MeshPeerNode>> = meshTransport.discoveredPeers
    val transmissionLogs: StateFlow<List<MeshLogEvent>> = meshTransport.transmissionLogs
    val deviceId: String = app.cryptoSigner.getDeviceId()

    private val _isBluetoothEnabled = MutableStateFlow(meshTransport.isBluetoothEnabled())
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isWifiEnabled = MutableStateFlow(meshTransport.isWifiEnabled())
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    // Responder Filter
    private val _priorityFilter = MutableStateFlow<AlertPriority?>(null)
    val priorityFilter: StateFlow<AlertPriority?> = _priorityFilter.asStateFlow()

    // Emergency Contacts Management
    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContactsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nearby Emergency Centers (Hospitals, Police, Relief) backed by Room offline cache
    val emergencyCenters: StateFlow<List<EmergencyCenter>> = _currentCoordinates
        .map { coords ->
            val liveCenters = emergencyCentersRepository.getEmergencyCenters(coords?.latitude, coords?.longitude)
            if (liveCenters.isNotEmpty()) {
                meshDatabaseRepository.cacheEmergencyCenters(liveCenters)
                liveCenters
            } else {
                // Do not surface legacy cached entries with unverified facility coordinates.
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emergencyCentersRepository.getEmergencyCenters(null, null))

    val nearestHospital: StateFlow<EmergencyCenter?> = _currentCoordinates
        .map { coords -> emergencyCentersRepository.getNearestHospital(coords?.latitude, coords?.longitude) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emergencyCentersRepository.getNearestHospital(null, null))

    val nearestPoliceStation: StateFlow<EmergencyCenter?> = _currentCoordinates
        .map { coords -> emergencyCentersRepository.getNearestPoliceStation(coords?.latitude, coords?.longitude) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emergencyCentersRepository.getNearestPoliceStation(null, null))

    val nearestReliefCamp: StateFlow<EmergencyCenter?> = _currentCoordinates
        .map { coords -> emergencyCentersRepository.getNearestReliefCamp(coords?.latitude, coords?.longitude) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emergencyCentersRepository.getNearestReliefCamp(null, null))

    // Surrounding Mesh Peer Nodes discovered via local Bluetooth LE / Wi-Fi Direct
    val surroundingMeshNodes: StateFlow<List<MeshPeerNode>> = meshTransport.discoveredPeers

    // Room Database Offline Cache Reactive Streams
    val cachedDistressSignals: StateFlow<List<MeshMessage>> = meshDatabaseRepository.cachedDistressSignalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedMeshNodes: StateFlow<List<MeshPeerNode>> = meshDatabaseRepository.cachedMeshNodesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedEmergencyCenters: StateFlow<List<EmergencyCenter>> = meshDatabaseRepository.cachedEmergencyCentersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedSignalsCount: StateFlow<Int> = meshDatabaseRepository.cachedSignalsCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cachedNodesCount: StateFlow<Int> = meshDatabaseRepository.cachedNodesCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cachedEmergencyCentersCount: StateFlow<Int> = meshDatabaseRepository.cachedCentersCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // SMS Dispatch telemetry
    private val _lastSmsResult = MutableStateFlow<SmsDispatchResult?>(null)
    val lastSmsResult: StateFlow<SmsDispatchResult?> = _lastSmsResult.asStateFlow()

    // Hardware Beacon (Siren & Torch SOS Strobe)
    val isTorchActive: StateFlow<Boolean> = beaconController.isTorchActive
    val isSirenActive: StateFlow<Boolean> = beaconController.isSirenActive
    val sirenError: StateFlow<String?> = beaconController.sirenError

    // Hardware Compass / Radar
    val compassAzimuth: StateFlow<Float> = compassManager.azimuthDegrees
    val isCompassAvailable: StateFlow<Boolean> = compassManager.isSensorAvailable

    // New Feature Singletons
    val morseSoundBeacon = app.morseSoundBeacon
    val whistleDetector = app.whistleDetector
    val walkieTalkieManager = app.walkieTalkieManager

    // Sound Beacon & Whistle state flows
    val isMorseBeaconActive: StateFlow<Boolean> = morseSoundBeacon.isPlaying
    val isWhistleListening: StateFlow<Boolean> = whistleDetector.isListening
    val isWhistleStarting: StateFlow<Boolean> = whistleDetector.isStarting
    val whistleError: StateFlow<String?> = whistleDetector.lastError
    val whistleConfidence: StateFlow<Float> = whistleDetector.detectionConfidence
    val whistleDetectedCount: StateFlow<Int> = whistleDetector.whistleDetectedCount

    // Walkie-Talkie state flows
    val isVoiceRecording: StateFlow<Boolean> = walkieTalkieManager.isRecording
    val isVoicePlaying: StateFlow<Boolean> = walkieTalkieManager.isPlaying
    val voiceMemos: StateFlow<List<VoiceMemoItem>> = walkieTalkieManager.recordedMemos

    // Family Circle state flows
    val familyMembers: StateFlow<List<FamilyMemberEntity>> = meshDatabaseRepository.familyMembersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Decentralized Supply Exchange state flows
    val supplyItems: StateFlow<List<SupplyExchangeEntity>> = meshDatabaseRepository.supplyExchangeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Full-screen Beacon and HUD state toggles
    private val _showCompassHud = MutableStateFlow(false)
    val showCompassHud: StateFlow<Boolean> = _showCompassHud.asStateFlow()

    private val _showFloodlight = MutableStateFlow(false)
    val showFloodlight: StateFlow<Boolean> = _showFloodlight.asStateFlow()

    // --- New Community Impact & Life Safety Managers ---
    val wifiEmergencyBeaconManager = com.example.data.location.WifiEmergencyBeaconManager(application)

    val medicalProfile: StateFlow<com.example.data.local.MedicalProfileEntity?> =
        meshDatabaseRepository.medicalProfileFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showBleedDialog = MutableStateFlow(false)
    val showBleedDialog: StateFlow<Boolean> = _showBleedDialog.asStateFlow()

    private val _isBackgroundRelayActive = MutableStateFlow(
        com.example.domain.service.GuardianMeshRelayService.isRelayEnabled(app)
    )
    val isBackgroundRelayActive: StateFlow<Boolean> = _isBackgroundRelayActive.asStateFlow()
    private val _relayStatusError = MutableStateFlow<String?>(null)
    val relayStatusError: StateFlow<String?> = _relayStatusError.asStateFlow()

    val isWifiBeaconActive: StateFlow<Boolean> = wifiEmergencyBeaconManager.isBeaconActive
    val wifiBeaconSsid: StateFlow<String?> = wifiEmergencyBeaconManager.activeSsid

    init {
        updateTriagePreview()
        updateCoordinates()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: EmergencyCategory) {
        _selectedCategory.value = category
        updateTriagePreview()
    }

    fun setPeopleCount(count: Int) {
        _peopleCount.value = count.coerceIn(1, 99)
        updateTriagePreview()
    }

    fun setUserNote(note: String) {
        _userNote.value = note
        updateTriagePreview()
    }

    fun setPriorityFilter(priority: AlertPriority?) {
        _priorityFilter.value = priority
    }

    fun updateCoordinates() {
        viewModelScope.launch {
            val coords = app.locationHelper.getCurrentCoordinates()
            _currentCoordinates.value = coords
            val nodes = emergencyCentersRepository.getSurroundingMeshNodes(coords?.latitude, coords?.longitude)
            if (nodes.isNotEmpty()) {
                meshDatabaseRepository.cacheMeshNodes(nodes)
            }
            val centers = emergencyCentersRepository.getEmergencyCenters(coords?.latitude, coords?.longitude)
            if (centers.isNotEmpty()) {
                meshDatabaseRepository.cacheEmergencyCenters(centers)
            }
        }
    }

    fun refreshRadioState() {
        _isBluetoothEnabled.value = meshTransport.isBluetoothEnabled()
        _isWifiEnabled.value = meshTransport.isWifiEnabled()
        updateCoordinates()
        meshTransport.logEvent("STATE_REFRESH", "Radio hardware and backhaul state queried.")
    }

    private fun updateTriagePreview() {
        val cat = _selectedCategory.value
        val note = _userNote.value
        val people = _peopleCount.value
        _triagePreview.value = triageService.triageOffline(cat, note, people)
        _offlineGemmaReport.value = triageService.analyzeSymptomsDetailed(note, cat, people)
    }

    fun runCustomGemmaTriage(symptoms: String) {
        _customSymptomInput.value = symptoms
        if (symptoms.isBlank()) {
            _customGemmaReport.value = null
            return
        }
        val report = triageService.analyzeSymptomsDetailed(symptoms)
        _customGemmaReport.value = report
    }

    fun sendTriageChatMessage(symptomText: String) {
        val trimmed = symptomText.trim()
        if (trimmed.isBlank()) return

        val userMessage = TriageChatMessage(
            isUser = true,
            text = trimmed
        )
        _triageChatMessages.value = _triageChatMessages.value + userMessage
        _customSymptomInput.value = trimmed
        _isTriageAiTyping.value = true

        viewModelScope.launch {
            try {
                val aiResponse = triageService.processChatInputAsync(trimmed, _triageChatMessages.value)
                _triageChatMessages.value = _triageChatMessages.value + aiResponse
                if (aiResponse.triageReport != null) {
                    _customGemmaReport.value = aiResponse.triageReport
                }
            } finally {
                _isTriageAiTyping.value = false
            }
        }
    }

    fun installOfflineGemmaModel(uri: android.net.Uri) {
        viewModelScope.launch {
            val result = runCatching {
                val fileName = getApplication<android.app.Application>().contentResolver.query(
                    uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null
                )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                    ?: uri.lastPathSegment.orEmpty()
                getApplication<android.app.Application>().contentResolver.openInputStream(uri)
                    ?.let { triageService.installOfflineGemmaModel(fileName, it) }
                    ?: error("Could not open the selected model file.")
            }
            result.exceptionOrNull()?.let { error ->
                _triageChatMessages.value += TriageChatMessage(
                    isUser = false,
                    text = "Offline model installation failed: ${error.message ?: "invalid model file"}"
                )
            }
        }
    }

    fun clearTriageChat() {
        _triageChatMessages.value = listOf(initialWelcomeMessage)
        _customSymptomInput.value = ""
        _customGemmaReport.value = null
    }

    fun applyTriageToSos(report: OfflineGemmaTriageReport) {
        _userNote.value = "${report.conditionName}: ${report.actionSteps.firstOrNull()?.title ?: ""}".take(140)
        _selectedCategory.value = when (report.priority) {
            AlertPriority.P1 -> EmergencyCategory.MEDICAL
            AlertPriority.P2 -> EmergencyCategory.TRAPPED
            else -> EmergencyCategory.MEDICAL
        }
        updateTriagePreview()
        selectTab(AppTab.CITIZEN_SOS)
    }

    fun startSosCountdown() {
        _countdownSeconds.value = 5
        _isCountdownActive.value = true
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _countdownSeconds.value = i
                delay(1000)
            }
            _isCountdownActive.value = false
            dispatchSosAlert()
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        _isCountdownActive.value = false
        _countdownSeconds.value = 5
    }

    private fun dispatchSosAlert() {
        viewModelScope.launch {
            routingEngine.startTransport()
            val currentPrio = _triagePreview.value?.priority ?: AlertPriority.P1
            val coords = app.locationHelper.getCurrentCoordinates()
            val baseMessage = MeshMessage(
                type = MessageType.SOS,
                category = _selectedCategory.value,
                priority = currentPrio,
                senderId = app.cryptoSigner.getDeviceId(),
                latitude = coords?.latitude,
                longitude = coords?.longitude,
                accuracyM = coords?.accuracyM,
                peopleCount = _peopleCount.value,
                note = _userNote.value.ifBlank { "${_selectedCategory.value.displayName} - ${_peopleCount.value} people" },
                ttl = if (currentPrio == AlertPriority.P1) 12 else 8
            )

            val originated = routingEngine.originateAlert(baseMessage)

            // Start Open Perimeter Wi-Fi Emergency Beacon (visible to surrounding devices scanning Wi-Fi)
            wifiEmergencyBeaconManager.startEmergencyBeacon(coords?.latitude, coords?.longitude, _selectedCategory.value.name)

            // Auto-dispatch SOS SMS with Live GPS & Nearest Hospital / Police / Relief
            launch {
                val contacts = emergencyContactDao.getAllContacts()
                val hosp = emergencyCentersRepository.getNearestHospital(coords?.latitude, coords?.longitude)
                val police = emergencyCentersRepository.getNearestPoliceStation(coords?.latitude, coords?.longitude)
                val relief = emergencyCentersRepository.getNearestReliefCamp(coords?.latitude, coords?.longitude)

                val distressMsg = smsDispatcher.composeDistressMessage(
                    category = _selectedCategory.value,
                    peopleCount = _peopleCount.value,
                    note = _userNote.value.ifBlank { "${_selectedCategory.value.displayName} - ${_peopleCount.value} people" },
                    coordinates = coords,
                    nearestHospital = hosp,
                    nearestPolice = police,
                    nearestRelief = relief
                )

                val smsResult = smsDispatcher.sendDirectSms(contacts, distressMsg)
                _lastSmsResult.value = smsResult
                meshTransport.logEvent(
                    "SOS_SMS_BROADCAST",
                    "SMS submission result: ${smsResult.sentCount} submitted to Android, ${smsResult.failedCount} failed"
                )
            }

            launch {
                _isTriageLoading.value = true
                val aiResult = triageService.triageWithAiAssist(
                    _selectedCategory.value,
                    _userNote.value,
                    _peopleCount.value
                )
                _triagePreview.value = aiResult
                _isTriageLoading.value = false

                if (aiResult.priority.level < originated.priority.level) {
                    val updated = originated.copy(
                        priority = aiResult.priority,
                        note = aiResult.shortNote
                    )
                    alertDao.updateMessage(MeshMessageEntity.fromDomain(updated, isLocalOrigin = true))
                }
            }
        }
    }

    fun acknowledgeAlert(msgId: String) {
        viewModelScope.launch {
            val responderLabel = "Responder (${app.cryptoSigner.getDeviceId()})"
            alertDao.markAcknowledged(msgId, responderLabel)
            meshTransport.logEvent("ACK_SENT", "Disaster alert $msgId acknowledged by $responderLabel")
        }
    }

    // Emergency Contacts Management
    fun addEmergencyContact(name: String, phoneNumber: String, relationship: String = "Family") {
        viewModelScope.launch {
            if (name.isNotBlank() && phoneNumber.isNotBlank()) {
                emergencyContactDao.insertContact(
                    EmergencyContactEntity(
                        name = name.trim(),
                        phoneNumber = phoneNumber.trim(),
                        relationship = relationship.trim()
                    )
                )
                meshTransport.logEvent("CONTACT_SAVED", "Saved emergency contact")
            }
        }
    }

    fun deleteEmergencyContact(contactId: Long) {
        viewModelScope.launch {
            emergencyContactDao.deleteContactById(contactId)
            meshTransport.logEvent("CONTACT_REMOVED", "Removed contact ID: $contactId")
        }
    }

    fun sendTestSms() {
        viewModelScope.launch {
            val contacts = emergencyContactDao.getAllContacts()
            val testMessage = "Guardian setup test: this is not an emergency. If you received this, SMS setup is working."
            val result = smsDispatcher.sendDirectSms(contacts, testMessage)
            _lastSmsResult.value = result
            meshTransport.logEvent(
                "SMS_TEST",
                "Test SMS submission result: ${result.sentCount} submitted to Android, ${result.failedCount} failed"
            )
        }
    }

    // Beacon controls (Search & Rescue audio siren and optical Morse strobe)
    fun toggleTorchStrobe() {
        beaconController.toggleTorchStrobe()
    }

    fun toggleSiren() {
        beaconController.toggleSiren()
    }

    fun stopAllBeacons() {
        beaconController.stopAll()
    }

    // Compass controls
    fun startCompass() {
        compassManager.startListening()
    }

    fun stopCompass() {
        compassManager.stopListening()
    }

    // --- Search & Rescue Beacons ---
    fun toggleMorseBeacon() {
        if (morseSoundBeacon.isPlaying.value) {
            morseSoundBeacon.stop()
            meshTransport.logEvent("AUDIO_BEACON", "Synthesized Morse SOS audio beacon STOPPED")
        } else {
            morseSoundBeacon.start()
            meshTransport.logEvent("AUDIO_BEACON", "Synthesized Morse SOS audio beacon (900 Hz tone) STARTED")
        }
    }

    fun toggleWhistleDetection() {
        if (whistleDetector.isListening.value || whistleDetector.isStarting.value) {
            whistleDetector.stopListening()
            meshTransport.logEvent("ACOUSTIC_WHISTLE", "Acoustic disaster whistle detector STOPPED")
        } else {
            whistleDetector.startListening()
            meshTransport.logEvent("ACOUSTIC_WHISTLE", "Whistle detector start requested")
        }
    }

    fun stopWhistleDetection() {
        if (whistleDetector.isListening.value || whistleDetector.isStarting.value) {
            whistleDetector.stopListening()
            meshTransport.logEvent("ACOUSTIC_WHISTLE", "Whistle detector stopped when leaving the SOS screen")
        }
    }

    // --- Off-Grid Walkie-Talkie ---
    fun startVoiceRecording() {
        walkieTalkieManager.startRecording()
        meshTransport.logEvent(
            "WALKIE_TALKIE",
            if (walkieTalkieManager.isRecording.value) "Recording local voice memo" else "Voice recording did not start; check microphone permission and device availability"
        )
    }

    fun stopVoiceRecording() {
        walkieTalkieManager.stopRecording(deviceId) { memo ->
            meshTransport.logEvent(
                "WALKIE_TALKIE",
                "Voice memo saved locally (${memo.durationSeconds}s). Voice memos are not shared over the SOS mesh."
            )
        }
    }

    fun playVoiceMemo(memo: VoiceMemoItem) {
        walkieTalkieManager.playMemo(memo)
    }

    fun stopVoicePlaying() {
        walkieTalkieManager.stopPlaying()
    }

    // --- Family Circle ---
    fun addFamilyMember(
        name: String,
        relation: String = "Family",
        deviceId: String = "node-${System.currentTimeMillis() % 10000}",
        phone: String = ""
    ) {
        viewModelScope.launch {
            meshDatabaseRepository.addFamilyMember(
                FamilyMemberEntity(
                    id = java.util.UUID.randomUUID().toString().take(8),
                    name = name.trim(),
                    relationship = relation.trim(),
                    deviceId = deviceId.trim(),
                    phoneNumber = phone.trim(),
                    lastSeenTimestamp = System.currentTimeMillis(),
                    isNearby = false,
                    distanceMeters = 0f
                )
            )
            meshTransport.logEvent("FAMILY_CIRCLE", "Added family member: $name ($relation)")
        }
    }

    fun removeFamilyMember(id: String) {
        viewModelScope.launch {
            meshDatabaseRepository.removeFamilyMember(id)
            meshTransport.logEvent("FAMILY_CIRCLE", "Removed family member ID: $id")
        }
    }

    // --- Decentralized Supply Exchange ---
    fun addSupplyItem(supply: SupplyExchangeEntity) {
        viewModelScope.launch {
            meshDatabaseRepository.addSupplyItem(supply)
            val action = if (supply.type == "OFFER") "Offered" else "Requested"
            meshTransport.logEvent("MUTUAL_AID", "$action supply item: ${supply.title} (${supply.category})")
        }
    }

    fun resolveSupplyItem(id: String) {
        viewModelScope.launch {
            meshDatabaseRepository.resolveSupplyItem(id)
            meshTransport.logEvent("MUTUAL_AID", "Resolved mutual aid supply item ID: $id")
        }
    }

    // --- HUD Overlays ---
    fun setCompassHudVisible(visible: Boolean) {
        _showCompassHud.value = visible
        if (visible) {
            compassManager.startListening()
        }
    }

    fun setFloodlightVisible(visible: Boolean) {
        _showFloodlight.value = visible
    }

    // --- Medical Profiles ---
    fun setBleedDialogVisible(visible: Boolean) {
        _showBleedDialog.value = visible
    }

    fun saveMedicalProfile(profile: com.example.data.local.MedicalProfileEntity) {
        viewModelScope.launch {
            meshDatabaseRepository.saveMedicalProfile(profile)
            meshTransport.logEvent("MEDICAL_ID", "Saved emergency medical profile")
        }
    }

    fun toggleBackgroundRelay() {
        val willActivate = !_isBackgroundRelayActive.value
        _relayStatusError.value = null
        viewModelScope.launch {
            if (willActivate) {
                runCatching {
                    val started = routingEngine.startTransport()
                    check(started) { "Nearby permissions or radio are unavailable" }
                    com.example.domain.service.GuardianMeshRelayService.startService(app)
                    _isBackgroundRelayActive.value = true
                    meshTransport.logEvent("NEARBY_RELAY", "Nearby discovery is active. Alerts transfer only when a Guardian peer connects.")
                }.onFailure {
                    _isBackgroundRelayActive.value = false
                    _relayStatusError.value = "Turn on Bluetooth and allow Nearby permissions, then retry."
                    com.example.domain.service.GuardianMeshRelayService.stopService(app)
                    meshTransport.logEvent("NEARBY_RELAY_FAILED", "Nearby discovery could not start; check Bluetooth and Nearby permissions.")
                }
            } else {
                _isBackgroundRelayActive.value = false
                _relayStatusError.value = null
                routingEngine.stopTransport()
                com.example.domain.service.GuardianMeshRelayService.stopService(app)
                meshTransport.logEvent("NEARBY_RELAY_STOPPED", "Nearby discovery stopped.")
            }
        }
    }

    fun stopWifiEmergencyBeacon() {
        wifiEmergencyBeaconManager.stopEmergencyBeacon()
    }

    fun toggleWifiEmergencyBeacon() {
        val coords = _currentCoordinates.value
        wifiEmergencyBeaconManager.toggleEmergencyBeacon(coords?.latitude, coords?.longitude)
        meshTransport.logEvent("WIFI_HOTSPOT", "Emergency Wi-Fi hotspot beacon: ${wifiEmergencyBeaconManager.activeSsid.value ?: "OFF"}")
    }

    override fun onCleared() {
        super.onCleared()
    }
}
