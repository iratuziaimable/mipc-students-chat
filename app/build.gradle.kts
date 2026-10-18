plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.mipc.studentschat"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.mipc.studentschat"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
