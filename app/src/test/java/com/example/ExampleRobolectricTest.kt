package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AlertPriority
import com.example.data.model.EmergencyCategory
import com.example.domain.ai.GemmaTriageService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Guardian app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Guardian", appName)
    }

    @Test
    fun `triage heuristic correctly flags critical child in flood as P1`() {
        val triageService = GemmaTriageService()
        val result = triageService.triageOffline(
            category = EmergencyCategory.FLOOD,
            rawNote = "Paani ghar me aa gaya hai, 2 bachche hain, madad chahiye jaldi",
            peopleCount = 4
        )

        assertEquals(AlertPriority.P1, result.priority)
        assertTrue(result.flags.contains("CHILDREN"))
        assertTrue(result.flags.contains("FLOOD_RISING"))
        assertEquals("hi", result.detectedLanguage)
    }

    @Test
    fun `triage heuristic sets safe checkin to P4 priority`() {
        val triageService = GemmaTriageService()
        val result = triageService.triageOffline(
            category = EmergencyCategory.SAFE,
            rawNote = "We reached high ground. Safe.",
            peopleCount = 2
        )

        assertEquals(AlertPriority.P4, result.priority)
    }

    @Test
    fun `smsDispatcher composes distress message with live location and omits unverified facilities`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dispatcher = com.example.data.location.SmsDispatcher(context)
        val centersRepo = com.example.data.location.EmergencyCentersRepository()

        val mockCoords = com.example.data.location.DeviceCoordinates(28.6139, 77.2090, 8.5f)
        val hospital = centersRepo.getNearestHospital(mockCoords.latitude, mockCoords.longitude)
        val police = centersRepo.getNearestPoliceStation(mockCoords.latitude, mockCoords.longitude)
        val relief = centersRepo.getNearestReliefCamp(mockCoords.latitude, mockCoords.longitude)

        val msg = dispatcher.composeDistressMessage(
            category = EmergencyCategory.FLOOD,
            peopleCount = 3,
            note = "Water rising fast on 2nd floor",
            coordinates = mockCoords,
            nearestHospital = hospital,
            nearestPolice = police,
            nearestRelief = relief
        )

        assertTrue(msg.startsWith("GUARDIAN SOS:"))
        assertTrue(msg.contains(EmergencyCategory.FLOOD.displayName))
        assertTrue(msg.contains("3p"))
        assertTrue(msg.contains("https://maps.google.com/?q=28.61390,77.20900"))
        assertTrue(msg.contains("call 112"))
        assertTrue(msg.length <= 160)
        assertFalse(msg.contains("🚨"))
        assertFalse(msg.contains("Nearest Hospital"))
        assertFalse(msg.contains("Nearest Police"))
    }

    @Test
    fun `distress SMS keeps location and emergency number when long notes are truncated`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dispatcher = com.example.data.location.SmsDispatcher(context)
        val coordinates = com.example.data.location.DeviceCoordinates(20.1714, 85.7110, 8.5f)

        val message = dispatcher.composeDistressMessage(
            category = EmergencyCategory.MEDICAL,
            peopleCount = 1,
            note = "TEST ONLY " + "details ".repeat(80),
            coordinates = coordinates,
            nearestHospital = null,
            nearestPolice = null,
            nearestRelief = null
        )

        assertTrue(message.length <= 160)
        assertTrue(message.contains("https://maps.google.com/?q=20.17140,85.71100"))
        assertTrue(message.contains("call 112"))
        assertTrue(message.contains("TEST ONLY"))
    }

    @Test
    fun `offline survival protocols contains CPR and severe bleeding life support`() {
        val guides = com.example.data.model.OfflineSurvivalProtocols.guides
        assertTrue(guides.isNotEmpty())
        assertTrue(guides.any { it.id == "cpr" && it.urgencyLevel == "CRITICAL" })
        assertTrue(guides.any { it.id == "bleeding" && it.steps.isNotEmpty() })
        assertTrue(guides.any { it.id == "flood" })
    }

    @Test
    fun `emergency contacts stored and retrieved in Room database`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val dao = db.emergencyContactDao()
        val contact1 = com.example.data.local.EmergencyContactEntity(
            name = "Mom",
            phoneNumber = "+1-555-0199",
            relationship = "Parent",
            isPrimary = true
        )
        val id = dao.insertContact(contact1)
        assertTrue(id > 0)

        val contacts = dao.getAllContacts()
        assertEquals(1, contacts.size)
        assertEquals("Mom", contacts[0].name)
        assertEquals("+1-555-0199", contacts[0].phoneNumber)

        dao.deleteContactById(id)
        val emptyContacts = dao.getAllContacts()
        assertTrue(emptyContacts.isEmpty())
        db.close()
    }

    @Test
    fun `emergency centers repository omits facilities until verified location data is configured`() {
        val repo = com.example.data.location.EmergencyCentersRepository()
        val userLat = 28.6139
        val userLng = 77.2090

        val centers = repo.getEmergencyCenters(userLat, userLng)
        assertTrue(centers.isEmpty())
        assertEquals(null, repo.getNearestHospital(userLat, userLng))
        assertEquals(null, repo.getNearestPoliceStation(userLat, userLng))
        assertEquals(null, repo.getNearestReliefCamp(userLat, userLng))

        val hospitalSearch = repo.createNearbyHospitalsMapIntent(userLat, userLng)
        assertTrue(hospitalSearch.data.toString().contains("geo:$userLat,$userLng"))
    }

    @Test
    fun `offline Gemma AI accurately diagnoses severe arterial bleeding, assigns P1, and flags tourniquet requirement`() {
        val gemmaEngine = com.example.domain.ai.OfflineGemmaEngine()
        val report = gemmaEngine.analyzeSymptoms(
            symptomsText = "Spurting bright red blood from deep calf wound, pale and dizzy",
            category = EmergencyCategory.MEDICAL,
            peopleCount = 1
        )

        assertEquals(AlertPriority.P1, report.priority)
        assertTrue(report.requiresTourniquet)
        assertTrue(report.detectedSymptomTags.contains("ARTERIAL_BLEED"))
        assertTrue(report.actionSteps.isNotEmpty())
        assertTrue(report.actionSteps.any { it.isCritical })
        assertTrue(report.conditionNameHindi.isNotEmpty())
    }

    @Test
    fun `offline Gemma AI accurately diagnoses cardiac arrest, assigns P1, and activates CPR rhythm`() {
        val gemmaEngine = com.example.domain.ai.OfflineGemmaEngine()
        val report = gemmaEngine.analyzeSymptoms(
            symptomsText = "Victim collapsed, unconscious, not breathing, no pulse after electric shock",
            category = EmergencyCategory.MEDICAL,
            peopleCount = 1
        )

        assertEquals(AlertPriority.P1, report.priority)
        assertTrue(report.requiresCprRhythm)
        assertTrue(report.detectedSymptomTags.contains("CARDIAC_ARREST"))
        assertTrue(report.actionSteps.any { it.title.contains("CPR", ignoreCase = true) || it.instruction.contains("CPR", ignoreCase = true) })
    }

    @Test
    fun `offline Gemma AI diagnoses smoke inhalation and airway distress during fire disaster`() {
        val gemmaEngine = com.example.domain.ai.OfflineGemmaEngine()
        val report = gemmaEngine.analyzeSymptoms(
            symptomsText = "Severe smoke inhalation from building fire, coughing soot, stridor wheezing child",
            category = EmergencyCategory.FIRE,
            peopleCount = 2
        )

        assertEquals(AlertPriority.P1, report.priority)
        assertTrue(report.detectedSymptomTags.contains("SMOKE_INHALATION"))
        assertTrue(report.detectedSymptomTags.contains("CHILD_AIRWAY"))
        assertTrue(report.clinicalRationale.isNotEmpty())
        assertTrue(report.triageCode.contains("RED"))
    }

    @Test
    fun `meshNodeDao persists and updates peer nodes and telemetry in Room database`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val dao = db.meshNodeDao()

        val node1 = com.example.data.local.MeshNodeStatusEntity(
            nodeId = "NODE_DELHI_RELAY_01",
            name = "Relay Beacon Beta",
            rssi = -68,
            isConnected = true,
            lastSeenTimestamp = System.currentTimeMillis(),
            latitude = 28.6145,
            longitude = 77.2105,
            batteryPct = 92,
            nodeRole = "ROUTER",
            hopDistance = 1,
            isGateway = false,
            isRelayingAlert = true
        )

        dao.insertOrUpdateNode(node1)

        val cached = dao.getAllCachedNodes()
        assertEquals(1, cached.size)
        assertEquals("NODE_DELHI_RELAY_01", cached[0].nodeId)
        assertEquals(92, cached[0].batteryPct)
        assertTrue(cached[0].isConnected)
        assertTrue(cached[0].isRelayingAlert)

        // Update telemetry
        val newTime = System.currentTimeMillis() + 1000
        dao.updateTelemetry("NODE_DELHI_RELAY_01", -55, 88, newTime)
        val updated = dao.getAllCachedNodes()
        assertEquals(-55, updated[0].rssi)
        assertEquals(88, updated[0].batteryPct)

        // Update connectivity
        dao.updateConnectivity("NODE_DELHI_RELAY_01", false)
        val disconnected = dao.getAllCachedNodes()
        assertEquals(false, disconnected[0].isConnected)

        db.close()
    }

    @Test
    fun `meshDatabaseRepository caches distress signals and peer nodes for complete offline functionality`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = com.example.data.local.MeshDatabaseRepository(
            alertDao = db.alertDao(),
            meshNodeDao = db.meshNodeDao(),
            emergencyCenterDao = db.emergencyCenterDao()
        )

        // 1. Cache a distress signal
        val distressSignal = com.example.data.model.MeshMessage(
            msgId = "SOS_MSG_TEST_99",
            type = com.example.data.model.MessageType.SOS,
            category = EmergencyCategory.TRAPPED,
            priority = AlertPriority.P1,
            senderId = "CITIZEN_DEVICE_01",
            latitude = 28.6139,
            longitude = 77.2090,
            accuracyM = 5.0f,
            peopleCount = 2,
            note = "Trapped under collapsed beam",
            ttl = 8
        )
        repository.cacheDistressSignal(distressSignal, isLocalOrigin = true)

        // 2. Cache a Safe signal
        val safeSignal = com.example.data.model.MeshMessage(
            msgId = "SAFE_MSG_TEST_01",
            type = com.example.data.model.MessageType.SAFE,
            category = EmergencyCategory.SAFE,
            priority = AlertPriority.P4,
            senderId = "CITIZEN_DEVICE_02",
            peopleCount = 1,
            note = "Reached open ground safe",
            ttl = 4
        )
        repository.cacheDistressSignal(safeSignal, isLocalOrigin = false)

        // 3. Verify repository retrieves cached distress signals
        val allMessages = repository.getAllCachedMessages()
        assertEquals(2, allMessages.size)

        // 4. Cache mesh nodes explicitly via repository
        val peerNodes = listOf(
            com.example.domain.mesh.MeshPeerNode(
                id = "PEER_POLICE_MOBILE",
                name = "Police Rapid Relay",
                rssi = -64,
                isConnected = true,
                latitude = 28.6150,
                longitude = 77.2080,
                batteryPct = 95,
                nodeRole = "GATEWAY",
                hopDistance = 1,
                isGateway = true
            )
        )
        repository.cacheMeshNodes(peerNodes)

        val cachedNodes = repository.getAllCachedNodes()
        assertEquals(1, cachedNodes.size)
        assertEquals("PEER_POLICE_MOBILE", cachedNodes[0].id)
        assertTrue(cachedNodes[0].isGateway)

        db.close()
    }

    @Test
    fun `surrounding mesh nodes returns genuine discovered nodes and rejects simulated dummy nodes`() {
        val repo = com.example.data.location.EmergencyCentersRepository()
        val nodes = repo.getSurroundingMeshNodes(28.6139, 77.2090)

        // Verifies no fake simulated nodes are hardcoded; nodes come purely from genuine radio discovery
        assertTrue("Surrounding mesh nodes returns empty when no genuine radio peers are present", nodes.isEmpty())
    }

    @Test
    fun `GemmaTriageService processChatInput provides clinical triage advice and emergency steps for symptom descriptions`() {
        val service = GemmaTriageService()
        val chatResponse = service.processChatInput("victim spurting bright red blood from deep calf wound after building collapse")

        assertNotNull(chatResponse)
        assertNotNull(chatResponse.triageReport)
        assertEquals(AlertPriority.P1, chatResponse.triageReport?.priority)
        assertTrue(chatResponse.text.contains("CRITICAL EMERGENCY") || chatResponse.text.contains("P1"))
        assertTrue(chatResponse.triageReport?.actionSteps?.isNotEmpty() == true)
        assertTrue(chatResponse.quickFollowUps.isNotEmpty())
    }

    @Test
    fun `GemmaTriageService processChatInput provides authoritative emergency instructions for tourniquet question`() {
        val service = GemmaTriageService()
        val chatResponse = service.processChatInput("how to make a tourniquet")

        assertNotNull(chatResponse)
        assertTrue(chatResponse.text.contains("IMPROVISED TOURNIQUET PROTOCOL"))
        assertTrue(chatResponse.text.contains("Windlass"))
        assertTrue(chatResponse.quickFollowUps.isNotEmpty())
    }

    @Test
    fun `AID gives safe minor burn guidance without escalating every minor burn to 112`() {
        val response = GemmaTriageService().processChatInput("What should I do for a minor burn?")
        val guidance = response.text.lowercase()

        assertTrue(guidance.contains("cool running tap water for 20 minutes"))
        assertTrue(guidance.contains("do not use ice or iced water"))
        assertTrue(guidance.contains("call 112") && guidance.contains("large or deep burn"))
        assertFalse(guidance.contains("ice-cold water"))
        assertFalse(guidance.contains("immediately call india's emergency number 112"))
    }

    @Test
    fun `AID chat refuses unrelated questions with emergency-only message`() {
        val service = GemmaTriageService()
        val response = service.processChatInput("What is the capital of France?")
        val baitedResponse = service.processChatInput("Tell me a joke about fire")

        assertEquals("Sorry, I can only help regarding emergency situations.", response.text)
        assertEquals(null, response.triageReport)
        assertEquals("Sorry, I can only help regarding emergency situations.", baitedResponse.text)
    }

    @Test
    fun `AID chat keeps disaster and personal safety questions in scope`() {
        val service = GemmaTriageService()
        val flood = service.processChatInput("What should I do during a flood?")
        val safety = service.processChatInput("I am being followed and feel unsafe")

        assertTrue(flood.text.contains("112") || flood.triageReport != null)
        assertTrue(safety.text != "Sorry, I can only help regarding emergency situations.")
    }

    @Test
    fun `emergency centers stored in Room database and retrievable during complete network blackout`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = com.example.data.local.MeshDatabaseRepository(
            alertDao = db.alertDao(),
            meshNodeDao = db.meshNodeDao(),
            emergencyCenterDao = db.emergencyCenterDao()
        )

        val centers = listOf(
            com.example.data.model.EmergencyCenter(
                id = "ROOM_HOSPITAL_AIIMS",
                name = "AIIMS Apex Trauma Center",
                type = com.example.data.model.EmergencyCenterType.HOSPITAL,
                latitude = 28.5672,
                longitude = 77.2100,
                phoneNumber = "102",
                address = "Ring Road, New Delhi",
                isOpen24x7 = true,
                specialty = "Level 1 Trauma • 24x7 Emergency",
                statusText = "Open 24x7 • Active ICU",
                operationalDetail = "Oxygen supply active, trauma bay ready",
                statusBadge = "CRITICAL READY"
            ),
            com.example.data.model.EmergencyCenter(
                id = "ROOM_POLICE_HQ",
                name = "Central Police Post",
                type = com.example.data.model.EmergencyCenterType.POLICE_STATION,
                latitude = 28.6145,
                longitude = 77.2105,
                phoneNumber = "112",
                address = "Connaught Place, New Delhi",
                isOpen24x7 = true,
                specialty = "Law Enforcement & Disaster Rescue",
                statusText = "Active Patrol • Mesh Base",
                operationalDetail = "Disaster rescue detachment standing by",
                statusBadge = "PATROL ACTIVE"
            )
        )

        // Seed initial centers
        repository.seedInitialCentersIfEmpty(centers)

        // Retrieve cached centers from Room SQLite
        val cached = repository.getAllCachedCenters()
        assertEquals(2, cached.size)
        assertTrue(cached.any { it.id == "ROOM_HOSPITAL_AIIMS" && it.type == com.example.data.model.EmergencyCenterType.HOSPITAL })
        assertTrue(cached.any { it.id == "ROOM_POLICE_HQ" && it.type == com.example.data.model.EmergencyCenterType.POLICE_STATION })

        db.close()
    }

    @Test
    fun `quick SOS distress signal broadcast originates P1 alert and caches into Room database`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val crypto = com.example.data.crypto.CryptoSigner()
        val transport = com.example.domain.mesh.RealMeshTransport(context, this)
        val routing = com.example.domain.mesh.RoutingEngine(db.alertDao(), crypto, transport, this)

        val repository = com.example.data.local.MeshDatabaseRepository(
            alertDao = db.alertDao(),
            meshNodeDao = db.meshNodeDao(),
            emergencyCenterDao = db.emergencyCenterDao()
        )

        val quickSosMessage = com.example.data.model.MeshMessage(
            type = com.example.data.model.MessageType.SOS,
            category = EmergencyCategory.TRAPPED,
            priority = AlertPriority.P1,
            senderId = crypto.getDeviceId(),
            latitude = 28.6139,
            longitude = 77.2090,
            accuracyM = 4.2f,
            peopleCount = 3,
            note = "CRITICAL SOS: High-contrast FAB triggered distress broadcast to local mesh nodes",
            ttl = 10,
            hopCount = 0
        )

        // Originate alert
        val originated = routing.originateAlert(quickSosMessage)
        assertEquals(AlertPriority.P1, originated.priority)
        assertEquals(com.example.data.model.MessageType.SOS, originated.type)
        assertNotNull(originated.signature)
        assertTrue("Originating public key should verify its signature", crypto.verifySignature(originated))
        assertTrue("Sender identity tampering must invalidate the signature", crypto.verifySignature(originated.copy(note = "tampered" )).not())

        // Cache in Room
        repository.cacheDistressSignal(originated, isLocalOrigin = true)

        val cachedSignals = repository.getAllCachedMessages()
        assertEquals(1, cachedSignals.size)
        assertEquals(originated.msgId, cachedSignals[0].msgId)
        assertEquals(AlertPriority.P1, cachedSignals[0].priority)

        // Verify broadcast log recorded in transmission logs
        val logs = transport.transmissionLogs.value
        assertTrue("Local-only status should be recorded when no peer is connected", logs.any { it.eventType == "LOCAL_ONLY" })

        db.close()
    }

    @Test
    fun `family member entity stored and proximity query works in Room database`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val dao = db.familyMemberDao()
        val member = com.example.data.local.FamilyMemberEntity(
            id = "FAM_TEST_1",
            name = "Aarav (Brother)",
            relationship = "Brother",
            deviceId = "NODE-AARAV-42",
            phoneNumber = "+91 99999 88888",
            lastSeenLatitude = 28.6139,
            lastSeenLongitude = 77.2090,
            lastSeenTimestamp = System.currentTimeMillis(),
            isNearby = true,
            distanceMeters = 150f
        )

        dao.insertFamilyMember(member)
        val all = dao.getAllFamilyMembers()
        assertEquals(1, all.size)
        assertEquals("Aarav (Brother)", all[0].name)
        assertTrue(all[0].isNearby)
        assertEquals(150f, all[0].distanceMeters)

        // Proximity update
        dao.updateProximity(
            deviceId = "NODE-AARAV-42",
            isNearby = true,
            distanceMeters = 85f,
            timestamp = System.currentTimeMillis(),
            lat = 28.6140,
            lng = 77.2095
        )

        val updated = dao.getAllFamilyMembers()
        assertEquals(85f, updated[0].distanceMeters)

        db.close()
    }

    @Test
    fun `supply exchange dao stores requests and marks resolved`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val dao = db.supplyExchangeDao()
        val supply = com.example.data.local.SupplyExchangeEntity(
            id = "SUPPLY_TEST_1",
            type = "REQUEST",
            category = "WATER",
            title = "Clean Drinking Water",
            description = "Need 10 liters clean water for elderly relatives",
            quantity = "10L",
            locationLabel = "Sector 4 Relief Point",
            contactInfo = "Near Community Hall",
            timestamp = System.currentTimeMillis(),
            isResolved = false
        )

        dao.insertSupply(supply)
        val active = dao.getAllSupplies()
        assertEquals(1, active.size)
        assertEquals("Clean Drinking Water", active[0].title)
        assertEquals(false, active[0].isResolved)

        // Mark resolved
        dao.markResolved("SUPPLY_TEST_1")
        val resolvedList = dao.getAllSupplies()
        assertTrue(resolvedList[0].isResolved)

        db.close()
    }

    @Test
    fun `city disaster packs contain regional packs with offline shelters`() {
        val packs = com.example.data.model.CityDisasterPacksData.availablePacks
        assertTrue(packs.size >= 4)
        assertTrue(packs.any { it.cityName.contains("Delhi") })
        assertTrue(packs.any { it.cityName.contains("Mumbai") })
        assertTrue(packs.any { it.id.contains("HIMALAYAN") || it.cityName.contains("Uttarakhand") })
        assertTrue(packs.any { it.cityName.contains("Chennai") })

        val delhi = packs.first { it.id == "PACK_DELHI_NCR" }
        assertTrue(delhi.shelterCount > 0)
        assertTrue(delhi.preparationChecklist.isNotEmpty())
    }

    @Test
    fun `medical profile stored and retrieved in Room database`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val dao = db.medicalProfileDao()
        val profile = com.example.data.local.MedicalProfileEntity(
            fullName = "Rohan Sharma",
            bloodGroup = "O+",
            age = 28,
            severeAllergies = "Penicillin, Peanuts",
            chronicConditions = "Asthma",
            regularMedications = "Albuterol Inhaler",
            emergencyContactName = "Ananya Sharma",
            emergencyContactPhone = "+91 98765 43210",
            emergencyContactRelation = "Spouse",
            isOrganDonor = true,
            medicalNotes = "Carries EpiPen in backpack"
        )

        dao.insertOrUpdateProfile(profile)
        val retrieved = dao.getMedicalProfile()
        assertNotNull(retrieved)
        assertEquals("Rohan Sharma", retrieved?.fullName)
        assertEquals("O+", retrieved?.bloodGroup)
        assertEquals(28, retrieved?.age)
        assertTrue(retrieved?.isOrganDonor == true)
        assertTrue(retrieved?.severeAllergies?.contains("Penicillin") == true)

        db.close()
    }

    @Test
    fun `offline QR generator creates medical payload and valid bitmap`() {
        val profile = com.example.data.local.MedicalProfileEntity(
            fullName = "Priya Patel",
            bloodGroup = "B+",
            age = 32,
            severeAllergies = "Sulfa Drugs",
            chronicConditions = "Type 1 Diabetes",
            regularMedications = "Insulin glargine",
            emergencyContactName = "Vikram Patel",
            emergencyContactPhone = "+91 91234 56789",
            emergencyContactRelation = "Brother",
            isOrganDonor = true
        )

        val payload = com.example.domain.medical.OfflineQrGenerator.createMedicalPayload(profile)
        assertTrue(payload.contains("EMERGENCY MEDICAL ID"))
        assertTrue(payload.contains("Priya Patel"))
        assertTrue(payload.contains("Blood: B+"))
        assertTrue(payload.contains("Sulfa Drugs"))
        assertTrue(payload.contains("Vikram Patel"))

        val qrBitmap = com.example.domain.medical.OfflineQrGenerator.generateEmergencyQrBitmap(payload, 256)
        assertNotNull(qrBitmap)
        assertTrue(qrBitmap.width > 0)
        assertTrue(qrBitmap.height > 0)
    }

    @Test
    fun `battery safety manager accurately calculates throttle states`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val batteryManager = com.example.domain.battery.BatterySafetyManager(context)

        val level = batteryManager.batteryLevel.value
        assertTrue("Battery percentage should be in valid range", level in 0..100)

        val throttleState = batteryManager.throttleState.value
        assertNotNull(throttleState)
        assertTrue(throttleState.displayName.isNotEmpty())
    }

    @Test
    fun `wifi emergency beacon manager uses Android controlled hotspot label and reports no false success`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val beaconManager = com.example.data.location.WifiEmergencyBeaconManager(context)

        val ssid = beaconManager.generateEmergencySsid(28.6139, 77.2090, "SOS")
        assertEquals("Emergency hotspot", ssid)

        beaconManager.startEmergencyBeacon(28.6139, 77.2090, "SOS")
        beaconManager.stopEmergencyBeacon()
        assertEquals(false, beaconManager.isBeaconActive.value)
        assertEquals(null, beaconManager.activeSsid.value)
    }
}

