import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/*
 * Versionierung
 *
 * Quelle der Wahrheit ist ausschließlich der Git-Tag (Format: vX.Y.Z).
 * Kein System.getenv(...) hier drin — funktioniert identisch lokal,
 * in GitHub Actions und im F-Droid-Builder, weil alle drei einen
 * echten Git-Checkout mit Tags haben.
 *
 * Kein Tag am aktuellen Commit -> "1.0.0-dev" (lokale Entwicklungsbuilds).
 */

fun commandOutput(vararg command: String): String {
    return try {
        val process = ProcessBuilder(*command)
            .redirectErrorStream(false)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        output
    } catch (e: Exception) {
        ""
    }
}

fun getGitTag(): String {
    val tag = commandOutput("git", "describe", "--tags", "--exact-match")
    return if (tag.startsWith("v")) tag else ""
}

fun getVersionName(): String {
    val tag = getGitTag()
    return if (tag.isNotEmpty()) tag.removePrefix("v") else "1.0.0-dev"
}

fun getVersionCode(): Int {
    (project.findProperty("versionCode") as String?)?.toIntOrNull()?.let { return it }

    val version = getVersionName().removeSuffix("-dev")
    val parts = version.split(".").mapNotNull { it.toIntOrNull() }

    if (parts.size >= 3) {
        return parts[0] * 1_000_000 + parts[1] * 1_000 + parts[2]
    }

    return 1
}

/*
 * Signing
 *
 * Liest ausschließlich aus keystore.properties im Projekt-Root.
 * Diese Datei existiert NIE im Repo — lokal legt jede:r sie selbst an,
 * CI erzeugt sie temporär aus Secrets, F-Droid berührt sie gar nicht
 * (dort bleibt storeFile == null, release läuft dann unsigniert durch,
 * denn F-Droid signiert selbst mit seinem eigenen Schlüssel).
 */
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.mak.claudeassist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mak.claudeassist"
        minSdk = 24
        targetSdk = 34

        versionCode = getVersionCode()
        versionName = getVersionName()
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties.getProperty("keystoreFile")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = keystoreProperties.getProperty("keystorePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = false

            val releaseSigningConfig = signingConfigs.getByName("release")
            if (releaseSigningConfig.storeFile != null) {
                signingConfig = releaseSigningConfig
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.webkit:webkit:1.11.0")
}