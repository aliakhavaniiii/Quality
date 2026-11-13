plugins {
    id("com.android.application")
    kotlin("android")
}
android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.qc.management"
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_1_8; targetCompatibility = JavaVersion.VERSION_1_8 }
    kotlinOptions { jvmTarget = "1.8" }
}
