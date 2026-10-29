package com.example.data.model

data class CityDisasterPack(
    val id: String,
    val cityName: String,
    val hazardType: String, // "FLOOD", "CYCLONE", "EARTHQUAKE", "HEATWAVE", "GRID_COLLAPSE"
    val riskSeverity: String, // "CRITICAL", "HIGH", "MODERATE"
    val dataSizeMb: Float,
    val shelterCount: Int,
    val isDownloaded: Boolean = false,
    val lastUpdated: String = "October 2026",
    val emergencyAgencies: List<String>,
    val preparationChecklist: List<String>
)

object CityDisasterPacksData {
    val availablePacks = listOf(
        CityDisasterPack(
            id = "PACK_DELHI_NCR",
            cityName = "Delhi NCR (Monsoon & Seismic Zone IV)",
            hazardType = "EARTHQUAKE / URBAN FLOOD",
            riskSeverity = "CRITICAL",
            dataSizeMb = 14.8f,
            shelterCount = 38,
            isDownloaded = true,
            emergencyAgencies = listOf("NDRF Control Room 1078", "Delhi Disaster Management (DDMA) 1077", "AIIMS Trauma 102"),
            preparationChecklist = listOf(
                "Keep 5L bottled water per person in sealed containers",
                "Store portable power banks at 100% with waterproof ziploc",
                "Locate designated Delhi open grounds & DDA park assembly points",
                "Memorize local radio mesh relay frequencies"
            )
        ),
        CityDisasterPack(
            id = "PACK_MUMBAI_COASTAL",
            cityName = "Mumbai & Konkan Coast",
            hazardType = "CYCLONE / MONSOON SURGE",
            riskSeverity = "CRITICAL",
            dataSizeMb = 18.2f,
            shelterCount = 54,
            isDownloaded = false,
            emergencyAgencies = listOf("BMC Disaster Cell 1916", "Indian Coast Guard 1554", "Navy Search & Rescue"),
            preparationChecklist = listOf(
                "Move valuables above high-tide flood line (2nd floor+)",
                "Secure tin roofing and loose balcony materials",
                "Charge Guardian radio mesh devices and seal medicines",
                "Identify high-ground civic shelters near railway lines"
            )
        ),
        CityDisasterPack(
            id = "PACK_HIMALAYAN_SEISMIC",
            cityName = "Uttarakhand & Himachal Faultline",
            hazardType = "LANDSLIDE / FLASH FLOOD",
            riskSeverity = "HIGH",
            dataSizeMb = 12.4f,
            shelterCount = 29,
            isDownloaded = false,
            emergencyAgencies = listOf("SDRF Uttarakhand 1070", "BRO Highway Helpline", "ITBP Rescue Posts"),
            preparationChecklist = listOf(
                "Carry high-decibel whistle and thermal emergency blanket",
                "Avoid riverbeds and unstable slope cutouts",
                "Keep satellite/mesh beacon device in exterior chest pocket",
                "Preload offline offline contour map elevations"
            )
        ),
        CityDisasterPack(
            id = "PACK_CHENNAI_FLOOD",
            cityName = "Chennai & Coromandel Coast",
            hazardType = "NORTHEAST MONSOON INUNDATION",
            riskSeverity = "HIGH",
            dataSizeMb = 15.6f,
            shelterCount = 42,
            isDownloaded = false,
            emergencyAgencies = listOf("Greater Chennai Corp 1913", "Tamil Nadu Disaster Management 1070"),
            preparationChecklist = listOf(
                "Store 3-day non-perishable rations and chlorine tablets",
                "Check Guardian offline relief camp list in Adyar & Velachery",
                "Keep solar torch and battery-powered AM/FM radio handy"
            )
        )
    )
}
