plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.maps.secrets)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.google.ksp)
}
android {
    namespace = "com.liara.smartass"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.liara.smartass"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "v1.0.0RC9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        manifestPlaceholders["auth0Domain"] = "@string/com_auth0_domain"
        manifestPlaceholders["auth0Scheme"] = "demo"
    }

    buildTypes {
        getByName("debug") {}
        getByName("release").apply {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.material.icons.extended)
    implementation(libs.monitor)
    implementation(libs.junit.ktx)
    implementation(libs.play.services.location)

    // Gson
    implementation(libs.gson)

    // ksp, needed to generate the code for room
    implementation(libs.dagger.compiler)
    implementation(libs.work.runtime.ktx)
    ksp(libs.dagger.compiler)

    // room shit
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Remote connection
    implementation(libs.okhttp)

    // Maps
    implementation(libs.android.maps.utils)
    implementation(libs.play.services.maps)
    // KTX for the Maps SDK for Android Utility Library
    implementation(libs.maps.utils.ktx)
    implementation(libs.maps.compose)


    // Auth0
    implementation(libs.auth0)
    implementation(libs.jwtdecode)

    // JSON
    implementation(libs.kotlinx.serialization.json)

    // ICAL
    implementation(libs.ical4j)

    implementation(libs.ktorm.core)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.activity.compose)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}
