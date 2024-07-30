import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.tomsky.hitv"
    compileSdk = 34

    signingConfigs {
        create("release") {
            keyAlias = "hitv"
            keyPassword = "tomsky"
            storeFile = file("itv.keystore")
            storePassword = "tomsky"
        }
    }

    defaultConfig {
        applicationId = "com.tomsky.hitv"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.1"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.glide)
    implementation(libs.recyclerview)
}