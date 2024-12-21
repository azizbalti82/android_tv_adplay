plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.balti.project_ads"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.balti.project_ads"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
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
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.appcompat)
    //websocket
    implementation(libs.java.websocket)

    //retrofit for api calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //to read media
    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)
    implementation (libs.androidx.media3.effect)
    implementation (libs.extension.cronet)
    implementation(libs.glide) //for image
    implementation(libs.lottie)


    //to schedule tasks
    implementation(libs.androidx.work.runtime.ktx)

    //for asynchronous
    implementation(libs.kotlinx.coroutines.android)

    //for permissions management
    implementation(libs.dexter)



}