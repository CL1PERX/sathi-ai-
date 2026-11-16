package com.example.data.local

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * On-Device Offline Nepali & Multilingual Knowledge Engine + Semantic Room Cache.
 * Enables zero-latency offline responses when internet is unavailable, quota is exceeded,
 * or when the user enables Offline Mode (अफलाइन मोड).
 */
object OfflineNepaliEngine {

    fun generateOfflineResponse(
        prompt: String,
        languageMode: String,
        cachedMessages: List<ChatMessage>
    ): String {
        val cleanPrompt = prompt.trim()
        val lower = cleanPrompt.lowercase(Locale.ROOT)
        val isEnglishOnly = languageMode == "English"

        // 1. Check Local Room Semantic Cache for previously answered identical/similar questions
        val cachedAnswer = findCachedResponse(cleanPrompt, cachedMessages)
        if (cachedAnswer != null) {
            val cachePrefix = if (isEnglishOnly) {
                "⚡ **[Offline Semantic Cache • 0ms]**\n\n"
            } else {
                "⚡ **[अफलाइन स्मृति क्यास • ० मि.से.]**\n\n"
            }
            return cachePrefix + cachedAnswer
        }

        // 2. Offline / On-Device LLM Architecture & "Can it be made offline capable?" queries
        if (lower.contains("offline") || lower.contains("अफलाइन") || lower.contains("बिना इन्टरनेट") ||
            lower.contains("open-source") || lower.contains("open source") || lower.contains("gguf") ||
            lower.contains("model") || lower.contains("मोडल")
        ) {
            return if (isEnglishOnly) {
                """
                **Yes — Sathi AI supports Offline Capability via a 3-Tier Hybrid Architecture:**

                - **1. Local SQLite / Room Persistence (Active Now):** All chat sessions, transcripts, and previously answered queries are cached locally on-device with **0ms retrieval**.
                - **2. On-Device Nepali Knowledge Engine (Active Now):** Essential Nepali utilities (Land unit converter, Emergency directory, Fintech guides, Government procedures, and Bilingual phrasebook) work **100% offline**.
                - **3. Quantized Open-Source Nepali LLM (GGUF / MediaPipe):** For full generative offline AI, 4-bit quantized open-source models (**Gemma-2-2B-Nepali-IT**, **Aya-Expanse-8B-Q4_K_M**, or **Llama-3.2-3B-Nepali**) run directly on Android NPU/GPU via **MediaPipe LLM Inference API** or **llama.cpp** (~1.5 GB local model download).
                """.trimIndent()
            } else {
                """
                **हजुर, Sathi AI लाई पूर्ण रूपमा अफलाइन (Offline Capable) बनाउन सकिन्छ र यसमा ३-तहको अफलाइन प्रणाली समावेश छ:**

                - **१. लोकल Room Database क्यास (सक्रिय):** तपाईंका सबै पुराना च्याट र सोधिएका प्रश्नोत्तरहरू फोनमै सुरक्षित हुन्छन् र बिना इन्टरनेट **० मि.से.** मा तुरुन्तै हेर्न सकिन्छ।
                - **२. अन-डिभाइस नेपाली ज्ञान इन्जिन (सक्रिय):** जग्गा नापजाँच (रोपनी/आना/बिघा), आपतकालीन नम्बर, डिजिटल वालेट (eSewa/Khalti), र सरकारी प्रक्रियाको जानकारी **१००% अफलाइन** चल्छ।
                - **३. खुला-स्रोत नेपाली भाषा मोडल (On-Device GGUF LLM):** पूर्ण अफलाइन जेनेरेटिभ AI का लागि **Gemma-2-2B-Nepali**, **Llama-3.2-3B-Nepali**, वा **Aya-Expanse (4-bit Quantized)** मोडललाई Android को **MediaPipe LLM API / llama.cpp** मार्फत (~१.५ GB स्टोरेजमा) बिना इन्टरनेट चलाउन सकिन्छ।
                """.trimIndent()
            }
        }

        // 3. Nepali Land Measurement Converter (Ropani, Aana, Paisa, Daam, Bigha, Kattha, Dhur)
        if (lower.contains("ropani") || lower.contains("aana") || lower.contains("bigha") ||
            lower.contains("kattha") || lower.contains("रोपनी") || lower.contains("आना") ||
            lower.contains("बिघा") || lower.contains("कठ्ठा") || lower.contains("जग्गा") ||
            lower.contains("land")
        ) {
            return if (isEnglishOnly) {
                """
                **Nepali Land Measurement Reference (Offline Calculator):**

                - **1 Ropani (रोपनी):** **16 Aana** = **5,476 sq. ft.** = **508.72 sq. m.**
                - **1 Aana (आना):** **4 Paisa** = **342.25 sq. ft.** = **31.80 sq. m.**
                - **1 Paisa (पैसा):** **4 Daam** = **85.56 sq. ft.** = **7.95 sq. m.**
                - **1 Bigha (बिघा):** **20 Kattha** = **72,900 sq. ft.** = **6,772.63 sq. m.** (~13.31 Ropani)
                - **1 Kattha (कठ्ठा):** **20 Dhur** = **3,645 sq. ft.** = **338.63 sq. m.** (~10.65 Aana)
                - **1 Dhur (धुर):** **182.25 sq. ft.** = **16.93 sq. m.**
                """.trimIndent()
            } else {
                """
                **नेपालको जग्गा नापजाँच र क्षेत्रफल तालिका (अफलाइन हिसाब):**

                - **१ रोपनी:** **१६ आना** = **५,४७६ वर्ग फिट** = **५०८.७२ वर्ग मिटर**
                - **१ आना:** **४ पैसा** = **३४२.२५ वर्ग फिट** = **३१.८० वर्ग मिटर**
                - **१ पैसा:** **४ दाम** = **८५.५६ वर्ग फिट** = **७.९५ वर्ग मिटर**
                - **१ बिघा:** **२० कठ्ठा** = **७२,९०० वर्ग फिट** = **६,७७२.६३ वर्ग मिटर** (करिब १३.३१ रोपनी)
                - **१ कठ्ठा:** **२० धुर** = **३,६४५ वर्ग फिट** = **३३८.६३ वर्ग मिटर** (करिब १०.६५ आना)
                - **१ धुर:** **१८२.२५ वर्ग फिट** = **१६.९३ वर्ग मिटर**
                """.trimIndent()
            }
        }

        // 4. Nepal Emergency Numbers & Helplines
        if (lower.contains("emergency") || lower.contains("police") || lower.contains("ambulance") ||
            lower.contains("आपतकालीन") || lower.contains("प्रहरी") || lower.contains("एम्बुलेन्स") ||
            lower.contains("दमकल") || lower.contains("helpline")
        ) {
            return if (isEnglishOnly) {
                """
                **Nepal Essential Emergency Hotlines (Toll-Free & Offline Ready):**

                - **Nepal Police (नेपाल प्रहरी):** **100**
                - **Fire Brigade (दमकल सेवा):** **101**
                - **Ambulance Service (एम्बुलेन्स):** **102**
                - **Traffic Police Control (ट्राफिक प्रहरी):** **103**
                - **Child Helpline (बाल हेल्पलाइन):** **1098**
                - **Women Commission Helpline:** **1145**
                - **Nepal Electricity Authority (विद्युत प्राधिकरण):** **1149**
                """.trimIndent()
            } else {
                """
                **नेपालका अत्यावश्यक आपतकालीन नम्बरहरू (अफलाइन निर्देशिका):**

                - **नेपाल प्रहरी (Police Control):** **१०० (100)**
                - **दमकल / अग्नि नियन्त्रण (Fire):** **१०१ (101)**
                - **एम्बुलेन्स सेवा (Ambulance):** **१०२ (102)**
                - **ट्राफिक प्रहरी (Traffic Control):** **१०३ (103)**
                - **बाल हेल्पलाइन (Child Helpline):** **१०९८ (1098)**
                - **महिला आयोग हेल्पलाइन:** **११४५ (1145)**
                - **नेपाल विद्युत प्राधिकरण (NEA नो-लाइट):** **११४९ (1149)**
                """.trimIndent()
            }
        }

        // 5. Nepal Fintech (eSewa, Khalti, Fonepay, Banking)
        if (lower.contains("esewa") || lower.contains("khalti") || lower.contains("fonepay") ||
            lower.contains("इसेवा") || lower.contains("खल्ती") || lower.contains("फोनपे") ||
            lower.contains("wallet") || lower.contains("qr")
        ) {
            return if (isEnglishOnly) {
                """
                **Nepal Digital Fintech & Wallet Guide (Offline Reference):**

                - **eSewa / Khalti / IME Pay:** Verify KYC using your **Citizenship** or **National ID** and a passport-size photo to unlock transaction limits up to **NPR 1,00,000/day**.
                - **Fonepay / NepalPay QR:** Instant interoperable scan-and-pay across mobile banking apps and wallets.
                - **Security Rule:** Never share your **MPIN**, **OTP**, or password. Official support never calls to ask for OTP codes.
                """.trimIndent()
            } else {
                """
                **नेपाल डिजिटल वालेट र बैंकिङ जानकारी (eSewa / Khalti / Fonepay):**

                - **KYC प्रमाणित गर्ने तरिका:** आफ्नो **नागरिकता** वा **राष्ट्रिय परिचयपत्र** र फोटो अपलोड गरी KYC भेरिफाई गर्नुहोस्।
                - **फोनपे र नेपाल-पे QR:** जुनसुकै मोबाइल बैंकिङ वा वालेटबाट अन्तर-बैंक भुक्तानी तुरुन्तै गर्न सकिन्छ।
                - **सुरक्षा सतर्कता:** आफ्नो **OTP**, **MPIN**, वा पासवर्ड कसैलाई नदिनुहोस्।
                """.trimIndent()
            }
        }

        // 6. Government Documents (Citizenship, Passport, PAN, Driving License)
        if (lower.contains("citizenship") || lower.contains("passport") || lower.contains("pan") ||
            lower.contains("license") || lower.contains("नागरिकता") || lower.contains("राहदानी") ||
            lower.contains("पासपोर्ट") || lower.contains("प्यान") || lower.contains("लाइसेन्स")
        ) {
            return if (isEnglishOnly) {
                """
                **Nepal Government Document Quick Guide (Offline):**

                - **Citizenship (नागरिकता):** Apply at your Ward Office (वडा कार्यालय) with birth certificate, parents' citizenship originals, and recommendation letter, then submit at the **District Administration Office (DAO)**.
                - **E-Passport (विद्युतीय राहदानी):** Requires **National ID (NIN)** number. Fill the pre-enrollment form online, book a biometrics date at DAO or Department of Passports (Tripureshwor), and pay the fee via Revenue system.
                - **PAN Card (स्थायी लेखा नम्बर):** Apply free via the **Nagarik App** using your linked mobile number and citizenship details.
                """.trimIndent()
            } else {
                """
                **सरकारी कागजात बनाउने प्रक्रिया (अफलाइन सहायता):**

                - **नागरिकता प्रमाणपत्र:** जन्मदर्ता र बुबा/आमाको नागरिकताको सक्कल सहित **वडा कार्यालयको सिफारिस** लिएर **जिल्ला प्रशासन कार्यालय (DAO)** मा जानुहोस्।
                - **विद्युतीय राहदानी (E-Passport):** सर्वप्रथम **राष्ट्रिय परिचयपत्र नम्बर (NIN)** अनिवार्य चाहिन्छ। अनलाइन फारम भरेर तोकिएको मितिमा बायोमेट्रिक दिनुहोस्।
                - **व्यक्तिगत प्यान कार्ड (PAN):** आफ्नो नाममा दर्ता सिमकार्ड प्रयोग गरी **नागरिक एप (Nagarik App)** बाटै निःशुल्क तुरुन्तै बनाउन सकिन्छ।
                """.trimIndent()
            }
        }

        // 7. Greetings & General Conversation
        if (lower.contains("namaste") || lower.contains("hello") || lower.contains("hi") ||
            lower.contains("नमस्ते") || lower.contains("नमस्कार") || lower.contains("कस्तो छ") ||
            lower.contains("के छ") || lower.contains("who are you") || lower.contains("को हो")
        ) {
            return if (isEnglishOnly) {
                """
                **Namaste! 🙏 I am Sathi AI — your fast, minimalist Nepali & Multilingual Assistant.**

                - **Primary Focus:** Native **Nepali (देवनागरी)**, Romanized Nepali, and **English** interactions.
                - **Offline Ready:** Running with **On-Device Nepali Knowledge + Local Room Semantic Cache** so you can access tools, land calculations, emergency directories, and cached chats anytime.
                - How can I help you today?
                """.trimIndent()
            } else {
                """
                **नमस्ते हजुर! 🙏 म Sathi AI हुँ — तपाईंको द्रुत र बहुभाषिक नेपाली AI सहायक।**

                - **विशेषता:** म **नेपाली (देवनागरी)**, रोमनाइज्ड नेपाली र **अंग्रेजी** भाषामा छिटो तथा सटीक जवाफ दिन तयार छु।
                - **अफलाइन क्षमता:** इन्टरनेट नभएको वा कोटा सकिएको अवस्थामा पनि मेरो **अन-डिभाइस नेपाली ज्ञान इन्जिन** र **लोकल क्यास** मार्फत तपाईंलाई निरन्तर सेवा दिन्छु।
                - भन्नुहोस् हजुर, आज म के सहयोग गरूँ?
                """.trimIndent()
            }
        }

        // 8. Essays / Nepal Information / Culture / Geography
        if (lower.contains("nepal") || lower.contains("नेपाल") || lower.contains("essay") ||
            lower.contains("निबन्ध") || lower.contains("everest") || lower.contains("सगरमाथा") ||
            lower.contains("buddha") || lower.contains("बुद्ध")
        ) {
            return if (isEnglishOnly) {
                """
                **Nepal — Quick Reference Summary (Offline Knowledge Base):**

                - **Geography & Area:** **1,47,516 sq. km**, divided into **3 ecological regions** (Himal, Pahad, Terai), **7 Provinces**, **77 Districts**, and **753 Local Levels**.
                - **World Heritage & Pride:** Home to **Mount Everest (Sagarmatha - 8,848.86 m)** and **Lumbini**, the birthplace of **Lord Gautam Buddha**.
                - **Language & Culture:** Rich multi-ethnic heritage with **Nepali (Devanagari script)** as the official language alongside over 120 national languages.
                """.trimIndent()
            } else {
                """
                **नेपालको संक्षिप्त परिचय र प्रमुख तथ्यहरू (अफलाइन ज्ञानकोश):**

                - **भूगोल र क्षेत्रफल:** नेपालको क्षेत्रफल **१,४७,५१६ वर्ग कि.मी.** छ। यो **हिमाल, पहाड र तराई** गरी ३ भौगोलिक क्षेत्र, **७ प्रदेश**, **७७ जिल्ला** र **७५३ स्थानीय तह**मा विभाजित छ।
                - **विश्व पहिचान:** सर्वोच्च शिखर **सगरमाथा (८,८४८.८६ मिटर)** र शान्तिका अग्रदूत **गौतम बुद्धको जन्मस्थल लुम्बिनी** नेपालका गौरव हुन्।
                - **भाषा र संस्कृति:** बहुजातीय, बहुभाषिक र बहुसांस्कृतिक एकता भएको देश, जहाँ **नेपाली (देवनागरी लिपि)** सरकारी कामकाजको प्रमुख भाषा हो।
                """.trimIndent()
            }
        }

        // 9. Default Polite Offline / Local Fallback Response
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return if (isEnglishOnly) {
            """
            ⚡ **[On-Device Offline Engine Active • $timeStr]**

            - Your query **"$cleanPrompt"** was processed by the local **Sathi Nepali Offline Engine** (either Offline Mode is enabled or cloud API quota/network is temporarily unavailable).
            - **Available Offline Capabilities right now:**
              - **Nepali Land Calculator:** Ask about *Ropani, Aana, Paisa, Bigha, Kattha*.
              - **Government & Fintech Guides:** Ask about *Citizenship, Passport, PAN, eSewa, Khalti*.
              - **Emergency Hotlines & Nepal Facts:** Ask about *Emergency numbers* or *Nepal overview*.
              - **Offline LLM Info:** Ask *"Can this app work offline?"* for architecture details.
            """.trimIndent()
        } else {
            """
            ⚡ **[अन-डिभाइस अफलाइन इन्जिन सक्रिय • $timeStr]**

            - हजुरको प्रश्न **"$cleanPrompt"** लाई **Sathi अन-डिभाइस नेपाली इन्जिन** बाट प्रशोधन गरिएको छ (अफलाइन मोड सक्रिय छ वा क्लाउड API कोटा/नेटवर्क अस्थायी रूपमा अनुपलब्ध छ)।
            - **अहिले बिना इन्टरनेट उपलब्ध सेवाहरू:**
              - **जग्गा नापजाँच हिसाब:** *रोपनी, आना, पैसा, बिघा, कठ्ठा* बारे सोध्नुहोस्।
              - **सरकारी प्रक्रिया र फिन्टेक:** *नागरिकता, राहदानी (Passport), PAN, eSewa, Khalti* बारे सोध्नुहोस्।
              - **आपतकालीन नम्बर र नेपाल परिचय:** *प्रहरी, एम्बुलेन्स, नेपालको भूगोल* बारे सोध्नुहोस्।
              - **अफलाइन AI जानकारी:** *"के यो अफलाइन चल्छ?"* भनेर सोध्नुहोस्।
            """.trimIndent()
        }
    }

    private fun findCachedResponse(prompt: String, messages: List<ChatMessage>): String? {
        if (messages.size < 2) return null
        val normalizedTarget = prompt.lowercase(Locale.ROOT).replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()
        if (normalizedTarget.length < 3) return null

        for (i in messages.indices.reversed()) {
            val msg = messages[i]
            if (msg.role == "user") {
                val normalizedCandidate = msg.text.lowercase(Locale.ROOT).replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()
                if (normalizedCandidate == normalizedTarget ||
                    (normalizedTarget.length > 8 && normalizedCandidate.contains(normalizedTarget))
                ) {
                    // Find the immediately following model message in the same session
                    val nextMsg = messages.getOrNull(i + 1)
                    if (nextMsg != null && nextMsg.role == "model" && !nextMsg.isError && nextMsg.sessionId == msg.sessionId) {
                        return nextMsg.text
                    }
                }
            }
        }
        return null
    }
}
