plugins {
    id("com.android.application")
}

android {
    namespace = "com.nyaminthar.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nyaminthar.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.core:core:1.16.0")
}
