package com.example

import android.app.Application
import com.example.data.crypto.CryptoSigner
import com.example.data.local.AppDatabase
import com.example.data.location.LocationHelper
import com.example.domain.ai.GemmaTriageService
import com.example.domain.mesh.RoutingEngine
import com.example.domain.mesh.RealMeshTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.mapsforge.map.android.graphics.AndroidGraphicFactory

class SosMeshApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: AppDatabase
        private set
    lateinit var cryptoSigner: CryptoSigner
        private set
    lateinit var locationHelper: LocationHelper
        private set
    lateinit var meshTransport: RealMeshTransport
        private set
    lateinit var routingEngine: RoutingEngine
        private set
    lateinit var triageService: GemmaTriageService
        private set
    lateinit var emergencyCentersRepository: com.example.data.location.EmergencyCentersRepository
        private set
    lateinit var smsDispatcher: com.example.data.location.SmsDispatcher
        private set
    lateinit var beaconController: com.example.domain.beacon.BeaconController
        private set
    lateinit var morseSoundBeacon: com.example.domain.beacon.MorseSoundBeacon
        private set
    lateinit var walkieTalkieManager: com.example.domain.mesh.WalkieTalkieManager
        private set
    lateinit var compassManager: com.example.data.location.CompassManager
        private set
    lateinit var meshDatabaseRepository: com.example.data.local.MeshDatabaseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidGraphicFactory.createInstance(this)

        database = AppDatabase.getDatabase(this)
        cryptoSigner = CryptoSigner()
        locationHelper = LocationHelper(this)
        meshTransport = RealMeshTransport(this, applicationScope)
        routingEngine = RoutingEngine(database.alertDao(), cryptoSigner, meshTransport, applicationScope)
        triageService = GemmaTriageService(com.example.domain.ai.OfflineGemmaChatModel(this))
        applicationScope.launch { triageService.prepareBundledGemmaModel() }
        emergencyCentersRepository = com.example.data.location.EmergencyCentersRepository()
        smsDispatcher = com.example.data.location.SmsDispatcher(this)
        beaconController = com.example.domain.beacon.BeaconController(this)
        morseSoundBeacon = com.example.domain.beacon.MorseSoundBeacon(applicationScope)
        walkieTalkieManager = com.example.domain.mesh.WalkieTalkieManager(this, applicationScope)
        compassManager = com.example.data.location.CompassManager(this)
        meshDatabaseRepository = com.example.data.local.MeshDatabaseRepository(
            database.alertDao(),
            database.meshNodeDao(),
            database.emergencyCenterDao(),
            database.familyMemberDao(),
            database.supplyExchangeDao(),
            database.medicalProfileDao()
        )
    }

    companion object {
        lateinit var instance: SosMeshApp
            private set
    }
}
