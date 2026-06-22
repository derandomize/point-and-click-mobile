import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.podzemnayapochta"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.podzemnayapochta"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

/**
 * Линтер игрового контента (см. docs/architecture.md — контент-пайплайн).
 * Проверяет ссылочную целостность assets/content/game.json:
 *  - стартовая локация существует;
 *  - связанные локации, NPC, адресаты писем и узлы диалогов ссылаются на существующие id;
 *  - id уникальны.
 */
@Suppress("UNCHECKED_CAST")
val lintContent =
    tasks.register("lintContent") {
        group = "verification"
        description = "Проверяет ссылочную целостность assets/content/game.json"

        val contentFile = file("src/main/assets/content/game.json")
        inputs.file(contentFile)

        doLast {
            require(contentFile.exists()) { "Не найден файл контента: $contentFile" }

            val root = JsonSlurper().parse(contentFile) as Map<String, Any?>
            val errors = mutableListOf<String>()

            val locations = root["locations"] as? List<Map<String, Any?>> ?: emptyList()
            val npcs = root["npcs"] as? List<Map<String, Any?>> ?: emptyList()
            val letters = root["letters"] as? List<Map<String, Any?>> ?: emptyList()
            val dialogues = root["dialogues"] as? List<Map<String, Any?>> ?: emptyList()

            val locationIds = locations.map { it["id"] as String }
            val npcIds = npcs.map { it["id"] as String }
            val dialogueIds = dialogues.map { it["id"] as String }
            val npcDialogueRoot = npcs.associate { (it["id"] as String) to (it["dialogueRootId"] as? String) }

            fun checkUnique(
                name: String,
                ids: List<String>,
            ) {
                val dupes =
                    ids
                        .groupingBy { it }
                        .eachCount()
                        .filter { it.value > 1 }
                        .keys
                if (dupes.isNotEmpty()) errors += "Дублирующиеся $name id: $dupes"
            }
            checkUnique("location", locationIds)
            checkUnique("npc", npcIds)
            checkUnique("dialogue", dialogueIds)

            val start = root["startLocationId"] as? String
            if (start == null || start !in locationIds) {
                errors += "startLocationId='$start' не указывает на существующую локацию"
            }

            locations.forEach { loc ->
                val id = loc["id"]
                (loc["connectedLocationIds"] as? List<String>).orEmpty().forEach { target ->
                    if (target !in locationIds) errors += "Локация '$id' ссылается на несуществующую '$target'"
                }
                (loc["npcIds"] as? List<String>).orEmpty().forEach { npc ->
                    if (npc !in npcIds) errors += "Локация '$id' ссылается на несуществующего NPC '$npc'"
                }
            }

            npcs.forEach { npc ->
                val id = npc["id"]
                val loc = npc["locationId"] as? String
                if (loc !in locationIds) errors += "NPC '$id' привязан к несуществующей локации '$loc'"
                val root2 = npc["dialogueRootId"] as? String
                if (root2 != null && root2 !in dialogueIds) {
                    errors += "NPC '$id' ссылается на несуществующий диалог '$root2'"
                }
            }

            letters.forEach { letter ->
                val id = letter["id"]
                val recipient = letter["recipientNpcId"] as? String
                if (recipient !in npcIds) {
                    errors += "Письмо '$id' адресовано несуществующему NPC '$recipient'"
                } else if (npcDialogueRoot[recipient] == null) {
                    errors += "Письмо '$id' адресовано NPC '$recipient' без диалога (dialogueRootId) — вручить нельзя"
                }
            }

            // Все флаги, которые когда-либо выставляются эффектами диалогов.
            val producedFlags =
                dialogues
                    .flatMap { (it["choices"] as? List<Map<String, Any?>>).orEmpty() }
                    .flatMap { (it["effects"] as? List<Map<String, Any?>>).orEmpty() }
                    .mapNotNull { it["flag"] as? String }
                    .toSet()

            dialogues.forEach { node ->
                val id = node["id"]
                (node["choices"] as? List<Map<String, Any?>>).orEmpty().forEach { choice ->
                    val target = choice["targetNodeId"] as? String
                    if (target != null && target !in dialogueIds) {
                        errors += "Диалог '$id' ведёт в несуществующий узел '$target'"
                    }
                    val condFlag = (choice["condition"] as? Map<String, Any?>)?.get("flag") as? String
                    if (condFlag != null && condFlag !in producedFlags) {
                        errors += "Диалог '$id' проверяет флаг '$condFlag', который не выставляется ни одним effects"
                    }
                }
            }

            if (errors.isNotEmpty()) {
                throw GradleException(
                    "Линтер контента нашёл ${errors.size} проблем:\n" + errors.joinToString("\n") { " - $it" },
                )
            }
            logger.lifecycle("Контент валиден: ${locationIds.size} локаций, ${npcIds.size} NPC, ${letters.size} писем.")
        }
    }

tasks.named("check") {
    dependsOn(lintContent)
}
