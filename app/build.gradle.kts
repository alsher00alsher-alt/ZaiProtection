plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ahmed.zai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ahmed.zai"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

// ده الجزء السحري اللي بيحل كل مشاكل التعارض
configurations.all {
    resolutionStrategy {
        eachDependency {
            when (requested.group) {
                "androidx.core" -> useVersion("1.12.0")
                "androidx.appcompat" -> useVersion("1.6.1")
                "androidx.fragment" -> useVersion("1.6.2")
                "androidx.lifecycle" -> useVersion("2.6.2")
                "androidx.activity" -> useVersion("1.8.2")
                "androidx.emoji2" -> useVersion("1.4.0")
                "androidx.savedstate" -> useVersion("1.2.1")
            }
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
