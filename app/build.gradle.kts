plugins {
id("com.android.application")
id("org.jetbrains.kotlin.android")
}

fun gitTag(): String {
return providers.exec {
commandLine(
"git",
"describe",
"--tags",
"--exact-match",
"--match",
"v[0-9].[0-9].[0-9]*"
)
}.standardOutput.asText.get().trim()
}

val versionName = providers.gradleProperty("fdroidVersionName")
.orElse(
providers.provider {
gitTag().removePrefix("v")
}
)
.get()

val versionCode = providers.gradleProperty("fdroidVersionCode")
.orElse(
providers.provider {
val (major, minor, patch) = versionName
.split(".")
.map(String::toLong)

        (major * 1_000_000L + minor * 1_000L + patch).toString()
    }
)
.get()
.toInt()

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