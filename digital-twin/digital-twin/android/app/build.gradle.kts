plugins {
}

android {
    namespace = "com.datamint.digitaltwin"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.datamint.digitaltwin"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
}

dependencies {
}