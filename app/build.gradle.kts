plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Firebase plugin
}

android {
    namespace = "com.example.quickcook_project"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.quickcook_project"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true // Enable Jetpack Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0" // Specify the Compose version
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

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    /* Core AndroidX libraries */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation (libs.bcrypt)

    /* Jetpack Compose dependencies */
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.v110)
    implementation (libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    /* Firebase dependencies s*/
    implementation(platform(libs.firebase.bom)) // Firebase BoM for the firebase platform
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.google.firebase.auth)
    implementation(libs.gms.play.services.auth) // Google Play services library and specify its version
    implementation (libs.facebook.android.sdk.v1502)
    implementation(libs.play.services.auth.v2101)

    /* Okhttp dependencies */
    implementation(libs.okhttp)
    implementation(libs.gson) // to format the JSON

    /* Email Sending */
    implementation(libs.sendgrid.sendgrid.java){
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    // Ajoute une version compatible d'Apache HttpClient
    implementation(libs.httpclient)
    implementation(libs.httpcore)
    implementation(libs.firebase.storage.ktx)

    /* Testing */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}