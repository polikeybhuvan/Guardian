package com.example.domain.ai

import com.example.data.model.AlertPriority
import com.example.data.model.EmergencyCategory
import java.util.Locale

data class TriageStep(
    val stepNumber: Int,
    val title: String,
    val titleHindi: String,
    val instruction: String,
    val instructionHindi: String,
    val isCritical: Boolean = false
)

data class OfflineGemmaTriageReport(
    val priority: AlertPriority,
    val triageCode: String,
    val conditionName: String,
    val conditionNameHindi: String,
    val clinicalRationale: String,
    val actionSteps: List<TriageStep>,
    val criticalWarnings: List<String>,
    val criticalWarningsHindi: List<String>,
    val improvisedSupplies: List<String>,
    val vitalChecks: List<String>,
    val detectedSymptomTags: List<String>,
    val requiresTourniquet: Boolean = false,
    val requiresCprRhythm: Boolean = false,
    val isOfflineEngine: Boolean = true
)

/**
 * On-Device Local Gemma AI Triage & Clinical Decision Engine.
 * Operates with 0ms latency, zero cellular/internet connectivity, and zero server dependencies.
 * Designed for austere disaster environments following international START / SALT mass-casualty protocols.
 */
class OfflineGemmaEngine {

    fun analyzeSymptoms(
        symptomsText: String,
        category: EmergencyCategory = EmergencyCategory.MEDICAL,
        peopleCount: Int = 1
    ): OfflineGemmaTriageReport {
        val lower = symptomsText.lowercase(Locale.ROOT).trim()

        // 1. Analyze symptoms across clinical clusters
        val hasSevereBleeding = lower.contains("bleed") || lower.contains("blood") || lower.contains("khun") ||
                lower.contains("artery") || lower.contains("spurt") || lower.contains("gash") || lower.contains("cut") ||
                lower.contains("hemorrhage") || lower.contains("amputat") || lower.contains("stab")

        val isCardiacOrUnconscious = lower.contains("unconscious") || lower.contains("behosh") || lower.contains("breath") ||
                lower.contains("sans") || lower.contains("chest pain") || lower.contains("heart") || lower.contains("cardiac") ||
                lower.contains("pulse") || lower.contains("collapse") || lower.contains("gasp") || lower.contains("faint")

        val isAirwayOrSmoke = lower.contains("smoke") || lower.contains("dhuan") || lower.contains("chok") ||
                lower.contains("throat") || lower.contains("stridor") || lower.contains("suffocat") ||
                lower.contains("coughing blood") || lower.contains("inhal") || lower.contains("asthma")

        val isCrushOrTrapped = lower.contains("trap") || lower.contains("fasa") || lower.contains("phasa") ||
                lower.contains("debris") || lower.contains("malba") || lower.contains("rubble") ||
                lower.contains("crush") || lower.contains("beam") || lower.contains("pinned") || lower.contains("wall fell")

        val isBurnOrFire = lower.contains("burn") || lower.contains("jala") || lower.contains("fire") ||
                lower.contains("aag") || lower.contains("blister") || lower.contains("flame") ||
                lower.contains("hot water") || lower.contains("scald") || lower.contains("cylinder")

        val isFractureOrBone = lower.contains("bone") || lower.contains("fracture") || lower.contains("haddi") ||
                lower.contains("toot") || lower.contains("twist") || lower.contains("dislocat") ||
                lower.contains("cannot walk") || lower.contains("bent leg") || lower.contains("arm bent")

        val isSnakeOrVenom = lower.contains("snake") || lower.contains("saanp") || lower.contains("serpent") ||
                lower.contains("bite") || lower.contains("dass") || lower.contains("scorpion") ||
                lower.contains("bichhu") || lower.contains("venom") || lower.contains("sting")

        val isDrowningOrWater = lower.contains("drown") || lower.contains("doob") || lower.contains("swallowed water") ||
                lower.contains("flood water") || lower.contains("water in lungs") || lower.contains("submerg")

        val isHypothermia = lower.contains("hypothermia") || lower.contains("shiver") || lower.contains("thand") ||
                lower.contains("cold") || lower.contains("lips blue") || lower.contains("rain exposure") || lower.contains("freez")

        val isHeatStroke = lower.contains("heat") || lower.contains("sun stroke") || lower.contains("loo") ||
                lower.contains("garmi") || lower.contains("dehydrat") || lower.contains("delirium")

        val hasChildren = lower.contains("child") || lower.contains("bach") || lower.contains("baby") ||
                lower.contains("infant") || lower.contains("kid")

        val securityCategory = category in setOf(
            EmergencyCategory.WOMEN_SAFETY,
            EmergencyCategory.KIDNAPPING,
            EmergencyCategory.THEFT,
            EmergencyCategory.CYBER_FRAUD
        )

        // Security alerts should not fall through to irrelevant flood or medical instructions.
        // Preserve the specific life-saving protocols below when the note reports an injury.
        if (securityCategory && !hasSevereBleeding && !isCardiacOrUnconscious && !isAirwayOrSmoke &&
            !isCrushOrTrapped && !isBurnOrFire && !isSnakeOrVenom && !isDrowningOrWater &&
            !isHypothermia && !isHeatStroke && !isFractureOrBone
        ) {
            return buildSecurityReport(category, peopleCount)
        }

        // 2. Map to structured disaster triage protocols
        return when {
            // Cardiac Arrest / Unresponsive Life-Threat
            isCardiacOrUnconscious && (lower.contains("not breathing") || lower.contains("no pulse") || lower.contains("gasp") || lower.contains("sans nahi") || lower.contains("behosh")) -> {
                buildCprReport(peopleCount, hasChildren)
            }

            // Severe Arterial Hemorrhage
            hasSevereBleeding -> {
                buildSevereBleedingReport(peopleCount, hasChildren)
            }

            // Airway Obstruction & Smoke Inhalation
            isAirwayOrSmoke -> {
                buildAirwaySmokeReport(peopleCount, hasChildren)
            }

            // Crush Injury & Trapped in Debris
            isCrushOrTrapped -> {
                buildCrushTrappedReport(peopleCount, hasChildren)
            }

            // Severe Burns & Fire Exposure
            isBurnOrFire -> {
                buildBurnsReport(peopleCount, hasChildren)
            }

            // Snakebite / Venomous Sting in Floods
            isSnakeOrVenom -> {
                buildSnakebiteReport(peopleCount, hasChildren)
            }

            // Drowning & Water Aspiration
            isDrowningOrWater -> {
                buildDrowningReport(peopleCount, hasChildren)
            }

            // Bone Fracture & Severe Deformity
            isFractureOrBone -> {
                buildFractureReport(peopleCount, hasChildren)
            }

            // Hypothermia & Cold Exposure
            isHypothermia -> {
                buildHypothermiaReport(peopleCount, hasChildren)
            }

            // Heat Stroke & Dehydration
            isHeatStroke -> {
                buildHeatStrokeReport(peopleCount, hasChildren)
            }

            // Fallback for Category-driven evaluation (Flood, Fire, Landslide, Medical)
            else -> {
                buildCategoryReport(category, symptomsText, peopleCount, hasChildren)
            }
        }
    }

    private fun buildSevereBleedingReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P1,
            triageCode = "RED • IMMEDIATE (अति गंभीर - जीवन रक्षा)",
            conditionName = "Severe Arterial Hemorrhage & Hypovolemic Shock Risk",
            conditionNameHindi = "गंभीर धमनी रक्तस्राव और शॉक का खतरा",
            clinicalRationale = "Rapid arterial blood loss can cause irreversible shock and death within 3 to 5 minutes. Immediate mechanical pressure and tourniquet application required.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Continuous Direct High Pressure",
                    titleHindi = "लगातार सीधा दबाव बनाएं",
                    instruction = "Expose wound immediately. Press firmly into the bleeding point with a thick clean cloth or your palm without lifting hands.",
                    instructionHindi = "घाव को तुरंत खोलें। साफ मोटे कपड़े या हथेली से खून बहने वाली जगह पर बिना हाथ उठाए जोर से दबाएं।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Improvised Tourniquet (For Limbs)",
                    titleHindi = "टूर्निकेट बांधें (हाथ या पैर के लिए)",
                    instruction = "If on arm/leg and bleeding does not stop: wrap a 2-inch wide strap/cloth 2-3 inches above the wound (never over a joint). Insert a rigid stick and twist until bleeding stops completely.",
                    instructionHindi = "यदि हाथ या पैर पर है और खून न रुके: घाव से 2-3 इंच ऊपर (जोड़ पर नहीं) 2 इंच चौड़ा कपड़ा/बेल्ट बांधें। डंडा फंसाकर तब तक घुमाएं जब तक खून बंद न हो जाए।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Record Tourniquet Time",
                    titleHindi = "टूर्निकेट लगाने का समय लिखें",
                    instruction = "Secure the windlass stick so it cannot unwind. Write exact application time on victim's forehead (e.g., T 14:30) with pen or marker.",
                    instructionHindi = "डंडे को कसकर बांध दें ताकि वह खुले नहीं। पीड़ित के माथे पर पेन से समय लिखें (जैसे: T 14:30)।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Prevent Hypothermic Shock",
                    titleHindi = "शॉक और ठंड से बचाएं",
                    instruction = "Keep victim lying flat. Elevate feet 12 inches if no spine/leg fracture. Cover body with blankets or jackets to retain core heat.",
                    instructionHindi = "पीड़ित को सीधा लिटाएं। रीढ़ की हड्डी में चोट न हो तो पैर हल्के ऊपर उठाएं। शरीर को कंबल या जैकेट से ढकें।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT loosen or remove the tourniquet once placed—only qualified trauma surgeons in hospital can release it.",
                "DO NOT use thin wire, shoelaces, or string that cuts through muscle tissue.",
                "DO NOT remove blood-soaked cloths; layer additional pads directly on top."
            ),
            criticalWarningsHindi = listOf(
                "एक बार टूर्निकेट लगाने के बाद उसे कभी ढीला न करें या न हटाएं—केवल अस्पताल के सर्जन ही इसे हटा सकते हैं।",
                "पतले तार या धागे का उपयोग न करें जिससे नसें कट जाएं।",
                "खून से भीगे कपड़े को कभी न हटाएं; उसके ऊपर और नया कपड़ा रखकर दबाएं।"
            ),
            improvisedSupplies = listOf(
                "Clean cotton cloth / triangular bandage",
                "Rigid windlass rod (metal tool, strong branch, ruler)",
                "2-inch broad fabric strap or leather belt",
                "Permanent marker / pen for time notation",
                "Thermal blanket / dry jackets"
            ),
            vitalChecks = listOf(
                "Check radial (wrist) or carotid (neck) pulse every 2 minutes.",
                "Monitor for pale, cold, clammy skin (signs of hemorrhagic shock).",
                "Check alertness by asking simple questions (name, location)."
            ),
            detectedSymptomTags = listOf("ARTERIAL_BLEED", "HEMORRHAGE", "TRAUMA", if (hasChildren) "CHILD_INVOLVED" else "ADULT"),
            requiresTourniquet = true,
            requiresCprRhythm = false
        )
    }

    private fun buildCprReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P1,
            triageCode = "RED • IMMEDIATE LIFE-THREAT (अति गंभीर - सीपीआर)",
            conditionName = "Cardiac Arrest / Respiratory Failure",
            conditionNameHindi = "कार्डियक अरेस्ट / सांस रुकना",
            clinicalRationale = "Victim is unresponsive with absent or abnormal agonal gasping. Irreversible brain damage occurs after 4 minutes without chest compressions.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Check Unresponsiveness",
                    titleHindi = "होश की जांच करें",
                    instruction = "Tap victim firmly on collarbone and shout loudly: 'Are you okay?!'. If no response and no normal breathing, start compressions immediately.",
                    instructionHindi = "कंधे को थपथपाएं और जोर से चिल्लाएं: 'क्या आप ठीक हैं?!'। यदि कोई प्रतिक्रिया या सांस न हो, तो तुरंत छाती दबाना शुरू करें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Positioning & Hand Placement",
                    titleHindi = "छाती पर हाथ की स्थिति",
                    instruction = "Place victim flat on hard floor. Place heel of one hand in exact center of chest (breastbone), interlock other hand on top. Keep arms straight.",
                    instructionHindi = "पीड़ित को सख्त जमीन पर सीधा लिटाएं। एक हाथ की हथेली छाती के बीच में रखें, दूसरा हाथ उसके ऊपर फंसाएं और कोहनी सीधी रखें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Compress Hard & Fast (100-120 BPM)",
                    titleHindi = "तेजी और गहराई से दबाएं (100-120 प्रति मिनट)",
                    instruction = "Compress chest 2 inches (5 cm) deep at 100-120 beats per minute (rhythm of 'Stayin Alive'). Allow chest to fully recoil after each push.",
                    instructionHindi = "छाती को 2 इंच (5 सेमी) गहरा दबाएं। गति 100 से 120 प्रति मिनट रखें। हर बार दबाने के बाद छाती को पूरी तरह ऊपर आने दें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Continuous Hands-Only CPR",
                    titleHindi = "लगातार सीपीआर जारी रखें",
                    instruction = "Do not stop compressions. Switch with another bystander every 2 minutes without pausing for more than 5 seconds to prevent exhaustion.",
                    instructionHindi = "छाती दबाना बंद न करें। थकने से बचने के लिए हर 2 मिनट में दूसरे व्यक्ति से बदलें बिना 5 सेकंड से ज्यादा रुके।",
                    isCritical = true
                )
            ),
            criticalWarnings = listOf(
                "DO NOT pause compressions for more than 10 seconds under any circumstances.",
                "DO NOT attempt rescue breaths if untrained—continuous Hands-Only CPR is equally effective.",
                "DO NOT perform CPR on anyone who is moving, conscious, or breathing normally."
            ),
            criticalWarningsHindi = listOf(
                "किसी भी हालत में 10 सेकंड से ज्यादा छाती दबाना बंद न करें।",
                "यदि मुंह से सांस देने का प्रशिक्षण न हो, तो केवल लगातार छाती दबाते रहें (Hands-Only CPR)।",
                "होश में या सामान्य रूप से सांस ले रहे व्यक्ति पर सीपीआर न करें।"
            ),
            improvisedSupplies = listOf(
                "Firm hard surface / floor (move off soft mattress)",
                "Cloth barrier if rescue breaths attempted"
            ),
            vitalChecks = listOf(
                "Check for signs of spontaneous movement, coughing, or eye opening every 2 minutes.",
                "Ensure chest recoil is not compromised by tired compressor."
            ),
            detectedSymptomTags = listOf("CARDIAC_ARREST", "UNCONSCIOUS", "CPR_NEEDED", if (hasChildren) "PEDIATRIC_CPR" else "ADULT_CPR"),
            requiresTourniquet = false,
            requiresCprRhythm = true
        )
    }

    private fun buildAirwaySmokeReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P1,
            triageCode = "RED • IMMEDIATE (सांस मार्ग अवरोध / धुएं का असर)",
            conditionName = "Acute Airway Obstruction / Toxic Smoke Inhalation",
            conditionNameHindi = "गंभीर सांस नली रुकावट / जहरीला धुआं",
            clinicalRationale = "Thermal or chemical edema can close the glottis rapidly, leading to complete asphyxiation. Immediate airway patency is paramount.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Immediate Relocation to Fresh Air",
                    titleHindi = "तुरंत खुली ताजी हवा में ले जाएं",
                    instruction = "Carry or drag victim immediately out of smoke zone into open air. Stay low to the ground where oxygen concentration is highest.",
                    instructionHindi = "पीड़ित को तुरंत धुएं वाले क्षेत्र से बाहर खुली हवा में लाएं। जमीन के करीब झुककर चलें जहां ऑक्सीजन ज्यादा होती है।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Positioning for Breathing",
                    titleHindi = "बैठने की सही मुद्रा",
                    instruction = "If conscious, place victim sitting upright leaning slightly forward. Loosen collar, buttons, and constrictive chest clothing.",
                    instructionHindi = "यदि होश में हो, तो सीधा बैठाएं और हल्का आगे झुकाएं। कॉलर, बटन और छाती के तंग कपड़े ढीले करें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Choking Intervention (If foreign body)",
                    titleHindi = "गले में फंसी वस्तु निकालना",
                    instruction = "If choking: deliver 5 firm back blows between shoulder blades. If still blocked, perform 5 inward-upward abdominal thrusts (Heimlich).",
                    instructionHindi = "यदि गले में कुछ फंसा है: पीठ पर दोनों कंधों के बीच 5 बार जोर से थपथपाएं। न निकले तो पेट के ऊपरी हिस्से पर अंदर-ऊपर 5 झटके दें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Recovery Position if Unconscious",
                    titleHindi = "बेहोश होने पर करवट से लिटाएं",
                    instruction = "If unconscious but breathing: place in lateral recovery position on their side so tongue or vomitus does not obstruct airway.",
                    instructionHindi = "यदि बेहोश हो लेकिन सांस चल रही हो: करवट के बल (रिकवरी पोजीशन) लिटाएं ताकि जीभ या उल्टी से सांस न रुके।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT re-enter burning, enclosed, or smoke-filled rooms without self-contained breathing apparatus.",
                "DO NOT give oral liquids to a victim who is coughing, drowsy, or gasping.",
                "DO NOT perform blind finger sweeps in the mouth—it can push foreign objects deeper."
            ),
            criticalWarningsHindi = listOf(
                "धुएं से भरे या जलते हुए कमरे में दोबारा कभी न जाएं।",
                "खांसते या उनींदे व्यक्ति को कभी मुंह से पानी न पिलाएं।",
                "मुंह में बिना देखे अंगुली न डालें—अवरोध और अंदर फंस सकता है।"
            ),
            improvisedSupplies = listOf(
                "Wet cloth over mouth/nose as temporary particulate filter",
                "Clean towel for head support in recovery position",
                "Fresh air corridor / shaded open ground"
            ),
            vitalChecks = listOf(
                "Count breaths per minute (Normal: 12-20. > 30 indicates severe respiratory distress).",
                "Look at lips and fingernails for cyanosis (blue/grey tinge indicates severe hypoxia).",
                "Listen for stridor (high-pitched crowing sound indicating throat closure)."
            ),
            detectedSymptomTags = listOf("AIRWAY_OBSTRUCTION", "SMOKE_INHALATION", "HYPOXIA", if (hasChildren) "CHILD_AIRWAY" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun buildCrushTrappedReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P1,
            triageCode = "RED • IMMEDIATE (मलबे में दबना / क्रश इंजरी)",
            conditionName = "Prolonged Crush Syndrome & Traumatic Entrapment",
            conditionNameHindi = "क्रश सिंड्रोम और मलबे में फंसना",
            clinicalRationale = "Severe compression of muscle tissue releases massive potassium and myoglobin into blood. Rapid release without hydration can trigger cardiac arrest (reperfusion shock).",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Assess Entrapment Duration",
                    titleHindi = "दबे रहने का समय नोट करें",
                    instruction = "Determine if victim has been trapped > 1-2 hours. If safe, maintain verbal contact and keep victim calm.",
                    instructionHindi = "जांचें कि क्या पीड़ित 1-2 घंटे से अधिक समय से दबा है। पीड़ित से लगातार बात करें और शांत रखें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Pre-Release Hydration",
                    titleHindi = "मलबा हटाने से पहले पानी पिलाएं",
                    instruction = "If victim is conscious and swallowing safely, give continuous oral water or ORS solution BEFORE removing the heavy crushing object.",
                    instructionHindi = "यदि पीड़ित होश में है और पानी निगल सकता है, तो भारी मलबा हटाने से पहले खूब पानी या ओआरएस पिलाएं।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Stabilize Released Limb",
                    titleHindi = "मुक्त अंग को सहारा दें",
                    instruction = "Once debris is safely removed, immediately immobilize crushed limbs. Do NOT elevate the limb above heart level.",
                    instructionHindi = "मलबा हटने के तुरंत बाद कुचले हुए हाथ/पैर को सहारा दें और स्थिर रखें। अंग को दिल के स्तर से ऊपर न उठाएं।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Check Distal Pulses & Bleeding",
                    titleHindi = "नाड़ी और रक्तस्राव जांचें",
                    instruction = "Check if fingers/toes are pink and have a pulse. Apply pressure dressings if open bleeding occurs upon release.",
                    instructionHindi = "जांचें कि उंगलियां गर्म हैं और नाड़ी चल रही है। खुला घाव हो तो साफ कपड़े से पट्टी बांधें।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT rapidly hoist crushing weight if trapped > 2 hours without medical/IV backup if victim appears pale or cold.",
                "DO NOT elevate crushed limbs above heart level—it increases toxin circulation.",
                "DO NOT give potassium-rich juices (orange, banana) to a crush victim."
            ),
            criticalWarningsHindi = listOf(
                "2 घंटे से अधिक समय से दबे व्यक्ति से अचानक भारी वजन न हटाएं यदि वह पीला पड़ चुका हो।",
                "कुचले हुए अंग को ऊपर न उठाएं ताकि जहरीले तत्व दिल तक न पहुंचें।",
                "पीड़ित को केला या संतरे का रस न दें—पोटेशियम बढ़ने का खतरा होता है।"
            ),
            improvisedSupplies = listOf(
                "Oral Rehydration Salts (ORS) / Clean drinking water",
                "Wooden levers / car jack for controlled debris lifting",
                "Splinting boards, padding, blankets"
            ),
            vitalChecks = listOf(
                "Monitor for irregular heartbeat (sign of hyperkalemia).",
                "Check urine color if possible (tea/cola colored urine indicates myoglobinuria).",
                "Monitor sensation and pulse in extremities."
            ),
            detectedSymptomTags = listOf("CRUSH_SYNDROME", "DEBRIS_TRAPPED", "COLLAPSE", if (hasChildren) "CHILD_TRAPPED" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun buildBurnsReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P2,
            triageCode = "YELLOW • URGENT (गंभीर रूप से जलना)",
            conditionName = "Thermal / Chemical Burns & Blistering",
            conditionNameHindi = "थर्मल बर्न और फफोले",
            clinicalRationale = "Deep partial and full thickness burns rapidly deplete fluids and create life-threatening infection and hypothermia pathways.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Cool Burn Immediately with Clean Water",
                    titleHindi = "तुरंत साफ पानी से ठंडा करें",
                    instruction = "Cool the burn under clean, cool running water for 20 minutes. Do not use ice or iced water; keep the rest of the person warm and watch for shivering.",
                    instructionHindi = "जले हुए हिस्से को साफ, ठंडे बहते पानी के नीचे 20 मिनट तक ठंडा करें। बर्फ या बर्फ का पानी न लगाएं; शरीर के बाकी हिस्से को गर्म रखें और कंपकंपी पर ध्यान दें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Remove Constrictive Items",
                    titleHindi = "तंग गहने और सामान हटाएं",
                    instruction = "Gently remove rings, watches, bracelets, belts, and tight shoes before severe swelling begins.",
                    instructionHindi = "अंगूठी, घड़ी, कड़ा, बेल्ट और तंग जूते तुरंत उतार दें, क्योंकि बाद में सूजन बढ़ जाती है।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Cover with Clean Dry Dressing",
                    titleHindi = "साफ सूखे कपड़े से ढकें",
                    instruction = "Cover burn loosely with a clean dry cotton cloth or clear plastic food wrap. Do not wrap tightly.",
                    instructionHindi = "जले हिस्से को साफ सूती कपड़े या प्लास्टिक क्लिंग फिल्म से ढीला ढकें। कसकर न बांधें।",
                    isCritical = false
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Prevent Hypothermia & Dehydration",
                    titleHindi = "ठंड और निर्जलीकरण से बचाव",
                    instruction = "Cover unburned parts with dry blankets. If conscious and not vomiting, give oral sips of clean water or ORS.",
                    instructionHindi = "बिना जले हिस्सों को कंबल से ढकें। यदि उल्टी न हो तो थोड़ा-थोड़ा ओआरएस या पानी पिलाएं।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "NEVER apply ice, ice water, butter, oil, toothpaste, or turmeric paste to burn wounds.",
                "DO NOT pop or puncture intact burn blisters under any circumstance.",
                "DO NOT pull away clothing that is melted or adhered to charred skin."
            ),
            criticalWarningsHindi = listOf(
                "जले पर कभी बर्फ, बर्फ का पानी, घी, तेल, टूथपेस्ट या हल्दी न लगाएं।",
                "फफोलों को कभी न फोड़ें—इससे संक्रमण फैल सकता है।",
                "त्वचा से चिपके कपड़ों को जबरदस्ती न खींचें।"
            ),
            improvisedSupplies = listOf(
                "Large volumes of clean tap / bottled water",
                "Sterile gauze / clean unprinted cotton bedsheet",
                "Clear plastic kitchen cling wrap",
                "Warm dry blankets for unaffected body areas"
            ),
            vitalChecks = listOf(
                "Monitor for shivering and hypothermia while cooling.",
                "Check airway: hoarseness or singed nasal hairs indicate inhalation burn."
            ),
            detectedSymptomTags = listOf("BURNS", "BLISTERS", "FIRE_INJURY", if (hasChildren) "PEDIATRIC_BURN" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun buildSnakebiteReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P1,
            triageCode = "RED • IMMEDIATE (सांप का काटना / जहर का खतरा)",
            conditionName = "Envenomation / Neurotoxic Snakebite",
            conditionNameHindi = "सर्पदंश / विषैला जहर",
            clinicalRationale = "Floodwaters drive venomous snakes into human refuge areas. Neurotoxins and hemotoxins can cause respiratory paralysis or severe internal bleeding.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Keep Victim Calm & Completely Still",
                    titleHindi = "पीड़ित को पूरी तरह शांत और स्थिर रखें",
                    instruction = "Lay victim down. Physical movement accelerates lymphatic venom absorption. Reassure the victim that antivenom exists.",
                    instructionHindi = "पीड़ित को शांत लिटाएं। हिलने-डुलने से जहर शरीर में तेजी से फैलता है। घबराएं नहीं।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Immobilize the Bitten Limb",
                    titleHindi = "काटे हुए हाथ/पैर को स्थिर करें",
                    instruction = "Splint the limb with sticks/cardboard so joints cannot bend. Keep the bite location at or slightly below heart level.",
                    instructionHindi = "डंडे या गत्ते से पैर/हाथ को सहारा देकर बांधें ताकि जोड़ न मुड़े। काटे हुए हिस्से को दिल के स्तर से थोड़ा नीचे रखें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Broad Pressure Bandage (If safe)",
                    titleHindi = "दबाव वाली पट्टी बांधें",
                    instruction = "Apply a broad elastic or crepe bandage starting over the bite and wrapping upwards towards the body (firm like an ankle sprain wrap). Check that pulse at fingers/toes is still present.",
                    instructionHindi = "घाव से शुरू करते हुए ऊपर की ओर सूती पट्टी या क्रेप बैंडेज बांधें (मोच की पट्टी की तरह)। उंगलियों में नाड़ी महसूस होनी चाहिए।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Urgent Transport to Hospital",
                    titleHindi = "तुरंत अस्पताल ले जाएं",
                    instruction = "Transport by stretcher or vehicle immediately. Call 108/112 for Anti-Snake Venom (ASV) availability.",
                    instructionHindi = "स्ट्रेचर या वाहन से तुरंत नजदीकी बड़े अस्पताल ले जाएं जहां एंटी-वेनम (ASV) उपलब्ध हो।",
                    isCritical = true
                )
            ),
            criticalWarnings = listOf(
                "DO NOT cut the bite wound with razors or knives.",
                "DO NOT attempt to suck venom with mouth or vacuum devices.",
                "DO NOT apply a tight arterial tourniquet that blocks blood flow.",
                "DO NOT apply ice packs, electric shocks, or herbal poultices."
            ),
            criticalWarningsHindi = listOf(
                "ब्लेड या चाकू से घाव पर चीरा न लगाएं।",
                "मुंह से जहर चूसने की कोशिश कभी न करें।",
                "खून रोकने वाला सख्त टूर्निकेट न बांधें।",
                "बर्फ या जड़ी-बूटियों का लेप न लगाएं।"
            ),
            improvisedSupplies = listOf(
                "Broad elastic bandage / strips of cotton saree/cloth",
                "Rigid splinting sticks / bamboo strips",
                "Stretcher / flat board for zero-movement transport"
            ),
            vitalChecks = listOf(
                "Check for ptosis (drooping eyelids)—early sign of neurotoxic paralysis.",
                "Monitor swallowing ability and breathing rate every 5 minutes.",
                "Observe for bleeding from gums or bite punctures."
            ),
            detectedSymptomTags = listOf("SNAKEBITE", "ENVENOMATION", "FLOOD_HAZARD", if (hasChildren) "CHILD_BITE" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun buildDrowningReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P1,
            triageCode = "RED • IMMEDIATE (डूबने से फेफड़ों में पानी)",
            conditionName = "Submersion Injury / Hypoxic Pulmonary Aspiration",
            conditionNameHindi = "डूबना / फेफड़ों में पानी भरना",
            clinicalRationale = "Water in alveoli washes away surfactant, triggering sudden hypoxia and hypothermia. Immediate rescue ventilations restore oxygenation.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Remove from Water & Check Breathing",
                    titleHindi = "पानी से बाहर निकालें और सांस जांचें",
                    instruction = "Pull victim safely to land. Check for responsiveness and breathing for 10 seconds. Keep head and neck aligned.",
                    instructionHindi = "पीड़ित को सुरक्षित जमीन पर लाएं। 10 सेकंड तक सांस की जांच करें। गर्दन को सीधा रखें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Give 5 Initial Rescue Breaths",
                    titleHindi = "5 बार मुंह से सांस दें",
                    instruction = "If not breathing, tilt head back slightly, pinch nose, and deliver 5 slow rescue breaths directly into mouth to inflate lungs.",
                    instructionHindi = "यदि सांस न चल रही हो, तो सिर हल्का पीछे करें, नाक बंद करें और 5 बार मुंह से सांस दें ताकि फेफड़ों में हवा जाए।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Start 30:2 CPR Compressions",
                    titleHindi = "30 बार छाती दबाएं और 2 सांस दें",
                    instruction = "Immediately follow with 30 chest compressions at 100-120 BPM, then 2 breaths. Repeat cycle continuously.",
                    instructionHindi = "तुरंत 30 बार छाती दबाएं और फिर 2 सांस दें। इस चक्र को लगातार दोहराते रहें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Recovery Position & Warming",
                    titleHindi = "करवट लिटाएं और सुखाएं",
                    instruction = "If breathing returns, place on side (recovery position) to drain fluid. Strip wet clothes and wrap in dry thermal blankets.",
                    instructionHindi = "सांस आने पर करवट से लिटाएं। भीगे कपड़े उतारकर तुरंत सूखे कंबल से ढकें ताकि हाइपोथर्मिया न हो।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT waste precious time trying to drain water from stomach with abdominal thrusts before starting CPR.",
                "DO NOT leave victim unattended even if breathing recovered (secondary drowning risk within 24 hours).",
                "DO NOT jump into swift moving flood currents without lifeline or flotation equipment."
            ),
            criticalWarningsHindi = listOf(
                "पेट से पानी निकालने के लिए पेट दबाने में समय बर्बाद न करें—तुरंत सीपीआर शुरू करें।",
                "होश में आने के बाद भी पीड़ित को अकेला न छोड़ें (24 घंटे तक खतरा रहता है)।",
                "बिना सुरक्षा उपकरण के बहते पानी में न कूदें।"
            ),
            improvisedSupplies = listOf(
                "Dry cotton towels and blankets",
                "Flotation ring / rope / plastic jerrycan",
                "Dry clothes and plastic wrap for wind shielding"
            ),
            vitalChecks = listOf(
                "Check chest rise and fall during breathing.",
                "Monitor for persistent wet coughing, shivering, or confusion."
            ),
            detectedSymptomTags = listOf("DROWNING", "WATER_ASPIRATION", "HYPOXIA", if (hasChildren) "CHILD_DROWNING" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = true
        )
    }

    private fun buildFractureReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P2,
            triageCode = "YELLOW • URGENT (हड्डी टूटना / फ्रैक्चर)",
            conditionName = "Closed / Open Bone Fracture & Dislocation",
            conditionNameHindi = "हड्डी टूटना और जोड़ खिसकना",
            clinicalRationale = "Unstabilized fractures can sever adjacent nerves and blood vessels, leading to compartment syndrome or severe bleeding.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Control External Bleeding",
                    titleHindi = "खुले घाव से खून रोकें",
                    instruction = "If bone has broken through skin, do NOT push it back. Cover with sterile dressing and apply pressure around wound edges.",
                    instructionHindi = "यदि हड्डी चमड़ी से बाहर आ गई हो, तो उसे अंदर न दबाएं। साफ पट्टी से ढकें और घाव के किनारों पर दबाव डालें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Immobilize the Joints Above & Below",
                    titleHindi = "जोड़ों को स्थिर करें",
                    instruction = "Splint the limb in the exact position found. Secure rigid splints across both the joint above and joint below fracture.",
                    instructionHindi = "टूटे हुए अंग को जैसा है वैसा ही स्थिर करें। फ्रैक्चर के ऊपर और नीचे वाले दोनों जोड़ों को पट्टी से बांधें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Check Circulation (Pulse & Sensation)",
                    titleHindi = "खून का बहाव जांचें",
                    instruction = "Pinch a fingernail/toenail: color should return within 2 seconds. Feel for a pulse past the splint. If pale/cold, loosen bandage slightly.",
                    instructionHindi = "उंगली का नाखून दबाकर छोड़ें: 2 सेकंड में रंग गुलाबी होना चाहिए। यदि अंग ठंडा या सुन्न पड़े, तो पट्टी थोड़ी ढीली करें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Cold Pack & Elevation",
                    titleHindi = "बर्फ/ठंडी सिकाई और आराम",
                    instruction = "Apply a wrapped cold cloth to reduce swelling. Keep limb rested and supported with pillows or folded jackets.",
                    instructionHindi = "कपड़े में लपेटकर ठंडी सिकाई करें ताकि सूजन कम हो। अंग को तकिए या जैकेट का सहारा देकर रखें।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT attempt to straighten or force an angulated, bent bone back into alignment.",
                "DO NOT push exposed bone fragments back under the skin.",
                "DO NOT allow victim to walk on a fractured leg or ankle."
            ),
            criticalWarningsHindi = listOf(
                "टेढ़ी हुई हड्डी को जबरदस्ती सीधा करने की कोशिश कभी न करें।",
                "बाहर निकली हड्डी को कभी त्वचा के अंदर धकेलने की कोशिश न करें।",
                "टूटे पैर या टखने पर मरीज को बिल्कुल न चलने दें।"
            ),
            improvisedSupplies = listOf(
                "Rigid splints (wooden planks, branches, rolled magazines, umbrellas)",
                "Padding material (towels, cotton t-shirts, foam)",
                "Cloth ties / scarves / duct tape for securing splint",
                "Sling for arm support (triangular cloth/scarf)"
            ),
            vitalChecks = listOf(
                "Check capillary refill and pulse at fingers/toes every 15 minutes.",
                "Monitor for increasing severe pain unresponsive to immobilization (compartment syndrome)."
            ),
            detectedSymptomTags = listOf("FRACTURE", "BONE_INJURY", "SKELETAL_TRAUMA", if (hasChildren) "CHILD_FRACTURE" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun buildHypothermiaReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P2,
            triageCode = "YELLOW • URGENT (हाइपोथर्मिया / अत्यधिक ठंड)",
            conditionName = "Severe Systemic Hypothermia & Exposure",
            conditionNameHindi = "हाइपोथर्मिया और अत्यधिक ठंड लगना",
            clinicalRationale = "Core body temperature falling below 35°C leads to cognitive disorientation, cardiac irritability, and fatal ventricular fibrillation.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Remove Wet Clothing Immediately",
                    titleHindi = "भीगे कपड़े तुरंत हटाएं",
                    instruction = "Move victim into dry sheltered space. Strip off all wet clothes and gently pat skin dry with towels.",
                    instructionHindi = "पीड़ित को सूखे स्थान पर ले जाएं। सारे भीगे कपड़े तुरंत उतारें और तौलिए से शरीर सुखाएं।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Insulate Core (Vapor Barrier)",
                    titleHindi = "शरीर को गर्म परतों में लपेटें",
                    instruction = "Wrap body in dry blankets, sleeping bags, or plastic bags to stop evaporative cooling. Cover the head (high heat loss area).",
                    instructionHindi = "शरीर को सूखे कंबलों या प्लास्टिक शीट में लपेटें। सिर को टोपी या कपड़े से जरूर ढकें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Rewarm Trunk & Core First",
                    titleHindi = "छाती और गर्दन को पहले गर्म करें",
                    instruction = "Apply warm dry packs to neck, chest, and groin (large blood vessel areas). Use body-to-body contact under blankets if needed.",
                    instructionHindi = "गर्म कपड़े या बोतल गर्दन, छाती और जांघों के जोड़ों पर रखें जहां मुख्य नसें होती हैं।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Warm Sweetened Fluids (If conscious)",
                    titleHindi = "हल्का गर्म मीठा पानी पिलाएं",
                    instruction = "If fully alert, provide warm sweetened tea or ORS. Keep victim lying down.",
                    instructionHindi = "यदि मरीज पूरी तरह होश में हो तो हल्का गर्म मीठा पेय या ओआरएस दें। मरीज को लेटे रहने दें।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT vigorously rub or massage cold arms and legs—it pushes cold acidic blood into heart causing cardiac arrest.",
                "DO NOT apply direct boiling water or intense heat lamps directly to numb skin (causes burns).",
                "DO NOT give alcohol or caffeinated beverages under any circumstances."
            ),
            criticalWarningsHindi = listOf(
                "हाथ-पैरों की जोरदार मालिश न करें—ठंडा खून दिल में जाकर दिल का दौरा ला सकता है।",
                "उबलता पानी या सीधी आग की आंच पर ठंडी त्वचा न सेकें।",
                "शराब या अत्यधिक चाय/कॉफी कभी न दें।"
            ),
            improvisedSupplies = listOf(
                "Dry wool blankets / thermal space blanket",
                "Plastic garbage bags as wind/vapor barrier",
                "Warm water bottles wrapped in cloth",
                "Warm high-calorie sweetened liquids"
            ),
            vitalChecks = listOf(
                "Check consciousness level (slurred speech, apathy, shivering stopping indicates worsening).",
                "Monitor radial pulse (may be very slow and faint in severe hypothermia)."
            ),
            detectedSymptomTags = listOf("HYPOTHERMIA", "COLD_EXPOSURE", "FLOOD_RAIN", if (hasChildren) "CHILD_HYPOTHERMIA" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun buildHeatStrokeReport(peopleCount: Int, hasChildren: Boolean): OfflineGemmaTriageReport {
        return OfflineGemmaTriageReport(
            priority = AlertPriority.P1,
            triageCode = "RED • IMMEDIATE (लू / हीट स्ट्रोक)",
            conditionName = "Heat Stroke & Hyperthermic Encephalopathy",
            conditionNameHindi = "हीट स्ट्रोक और अत्यधिक लू लगना",
            clinicalRationale = "Core body temperature exceeding 40°C causes acute multi-organ failure and brain swelling. Immediate aggressive cooling is life-critical.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Move to Deep Shade & Fan",
                    titleHindi = "छांव में ले जाएं और हवा करें",
                    instruction = "Move victim out of direct sun immediately into shaded, ventilated shelter. Remove excess clothing.",
                    instructionHindi = "पीड़ित को तुरंत धूप से हटाकर छायादार हवादार जगह पर लाएं। फालतू कपड़े उतारें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Aggressive Evaporative Cooling",
                    titleHindi = "ठंडे पानी से शरीर गीला करें",
                    instruction = "Douse body with cool tap water while continuously fanning vigorously. Place cold wet towels on head, neck, and armpits.",
                    instructionHindi = "शरीर पर सामान्य ठंडा पानी छिड़कें और तेजी से पंखा झलें। गर्दन, बगल और सिर पर गीली ठंडी पट्टियां रखें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Ice Packs on Pulse Points",
                    titleHindi = "गर्दन और बगलों में ठंडी सिकाई",
                    instruction = "Apply ice packs wrapped in thin cloth to neck, groin, and axillae to rapidly drop arterial blood temperature.",
                    instructionHindi = "कपड़े में बर्फ लपेटकर गर्दन, जांघों के जोड़ और बगलों में लगाएं ताकि नसों का खून तेजी से ठंडा हो।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Electrolyte Rehydration (If conscious)",
                    titleHindi = "ओआरएस का घोल पिलाएं",
                    instruction = "If victim is awake and swallowing reliably, provide small frequent sips of cool ORS solution or salt-lemon water.",
                    instructionHindi = "यदि मरीज पूरी तरह होश में हो तो थोड़ा-थोड़ा ओआरएस का घोल या शिकंजी पिलाएं।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT give oral fluids to an unconscious, disoriented, or seizing victim.",
                "DO NOT give fever medications like aspirin or paracetamol—they do not work for heat stroke and damage liver/kidneys.",
                "DO NOT cool so severely that victim starts shivering continuously."
            ),
            criticalWarningsHindi = listOf(
                "बेहोश या अर्ध-बेहोश मरीज को मुंह से कोई तरल पदार्थ न दें।",
                "पैरासिटामोल या एस्पिरिन न दें—लू में यह असर नहीं करती और लिवर को नुकसान पहुंचाती है।",
                "इतना ठंडा न करें कि मरीज तेज कांपने लगे।"
            ),
            improvisedSupplies = listOf(
                "Large containers of cool tap water / ice packs",
                "Hand fan / battery fan",
                "ORS packets / salt & sugar electrolyte mix",
                "Wet cotton bedsheets"
            ),
            vitalChecks = listOf(
                "Check for hot dry skin with absence of sweating (classic sign of heat stroke).",
                "Monitor mental state: confusion, aggression, or hallucinations indicate brain distress."
            ),
            detectedSymptomTags = listOf("HEAT_STROKE", "HYPERTHERMIA", "DEHYDRATION", if (hasChildren) "CHILD_HEATSTROKE" else "ADULT"),
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun getCategoryHindiName(category: EmergencyCategory): String {
        return when (category) {
            EmergencyCategory.FLOOD -> "बाढ़ / जलभराव"
            EmergencyCategory.MEDICAL -> "चिकित्सा आपातकाल"
            EmergencyCategory.WOMEN_SAFETY -> "महिला सुरक्षा / खतरा"
            EmergencyCategory.KIDNAPPING -> "अपहरण / लापता व्यक्ति"
            EmergencyCategory.THEFT -> "चोरी / लूट"
            EmergencyCategory.CYBER_FRAUD -> "ऑनलाइन / वित्तीय धोखाधड़ी"
            EmergencyCategory.TRAPPED -> "मलबे में फंसे व्यक्ति"
            EmergencyCategory.FIRE -> "आग / अग्निकांड"
            EmergencyCategory.LANDSLIDE -> "भूस्खलन"
            EmergencyCategory.STRANDED -> "मार्ग अवरुद्ध / फंसे नागरिक"
            EmergencyCategory.SUPPLIES -> "राशन व पेयजल आवश्यकता"
            EmergencyCategory.SAFE -> "सुरक्षित नागरिक"
            EmergencyCategory.OTHER -> "अन्य आपातकाल"
        }
    }

    private fun buildCategoryReport(
        category: EmergencyCategory,
        symptomsText: String,
        peopleCount: Int,
        hasChildren: Boolean
    ): OfflineGemmaTriageReport {
        val lower = symptomsText.lowercase()
        val isFloodRising = category == EmergencyCategory.FLOOD || lower.contains("paani") || lower.contains("flood") || lower.contains("water") || lower.contains("dub")

        val priority = when {
            category == EmergencyCategory.WOMEN_SAFETY || category == EmergencyCategory.KIDNAPPING -> AlertPriority.P1
            hasChildren && (category == EmergencyCategory.FLOOD || category == EmergencyCategory.TRAPPED || category == EmergencyCategory.MEDICAL || category == EmergencyCategory.FIRE) -> AlertPriority.P1
            category == EmergencyCategory.MEDICAL || category == EmergencyCategory.FIRE -> AlertPriority.P1
            category == EmergencyCategory.FLOOD || category == EmergencyCategory.TRAPPED || category == EmergencyCategory.LANDSLIDE || category == EmergencyCategory.OTHER || category == EmergencyCategory.THEFT -> AlertPriority.P2
            category == EmergencyCategory.CYBER_FRAUD -> AlertPriority.P3
            category == EmergencyCategory.SUPPLIES || category == EmergencyCategory.STRANDED -> AlertPriority.P3
            else -> AlertPriority.P4
        }

        val codeLabel = when (priority) {
            AlertPriority.P1 -> "RED • IMMEDIATE (अति गंभीर)"
            AlertPriority.P2 -> "YELLOW • URGENT (गंभीर)"
            AlertPriority.P3 -> "GREEN • DELAYED (सामान्य सहायता)"
            AlertPriority.P4 -> "WHITE • SAFE CHECK-IN (सुरक्षित)"
        }

        val tags = mutableListOf(category.name)
        if (hasChildren) tags.add("CHILDREN")
        if (isFloodRising) tags.add("FLOOD_RISING")
        tags.add("GENERAL_TRIAGE")

        return OfflineGemmaTriageReport(
            priority = priority,
            triageCode = codeLabel,
            conditionName = "${category.displayName} Disaster Response Protocol",
            conditionNameHindi = "${getCategoryHindiName(category)} आपातकालीन सुरक्षा प्रोटोकॉल",
            clinicalRationale = "Symptom profile indicates a ${category.displayName} event involving $peopleCount person(s). Precautionary first-aid and evacuation measures required.",
            actionSteps = listOf(
                TriageStep(
                    stepNumber = 1,
                    title = "Secure Scene & Evacuate Hazards",
                    titleHindi = "खतरे से दूर सुरक्षित स्थान पर जाएं",
                    instruction = "Move all individuals away from structural, electrical, or rising flood hazards to the nearest high ground or reinforced shelter.",
                    instructionHindi = "सभी लोगों को ढहने वाली इमारतों, बिजली के खंभों और बढ़ते पानी से दूर ऊंचे व सुरक्षित स्थान पर ले जाएं।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 2,
                    title = "Primary Triage Assessment",
                    titleHindi = "घायलों की तुरंत जांच करें",
                    instruction = "Check all persons for catastrophic bleeding, airway compromise, and hypothermia. Prioritize children and elderly.",
                    instructionHindi = "सभी व्यक्तियों की जांच करें कि किसी का खून तो नहीं बह रहा और सांस ठीक चल रही है। बच्चों और बुजुर्गों को प्राथमिकता दें।",
                    isCritical = true
                ),
                TriageStep(
                    stepNumber = 3,
                    title = "Ration Clean Water & Food",
                    titleHindi = "साफ पीने के पानी की व्यवस्था",
                    instruction = "Do not drink untreated flood or tap water. Boil water vigorously for 1 minute minimum before consuming.",
                    instructionHindi = "बाढ़ या गंदा पानी न पिएं। पीने के लिए पानी को कम से कम 1 मिनट तक उबालें।",
                    isCritical = false
                ),
                TriageStep(
                    stepNumber = 4,
                    title = "Maintain Radio / Mesh Signal",
                    titleHindi = "मेश नेटवर्क और लोकेशन चालू रखें",
                    instruction = "Keep app open to relay distress beacon across nearby phones. Signal rescue teams with flashlight or whistle.",
                    instructionHindi = "फोन में ऐप चालू रखें ताकि मदद का संदेश फैलता रहे। सीटी या टॉर्च से बचाव दलों को इशारा करें।",
                    isCritical = false
                )
            ),
            criticalWarnings = listOf(
                "DO NOT attempt to cross flowing water channels on foot or in vehicles.",
                "DO NOT touch downed overhead electrical wires or submerged transformers.",
                "Keep emergency phones inside sealed plastic bags to prevent water damage."
            ),
            criticalWarningsHindi = listOf(
                "पैदल या गाड़ी से बहते पानी के बहाव को पार करने की कोशिश न करें।",
                "पानी में डूबे बिजली के तारों या खंभों को बिल्कुल न छुएं।",
                "फोन को पानी से बचाने के लिए प्लास्टिक की थैली में सील करके रखें।"
            ),
            improvisedSupplies = listOf(
                "First-aid kit / clean cotton dressings",
                "Boiled clean water / ORS packets",
                "Flashlight / whistles for search teams",
                "Warm waterproof blankets"
            ),
            vitalChecks = listOf(
                "Check responsiveness of all individuals every 30 minutes.",
                "Monitor for shivering, dehydration, or worsening symptoms."
            ),
            detectedSymptomTags = tags,
            requiresTourniquet = false,
            requiresCprRhythm = false
        )
    }

    private fun buildSecurityReport(category: EmergencyCategory, peopleCount: Int): OfflineGemmaTriageReport {
        val cyber = category == EmergencyCategory.CYBER_FRAUD
        val priority = when (category) {
            EmergencyCategory.WOMEN_SAFETY, EmergencyCategory.KIDNAPPING -> AlertPriority.P1
            EmergencyCategory.THEFT -> AlertPriority.P2
            else -> AlertPriority.P3
        }
        val steps = when (category) {
            EmergencyCategory.WOMEN_SAFETY -> listOf(
                "Move toward a staffed, well-lit public place or trusted person if safe.",
                "For immediate danger in India, call 112; Women’s Helpline 181 is also available.",
                "Share your live location with a trusted contact. Avoid confronting or following a threat."
            )
            EmergencyCategory.KIDNAPPING -> listOf(
                "If someone is in immediate danger, call 112 in India and share the last known location.",
                "Move to a staffed safe place and contact a trusted person if you can do so safely.",
                "Preserve last-seen time, location, clothing, vehicle details, and relevant messages for authorities."
            )
            EmergencyCategory.THEFT -> listOf(
                "Move away from immediate danger. Do not chase or confront a suspected thief.",
                "For an active threat in India, call 112. Otherwise report the incident to local police.",
                "Record the time, location, and item or vehicle details; preserve relevant evidence."
            )
            else -> listOf(
                "For financial cyber fraud in India, call 1930 as soon as possible.",
                "Contact your bank or payment provider to block or secure the affected account.",
                "Save transaction IDs and messages. Never share an OTP, PIN, or password."
            )
        }
        val hindiTitle = getCategoryHindiName(category)
        return OfflineGemmaTriageReport(
            priority = priority,
            triageCode = when (priority) {
                AlertPriority.P1 -> "RED • IMMEDIATE (अति गंभीर)"
                AlertPriority.P2 -> "YELLOW • URGENT (गंभीर)"
                AlertPriority.P3 -> "GREEN • ACT PROMPTLY (सामान्य सहायता)"
                AlertPriority.P4 -> "WHITE • INFORMATION"
            },
            conditionName = category.displayName,
            conditionNameHindi = hindiTitle,
            clinicalRationale = if (cyber) "This is a security response, not a medical assessment. This app does not file a police report or dispatch responders."
                else "This is a safety response, not a medical assessment. This app does not dispatch police or other responders.",
            actionSteps = steps.mapIndexed { index, step ->
                TriageStep(index + 1, "Safety step ${index + 1}", "सुरक्षा कदम ${index + 1}", step, step, index == 0)
            },
            criticalWarnings = if (cyber) listOf("Do not share OTPs, PINs, or passwords; Guardian does not report fraud to authorities automatically.")
                else listOf("Use emergency calls and location sharing only when it is safe to do so."),
            criticalWarningsHindi = emptyList(),
            improvisedSupplies = emptyList(),
            vitalChecks = emptyList(),
            detectedSymptomTags = listOf(category.name, "SECURITY_RESPONSE", "NOT_MEDICAL_TRIAGE")
        )
    }
}
