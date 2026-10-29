package com.example.data.model

data class SurvivalGuide(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val category: String,
    val urgencyLevel: String, // "CRITICAL", "HIGH", "ESSENTIAL"
    val summary: String,
    val steps: List<String>,
    val dos: List<String>,
    val donts: List<String>,
    val iconName: String
)

object OfflineSurvivalProtocols {
    val guides = listOf(
        SurvivalGuide(
            id = "cpr",
            title = "CPR & Cardiac Emergency",
            hindiTitle = "सीपीआर और दिल का दौरा",
            category = "Life Support",
            urgencyLevel = "CRITICAL",
            summary = "For unresponsive victims not breathing or only gasping. Immediate chest compressions sustain vital blood flow to the brain.",
            steps = listOf(
                "Call for emergency help or dispatch SOS immediately.",
                "Place victim on a firm, flat surface.",
                "Place heel of one hand in center of chest, interlock other hand on top.",
                "Compress hard and fast: 100-120 beats/minute (to the beat of 'Stayin' Alive'), 5 cm (2 inches) deep.",
                "Allow chest to completely recoil between compressions.",
                "Continue until emergency medical responders arrive or victim starts breathing."
            ),
            dos = listOf(
                "Keep arms straight and lock elbows to use body weight.",
                "Switch compressors every 2 minutes if tired.",
                "Check for mouth obstructions if safe."
            ),
            donts = listOf(
                "Do NOT stop compressions for more than 10 seconds.",
                "Do NOT perform CPR on conscious or breathing victims."
            ),
            iconName = "cpr"
        ),
        SurvivalGuide(
            id = "bleeding",
            title = "Severe Bleeding & Tourniquets",
            hindiTitle = "गंभीर रक्तस्राव और टूर्निकेट",
            category = "Trauma",
            urgencyLevel = "CRITICAL",
            summary = "Uncontrolled arterial bleeding can cause death in minutes. Direct high pressure and improvised tourniquets save lives.",
            steps = listOf(
                "Expose wound and immediately apply direct, continuous pressure with clean cloth or hands.",
                "If bleeding does not stop and is on limb, apply a tourniquet 2-3 inches above wound (never directly over a joint).",
                "Wrap strap/cloth tightly, tie a half knot, insert a rigid stick/rod, and twist until bright red bleeding ceases.",
                "Secure the stick in place so it cannot unwind.",
                "Write the exact application time on the victim's forehead or limb (e.g. T 14:30)."
            ),
            dos = listOf(
                "Keep direct pressure uninterrupted while preparing tourniquet.",
                "Keep victim warm with blanket or extra clothing to prevent hypothermia and shock."
            ),
            donts = listOf(
                "Do NOT loosen or remove the tourniquet once placed—only medical surgeons can remove it.",
                "Do NOT use thin wire or string that cuts through tissue."
            ),
            iconName = "bleeding"
        ),
        SurvivalGuide(
            id = "flood",
            title = "Flood Survival & Water Purification",
            hindiTitle = "बाढ़ से बचाव और जल शोधन",
            category = "Natural Disaster",
            urgencyLevel = "HIGH",
            summary = "Flash floods and rising waters create structural collapse, contaminated water, and electrical shock hazards.",
            steps = listOf(
                "Immediately evacuate to upper floor or rooftop if trapped; do not enter closed attics without roof hatch.",
                "Turn off main electrical breaker switch before water reaches outlets.",
                "Signal search helicopters or boats using bright colored cloth or flashlight.",
                "To purify flood water for drinking: Boil vigorously for 1 minute minimum, or add 2 drops of unscented bleach per liter and wait 30 minutes.",
                "Keep emergency phones and documents in sealed plastic ziplock bags."
            ),
            dos = listOf(
                "Wear shoes to prevent puncture wounds from hidden debris underwater.",
                "Listen to battery/crank radio or mesh alerts for evacuation corridors."
            ),
            donts = listOf(
                "Never walk or drive through moving flood waters (6 inches sweeps you off feet; 2 feet floats cars).",
                "Do NOT touch downed power lines or submerged transformer units."
            ),
            iconName = "flood"
        ),
        SurvivalGuide(
            id = "earthquake",
            title = "Earthquake & Structural Collapse",
            hindiTitle = "भूकंप और इमारत ढहना",
            category = "Natural Disaster",
            urgencyLevel = "HIGH",
            summary = "During tremors and building collapse, falling debris and shattered glass pose the highest injury risk.",
            steps = listOf(
                "DROP onto hands and knees immediately.",
                "COVER your head and neck under a sturdy table, desk, or against an interior wall.",
                "HOLD ON to your shelter until shaking stops.",
                "If trapped under rubble: Cover mouth with cloth to avoid breathing dust.",
                "Tap on pipes or wall with stones in groups of 3 (SOS signal) so acoustic listening gear can pinpoint you."
            ),
            dos = listOf(
                "Protect airway from dust inhalation using fabric.",
                "Stay calm and shout only when you hear rescuers nearby to conserve oxygen."
            ),
            donts = listOf(
                "Do NOT run outside during active tremors where facades fall.",
                "Do NOT use elevators or light matches/lighters (gas leak hazard)."
            ),
            iconName = "earthquake"
        ),
        SurvivalGuide(
            id = "burns",
            title = "Burns & Smoke Inhalation",
            hindiTitle = "जलने और धुएं के उपचार",
            category = "First Aid",
            urgencyLevel = "HIGH",
            summary = "Immediate cooling minimizes tissue damage and prevents deep burn shock.",
            steps = listOf(
                "Extinguish flames and remove hot, non-adhering clothing/jewelry quickly before swelling.",
                "Cool burn with clean running cool water for 15-20 minutes.",
                "Cover burn loosely with sterile non-stick bandage or clean plastic wrap.",
                "For smoke inhalation, move immediately to open, fresh air and keep head elevated."
            ),
            dos = listOf(
                "Keep patient hydrated with small sips of water if conscious.",
                "Protect burned area from contamination and dirt."
            ),
            donts = listOf(
                "Do NOT apply ice, ice water, butter, toothpaste, or oil (causes tissue necrosis).",
                "Do NOT pop blisters or pull clothing stuck to charred skin."
            ),
            iconName = "burns"
        ),
        SurvivalGuide(
            id = "fracture",
            title = "Fractures & Bone Splinting",
            hindiTitle = "हड्डी टूटना और स्प्लिंट बांधना",
            category = "Trauma",
            urgencyLevel = "ESSENTIAL",
            summary = "Immobilize the injured joint and bone to prevent severed arteries and nerve damage during transport.",
            steps = listOf(
                "Support injured limb above and below suspected break.",
                "Check for circulation (pulse and warmth in fingers/toes).",
                "Apply rigid support (cardboard, wood, thick magazine) on both sides of limb.",
                "Secure splint with cloth ties or bandaging—firm enough to immobilize, but not cutting off pulse.",
                "Elevate limb slightly if comfortable and apply cold pack wrapped in cloth."
            ),
            dos = listOf(
                "Immobilize the joint above and joint below the fracture.",
                "Pad rigid splints with soft clothing for comfort."
            ),
            donts = listOf(
                "Do NOT attempt to push back protruding bone in compound fractures.",
                "Do NOT straighten a severely deformed limb forcibly."
            ),
            iconName = "fracture"
        ),
        SurvivalGuide(
            id = "snakebite",
            title = "Snakebite & Venomous Stings",
            hindiTitle = "सांप का काटना और विषैले डंक",
            category = "Wildlife / Hazard",
            urgencyLevel = "CRITICAL",
            summary = "Floods force venomous snakes onto high ground where humans seek shelter. Calmness slows venom circulation.",
            steps = listOf(
                "Keep victim extremely calm and still; anxiety accelerates heart rate and venom spread.",
                "Position bitten limb at or slightly below heart level.",
                "Remove rings, tight clothing, or shoes before swelling begins.",
                "Clean bite site gently with soap and water; cover with dry dressing.",
                "Note snake color/pattern if seen safely, or take a quick photo from distance for antivenom selection.",
                "Transport immediately to nearest hospital with antivenom stock."
            ),
            dos = listOf(
                "Immobilize the bitten extremity with a broad splint.",
                "Transport patient lying down on a stretcher or vehicle."
            ),
            donts = listOf(
                "Do NOT cut, suck venom, or use electric shocks.",
                "Do NOT apply ice packs or tight arterial tourniquets."
            ),
            iconName = "snakebite"
        )
    )
}
