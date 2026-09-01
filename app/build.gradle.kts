import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/*
 * Version handling
 *
 * Release:
 *   Git tag v1.2.3 -> versionName 1.2.3
 *
 * Development:
 *   main / pull request -> versionName 1.2.3-dev
 *
 * versionCode is derived from the GitHub run number for CI builds.
 * For F-Droid, versionCode can be overridden through VERSION_CODE.
 */

fun commandOutput(vararg command: String): String {
    return try {
        val output = ByteArrayOutputStream()

        exec {
            commandLine(*command)
            standardOutput = output
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }

        output.toString().trim()
    } catch (_: Exception) {
        ""
    }
}

fun getGitTag(): String {
    val githubRefName = System.getenv("GITHUB_REF_NAME")

    if (!githubRefName.isNullOrBlank() && githubRefName.startsWith("v")) {
        return githubRefName
    }

    val gitTag = commandOutput("git", "describe", "--tags", "--exact-match")

    return if (gitTag.startsWith("v")) {
        gitTag
    } else {
        ""
    }
}

fun getVersionName(): String {
    val tag = getGitTag()

    if (tag.isNotEmpty()) {
        return tag.removePrefix("v")
    }

    return "1.0.0-dev"
}

fun getVersionCode(): Int {
    /*
     * F-Droid can provide VERSION_CODE explicitly.
     */
    val suppliedVersionCode = System.getenv("VERSION_CODE")
        ?.toIntOrNull()

    if (suppliedVersionCode != null) {
        return suppliedVersionCode
    }

    /*
     * GitHub Actions:
     * Use the GitHub run number for development builds.
     *
     * Release builds get a deterministic versionCode from the
     * semantic version.
     */
    val version = getVersionName()
        .removeSuffix("-dev")

    val parts = version
        .split(".")
        .mapNotNull { it.toIntOrNull() }

    if (parts.size >= 3) {
        val major = parts[0]
        val minor = parts[1]
        val patch = parts[2]

        return major * 1_000_000 +
                minor * 1_000 +
                patch
    }

    return System.getenv("GITHUB_RUN_NUMBER")
        ?.toIntOrNull()
        ?: 1
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

            /*
             * Only GitHub Actions provides KEYSTORE_PATH.
             *
             * F-Droid builds do not provide it, so F-Droid can
             * build the release variant and sign the APK itself.
             */
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