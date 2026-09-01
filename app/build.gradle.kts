plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/*
 * Version handling
 *
 * F-Droid:
 *   fdroidVersionName / fdroidVersionCode are supplied by the
 *   F-Droid metadata file and therefore have absolute priority.
 *
 * GitHub:
 *   For tagged builds, GITHUB_REF_NAME is used directly.
 *   Example: v1.0.1 -> versionName 1.0.1
 *
 * Local / non-tag builds:
 *   Use a safe development version without requiring Git.
 */

private val isFdroidBuild =
    providers.gradleProperty("fdroidVersionName").isPresent ||
    providers.gradleProperty("fdroidVersionCode").isPresent

private fun parseVersionNameFromTag(tag: String): String {
    val version = tag.removePrefix("v").trim()

    require(version.matches(Regex("""\d+\.\d+\.\d+"""))) {
        "Invalid release tag '$tag'. Expected format vMAJOR.MINOR.PATCH."
    }

    return version
}

private fun versionCodeFromVersionName(versionName: String): Int {
    val parts = versionName.split(".").map { it.toInt() }

    require(parts.size == 3) {
        "Invalid version '$versionName'. Expected MAJOR.MINOR.PATCH."
    }

    val (major, minor, patch) = parts

    return major * 1_000_000 +
        minor * 1_000 +
        patch
}

/*
 * F-Droid is completely independent from GitHub's environment.
 *
 * The metadata contains:
 *
 *   gradleprops:
 *     fdroidVersionName=$$VERSION$$
 *     fdroidVersionCode=$$VERCODE$$
 */
val versionName: String
val versionCode: Int

if (isFdroidBuild) {
    versionName = providers.gradleProperty("fdroidVersionName")
        .get()
        .trim()

    versionCode = providers.gradleProperty("fdroidVersionCode")
        .get()
        .trim()
        .toInt()

    println("F-Droid build: versionName=$versionName versionCode=$versionCode")
} else {
    val githubRefName = System.getenv("GITHUB_REF_NAME")

    if (!githubRefName.isNullOrBlank() &&
        githubRefName.matches(Regex("""v\d+\.\d+\.\d+"""))
    ) {
        versionName = parseVersionNameFromTag(githubRefName)
        versionCode = versionCodeFromVersionName(versionName)

        println("GitHub tag build: versionName=$versionName versionCode=$versionCode")
    } else {
        versionName = "1.0.0-dev"
        versionCode = 1_000_000

        println("Development build: versionName=$versionName versionCode=$versionCode")
    }
}

android {
    namespace = "com.mak.claudeassist"

    compileSdk = 34

    defaultConfig {
        applicationId = "com.mak.claudeassist"

        minSdk = 24
        targetSdk = 34

        this.versionName = versionName
        this.versionCode = versionCode
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")

            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
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

            val keystorePath = System.getenv("KEYSTORE_PATH")

            if (!keystorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
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
