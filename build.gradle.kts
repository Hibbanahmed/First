plugins {
    id("com.android.application") version "8.7.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

buildscript {
    dependencies {
        // Add this line
        classpath("com.google.gms:google-services:4.3.10' // Check for the latest version")
    }
}
