plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.workoutsolidproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.workoutsolidproject"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        compileSdkPreview = "VanillaIceCream"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.workoutsolidproject"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        task("testClasses")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        @Suppress("DEPRECATION")
        exclude ("META-INF/atomicfu.kotlin_module")
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Needed for jar file implementation
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":app:solid-annotation"))
    ksp(project(":app:solid-processor"))
    ksp(project(":app:solid-auth"))

    // jwt creation
    implementation(libs.nimbus.jose.jwt)

    // http services
    implementation(libs.okhttp)

    // datastore
    implementation(libs.androidx.datastore.preferences)

    // reflection for datastore
    implementation(kotlin("reflect"))

    // code verifier util
    // jwt utils
    implementation(libs.appauth)


    debugImplementation(libs.androidx.ui.test.manifest)
    ///////////////////////////////////////////////////////////////

    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation(libs.androidx.credentials.play.services.auth)
    ////////////////////////////////////////////////

    implementation (libs.androidx.room.runtime.v260)
    implementation (libs.androidx.room.ktx.v260)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    ksp (libs.androidx.room.compiler.v260)

    implementation(libs.androidx.appcompat)
    //noinspection GradleDependency
    implementation(libs.androidx.activity.compose) // Use the latest version
    implementation(platform(libs.androidx.compose.bom.v20240600))
    implementation(libs.accompanist.themeadapter.material3)


    implementation(libs.activity.ktx) // Use the latest version


    // Dependencies for working with Architecture components
    // You'll probably have to update the version numbers in build.gradle (Project)

    implementation (libs.androidx.graphics.shapes)
    implementation(libs.androidx.coordinatorlayout)

    // Room components
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.runtime.android)
    implementation (libs.androidx.runtime)
    implementation (libs.androidx.room.runtime)


    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    ksp(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.room.testing)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx.v251)


    implementation(libs.androidx.lifecycle.livedata.ktx)
    //noinspection GradleDependency
    implementation(libs.androidx.lifecycle.common.java8)

    // Kotlin components
    //noinspection GradleDependency
//    implementation(libs.kotlin.stdlib.jdk7)
    implementation (libs.kotlin.stdlib)
    ksp(libs.dagger.compiler)
    api(libs.kotlinx.coroutines.core)
    //noinspection GradleDependency
    api(libs.kotlinx.coroutines.android)

    // UI
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    androidTestImplementation(libs.androidx.junit.v121)
}

