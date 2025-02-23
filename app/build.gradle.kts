plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Add this line
}

android {
    namespace = "com.example.trustpass"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.trustpass"
        minSdk = 24
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.firebase:firebase-database-ktx") // Specify the library without latest_version
    implementation("com.google.firebase:firebase-firestore:23.0.3")

    // AndroidX Libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.swiperefreshlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
