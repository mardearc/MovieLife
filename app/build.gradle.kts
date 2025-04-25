plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Google services
    alias(libs.plugins.google.services)

}

android {
    namespace = "com.example.movielife"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.movielife"
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
        isCoreLibraryDesugaringEnabled = true

    }
    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "2.1"
        apiVersion = "2.1"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //Picasso
    implementation ("com.squareup.picasso:picasso:2.8")

    //FireBase
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    implementation(libs.androidx.activity)
    implementation(libs.androidx.annotation)
    implementation(libs.play.services.basement)
    implementation(libs.firebase.auth.ktx)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.firebase.database.ktx)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}