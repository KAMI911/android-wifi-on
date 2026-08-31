import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Read signing config from environment variables (CI) or local.properties (local dev).
// local.properties keys: KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

fun prop(key: String): String? = System.getenv(key) ?: localProps.getProperty(key)

android {
    namespace = "org.kami911.wifion"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.kami911.wifion"
        minSdk = 28
        targetSdk = 31
        versionCode = prop("VERSION_CODE")?.toIntOrNull() ?: 5
        versionName = prop("VERSION_NAME") ?: "0.0.5"
    }

    signingConfigs {
        create("release") {
            val keystorePath = prop("KEYSTORE_FILE")
            val storePass   = prop("KEYSTORE_PASSWORD")
            val alias       = prop("KEY_ALIAS")
            val keyPass     = prop("KEY_PASSWORD")
            if (!keystorePath.isNullOrBlank() && !storePass.isNullOrBlank() &&
                !alias.isNullOrBlank() && !keyPass.isNullOrBlank()
            ) {
                storeFile     = file(keystorePath)
                storePassword = storePass
                keyAlias      = alias
                keyPassword   = keyPass
            }
        }
    }

    flavorDimensions.add("ui")
    productFlavors {
        create("original") {
            dimension = "ui"
            applicationIdSuffix = ""
        }
        create("redesign") {
            dimension = "ui"
            applicationIdSuffix = ".redesign"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.getByName("release")
            if (releaseConfig.storeFile?.isFile == true) {
                signingConfig = releaseConfig
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)

    // Material Design 3
    implementation("androidx.compose.material3:material3:1.2.0")

    // Dark mode & accessibility
    implementation("androidx.appcompat:appcompat:1.7.0")
}
