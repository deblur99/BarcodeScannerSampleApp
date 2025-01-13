plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.barcodeexampleproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.barcodeexampleproject"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 1) zxing 라이브러리 방식
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    // 2) Google ML Kit 라이브러리 방식
    implementation("com.google.mlkit:barcode-scanning:17.1.0")

    val cameraxVersion = "1.4.1"
    // CameraX 핵심 라이브러리
    implementation("androidx.camera:camera-core:$cameraxVersion")
    // Camera2 구현체 (없으면 CameraX is not configured properly 런타임 오류 발생)
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    // CameraX Preview View
    implementation("androidx.camera:camera-view:$cameraxVersion")
    // CameraX 라이프사이클
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
