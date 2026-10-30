package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.ui.AppTab
import com.example.ui.SosMeshViewModel
import com.example.ui.components.FirstLaunchPermissionsDialog
import com.example.ui.components.EmergencyTopBar
import com.example.ui.components.FloodlightDialog
import com.example.ui.components.TacticalCompassHudDialog
import com.example.ui.screens.CitizenSosScreen
import com.example.ui.screens.CityDisasterPacksScreen
import com.example.ui.screens.EmergencyContactsScreen
import com.example.ui.screens.FamilyCircleScreen
import com.example.ui.screens.MedicalProfileScreen
import com.example.ui.screens.MeshDiagnosticsScreen
import com.example.ui.screens.NearbyHelpRadarScreen
import com.example.ui.screens.ResponderScreen
import com.example.ui.screens.SupplyExchangeScreen
import com.example.ui.screens.SurvivalGuidesScreen
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val tileSosRequest = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tileSosRequest.value = intent?.getBooleanExtra(EXTRA_TRIGGER_SOS, false) == true
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val tileSosRequested by tileSosRequest.collectAsStateWithLifecycle()
                SosMeshAppRoot(
                    tileSosRequested = tileSosRequested,
                    onTileSosHandled = { tileSosRequest.value = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_TRIGGER_SOS, false)) {
            tileSosRequest.value = true
        }
    }

    companion object {
        const val EXTRA_TRIGGER_SOS = "EXTRA_TRIGGER_SOS"
    }
}

@Composable
fun SosMeshAppRoot(
    viewModel: SosMeshViewModel = viewModel(),
    tileSosRequested: Boolean = false,
    onTileSosHandled: () -> Unit = {}
) {
    FirstLaunchPermissionsDialog()
    LaunchedEffect(tileSosRequested) {
        if (tileSosRequested) {
            viewModel.startSosCountdown()
            onTileSosHandled()
        }
    }
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val connectedPeers by viewModel.connectedPeersCount.collectAsStateWithLifecycle()
    val isInternetAvailable by viewModel.isInternetAvailable.collectAsStateWithLifecycle()
    val emergencyContacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()
    val emergencyCenters by viewModel.emergencyCenters.collectAsStateWithLifecycle()
    val nearestHospital by viewModel.nearestHospital.collectAsStateWithLifecycle()
    val nearestPolice by viewModel.nearestPoliceStation.collectAsStateWithLifecycle()
    val nearestRelief by viewModel.nearestReliefCamp.collectAsStateWithLifecycle()
    val lastSmsResult by viewModel.lastSmsResult.collectAsStateWithLifecycle()
    val isTorchActive by viewModel.isTorchActive.collectAsStateWithLifecycle()
    val isSirenActive by viewModel.isSirenActive.collectAsStateWithLifecycle()
    val sirenError by viewModel.sirenError.collectAsStateWithLifecycle()
    val compassAzimuth by viewModel.compassAzimuth.collectAsStateWithLifecycle()
    val isCompassAvailable by viewModel.isCompassAvailable.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val supplyItems by viewModel.supplyItems.collectAsStateWithLifecycle()
    val isMorseBeaconActive by viewModel.isMorseBeaconActive.collectAsStateWithLifecycle()
    val showCompassHud by viewModel.showCompassHud.collectAsStateWithLifecycle()
    val showFloodlight by viewModel.showFloodlight.collectAsStateWithLifecycle()
    val isBackgroundRelayActive by viewModel.isBackgroundRelayActive.collectAsStateWithLifecycle()
    val relayStatusError by viewModel.relayStatusError.collectAsStateWithLifecycle()
    val isWifiBeaconActive by viewModel.isWifiBeaconActive.collectAsStateWithLifecycle()
    val wifiBeaconSsid by viewModel.wifiBeaconSsid.collectAsStateWithLifecycle()
    val medicalProfile by viewModel.medicalProfile.collectAsStateWithLifecycle()
    var showCityPacksDialog by remember { mutableStateOf(false) }

    // Handle Back Button navigation
    if (currentTab != AppTab.CITIZEN_SOS) {
        BackHandler {
            viewModel.selectTab(AppTab.CITIZEN_SOS)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                EmergencyTopBar(
                    connectedPeers = connectedPeers,
                    onOpenDiagnostics = { viewModel.selectTab(AppTab.DIAGNOSTICS) },
                    onOpenCityPacks = { showCityPacksDialog = true },
                    onOpenMedicalId = { viewModel.selectTab(AppTab.MEDICAL_ID) }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_navigation_bar"),
                    containerColor = DarkSurfaceHigh,
                    tonalElevation = 8.dp
                ) {
                    // Tab 1: Citizen SOS
                    val isSosSelected = currentTab == AppTab.CITIZEN_SOS
                    NavigationBarItem(
                        selected = isSosSelected,
                        onClick = { viewModel.selectTab(AppTab.CITIZEN_SOS) },
                        icon = {
                            Icon(
                                imageVector = if (isSosSelected) Icons.Filled.CrisisAlert else Icons.Outlined.CrisisAlert,
                                contentDescription = "Citizen SOS"
                            )
                        },
                        label = { Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = EmergencyRed,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_citizen_sos")
                    )

                    // Tab 2: Nearby Help Centers & Map
                    val isMapSelected = currentTab == AppTab.NEARBY_HELP
                    NavigationBarItem(
                        selected = isMapSelected,
                        onClick = { viewModel.selectTab(AppTab.NEARBY_HELP) },
                        icon = {
                            Icon(
                                imageVector = if (isMapSelected) Icons.Filled.Map else Icons.Outlined.Map,
                                contentDescription = "Help & Map"
                            )
                        },
                        label = { Text("Map", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = EmergencyRed,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_radar")
                    )

                    // Tab 3: Offline Survival Guides & First-Aid
                    val isGuidesSelected = currentTab == AppTab.SURVIVAL_GUIDE
                    NavigationBarItem(
                        selected = isGuidesSelected,
                        onClick = { viewModel.selectTab(AppTab.SURVIVAL_GUIDE) },
                        icon = {
                            Icon(
                                imageVector = if (isGuidesSelected) Icons.Filled.MedicalServices else Icons.Outlined.MedicalServices,
                                contentDescription = "First Aid"
                            )
                        },
                        label = { Text("Aid", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = EmergencyRed,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_guides")
                    )

                    // Tab 4: Safety Hub (Contacts, Medical ID, Family)
                    val isContactsSelected = currentTab == AppTab.CONTACTS || currentTab == AppTab.FAMILY || currentTab == AppTab.MEDICAL_ID
                    NavigationBarItem(
                        selected = isContactsSelected,
                        onClick = { viewModel.selectTab(AppTab.CONTACTS) },
                        icon = {
                            Icon(
                                imageVector = if (isContactsSelected) Icons.Filled.ContactPhone else Icons.Outlined.ContactPhone,
                                contentDescription = "Safety Hub"
                            )
                        },
                        label = { Text("Safety", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = EmergencyRed,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_safety_hub")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.CITIZEN_SOS -> {
                        val category by viewModel.selectedCategory.collectAsStateWithLifecycle()
                        val peopleCount by viewModel.peopleCount.collectAsStateWithLifecycle()
                        val userNote by viewModel.userNote.collectAsStateWithLifecycle()
                        val currentCoordinates by viewModel.currentCoordinates.collectAsStateWithLifecycle()
                        val triagePreview by viewModel.triagePreview.collectAsStateWithLifecycle()
                        val isTriageLoading by viewModel.isTriageLoading.collectAsStateWithLifecycle()
                        val isCountdownActive by viewModel.isCountdownActive.collectAsStateWithLifecycle()
                        val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()
                        val latestAlert by viewModel.latestLocalAlert.collectAsStateWithLifecycle()
                        val offlineGemmaReport by viewModel.offlineGemmaReport.collectAsStateWithLifecycle()

                        CitizenSosScreen(
                            selectedCategory = category,
                            peopleCount = peopleCount,
                            userNote = userNote,
                            triagePreview = triagePreview,
                            isTriageLoading = isTriageLoading,
                            isCountdownActive = isCountdownActive,
                            countdownSeconds = countdownSeconds,
                            latestAlert = latestAlert,
                            currentCoordinates = currentCoordinates,
                            connectedPeers = connectedPeers,
                            isSirenActive = isSirenActive,
                            sirenError = sirenError,
                            isTorchActive = isTorchActive,
                            savedContactsCount = emergencyContacts.size,
                            lastSmsResult = lastSmsResult,
                            nearestHospital = nearestHospital,
                            nearestPolice = nearestPolice,
                            offlineGemmaReport = offlineGemmaReport,
                            isMorseBeaconActive = isMorseBeaconActive,
                            onSelectCategory = viewModel::selectCategory,
                            onPeopleCountChange = viewModel::setPeopleCount,
                            onUserNoteChange = viewModel::setUserNote,
                            onTriggerSos = viewModel::startSosCountdown,
                            onCancelSos = viewModel::cancelSosCountdown,
                            onUpdateLocation = viewModel::updateCoordinates,
                            onToggleSiren = viewModel::toggleSiren,
                            onToggleTorch = viewModel::toggleTorchStrobe,
                            onToggleMorseBeacon = viewModel::toggleMorseBeacon,
                            onOpenFloodlight = { viewModel.setFloodlightVisible(true) },
                            onOpenCompassHud = { viewModel.setCompassHudVisible(true) },
                            onNavigateToFamily = { viewModel.selectTab(AppTab.FAMILY) },
                            onNavigateToSupplies = { viewModel.selectTab(AppTab.SUPPLIES) },
                            onNavigateToContacts = { viewModel.selectTab(AppTab.CONTACTS) },
                            onNavigateToMap = { viewModel.selectTab(AppTab.NEARBY_HELP) },
                            onOpenAiTriage = { viewModel.selectTab(AppTab.SURVIVAL_GUIDE) },
                            onNavigateToMedicalProfile = { viewModel.selectTab(AppTab.MEDICAL_ID) },
                            isBackgroundRelayActive = isBackgroundRelayActive,
                            relayStatusError = relayStatusError,
                            onToggleBackgroundRelay = viewModel::toggleBackgroundRelay,
                            isWifiBeaconActive = isWifiBeaconActive,
                            wifiBeaconSsid = wifiBeaconSsid,
                            onToggleWifiBeacon = viewModel::toggleWifiEmergencyBeacon
                        )
                    }

                    AppTab.FAMILY -> {
                        FamilyCircleScreen(
                            familyMembers = familyMembers,
                            onAddMember = { name, relation, devId, phone ->
                                viewModel.addFamilyMember(name, relation, devId, phone)
                            },
                            onRemoveMember = { id ->
                                viewModel.removeFamilyMember(id)
                            },
                            onTriggerWalkieTalkie = { member ->
                                viewModel.startVoiceRecording()
                            }
                        )
                    }

                    AppTab.SUPPLIES -> {
                        SupplyExchangeScreen(
                            supplies = supplyItems,
                            onAddSupply = { supply ->
                                viewModel.addSupplyItem(supply)
                            },
                            onResolveSupply = { id ->
                                viewModel.resolveSupplyItem(id)
                            }
                        )
                    }

                    AppTab.CONTACTS -> {
                        val currentCoordinates by viewModel.currentCoordinates.collectAsStateWithLifecycle()
                        val category by viewModel.selectedCategory.collectAsStateWithLifecycle()

                        EmergencyContactsScreen(
                            contacts = emergencyContacts,
                            currentCoordinates = currentCoordinates,
                            nearestHospital = nearestHospital,
                            nearestPolice = nearestPolice,
                            nearestRelief = nearestRelief,
                            selectedCategory = category,
                            lastSmsResult = lastSmsResult,
                            onAddContact = viewModel::addEmergencyContact,
                            onDeleteContact = viewModel::deleteEmergencyContact,
                            onSendTestSms = viewModel::sendTestSms,
                            onOpenMedicalId = { viewModel.selectTab(AppTab.MEDICAL_ID) },
                            onOpenFamilyCircle = { viewModel.selectTab(AppTab.FAMILY) }
                        )
                    }

                    AppTab.NEARBY_HELP -> {
                        val currentCoordinates by viewModel.currentCoordinates.collectAsStateWithLifecycle()

                        NearbyHelpRadarScreen(
                            currentCoordinates = currentCoordinates,
                            onRefreshLocation = viewModel::updateCoordinates
                        )
                    }

                    AppTab.SURVIVAL_GUIDE -> {
                        val customSymptomInput by viewModel.customSymptomInput.collectAsStateWithLifecycle()
                        val customGemmaReport by viewModel.customGemmaReport.collectAsStateWithLifecycle()
                        val triageChatMessages by viewModel.triageChatMessages.collectAsStateWithLifecycle()
                        val isAiTyping by viewModel.isTriageAiTyping.collectAsStateWithLifecycle()
                        val offlineGemmaStatus by viewModel.offlineGemmaStatus.collectAsStateWithLifecycle()

                        SurvivalGuidesScreen(
                            customSymptomInput = customSymptomInput,
                            customGemmaReport = customGemmaReport,
                            triageChatMessages = triageChatMessages,
                            isAiTyping = isAiTyping,
                            offlineGemmaStatus = offlineGemmaStatus,
                            onInstallGemmaModel = viewModel::installOfflineGemmaModel,
                            onSymptomInputChange = viewModel::runCustomGemmaTriage,
                            onSendChatMessage = viewModel::sendTriageChatMessage,
                            onClearChat = viewModel::clearTriageChat,
                            onApplyTriageToSos = viewModel::applyTriageToSos,
                            onTriggerSosShortcut = { viewModel.selectTab(AppTab.CITIZEN_SOS) }
                        )
                    }

                    AppTab.RESPONDER -> {
                        val incidents by viewModel.allIncidents.collectAsStateWithLifecycle()
                        val filter by viewModel.priorityFilter.collectAsStateWithLifecycle()

                        ResponderScreen(
                            incidents = incidents,
                            selectedFilter = filter,
                            onSelectFilter = viewModel::setPriorityFilter,
                            onAcknowledgeAlert = viewModel::acknowledgeAlert
                        )
                    }

                    AppTab.MEDICAL_ID -> {
                        MedicalProfileScreen(
                            profile = medicalProfile,
                            onSaveProfile = viewModel::saveMedicalProfile
                        )
                    }

                    AppTab.DIAGNOSTICS -> {
                        val isBtEnabled by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
                        val isWifiEnabled by viewModel.isWifiEnabled.collectAsStateWithLifecycle()
                        val allIncidents by viewModel.allIncidents.collectAsStateWithLifecycle()
                        val peersList by viewModel.discoveredPeers.collectAsStateWithLifecycle()
                        val logs by viewModel.transmissionLogs.collectAsStateWithLifecycle()
                        val cachedSignalsCount by viewModel.cachedSignalsCount.collectAsStateWithLifecycle()
                        val cachedNodesCount by viewModel.cachedNodesCount.collectAsStateWithLifecycle()

                        MeshDiagnosticsScreen(
                            isBluetoothEnabled = isBtEnabled,
                            isWifiEnabled = isWifiEnabled,
                            isInternetAvailable = isInternetAvailable,
                            deviceId = viewModel.deviceId,
                            totalStoredAlerts = allIncidents.size,
                            connectedPeers = connectedPeers,
                            peersList = peersList,
                            logs = logs,
                            onRefreshState = viewModel::refreshRadioState,
                            cachedSignalsCount = cachedSignalsCount,
                            cachedNodesCount = cachedNodesCount
                        )
                    }
                }
            }
        }

        // Full-screen Heads-Up Tactical AR Compass Dialog
        if (showCompassHud) {
            val targetFacility = nearestHospital ?: nearestPolice ?: emergencyCenters.firstOrNull()
            val currentCoordinates by viewModel.currentCoordinates.collectAsStateWithLifecycle()
            TacticalCompassHudDialog(
                targetFacility = targetFacility,
                currentCoordinates = currentCoordinates,
                azimuthDegrees = compassAzimuth,
                isCompassAvailable = isCompassAvailable,
                onDismiss = { viewModel.setCompassHudVisible(false) }
            )
        }

        // Full-screen red, white, and blue rescue floodlight.
        if (showFloodlight) {
            FloodlightDialog(
                onDismiss = { viewModel.setFloodlightVisible(false) }
            )
        }

        // City Disaster Packs Modal Sheet / Dialog
        if (showCityPacksDialog) {
            Dialog(
                onDismissRequest = { showCityPacksDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                CityDisasterPacksScreen()
            }
        }

    }
}
