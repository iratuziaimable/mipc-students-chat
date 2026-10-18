plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "com.mipc.studentschat"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.mipc.studentschat"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    
    kotlinOptions {
        jvmTarget = "17"
    }
}

